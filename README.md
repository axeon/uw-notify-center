# uw-notify-center

基于 SSE（Server-Sent Events）的 Web 实时通知中心。负责维护用户级 SSE 长连接，并通过 Redis Pub/Sub 在多实例间路由投递，把通知实时下发给**在线**的目标用户。

## 架构概览

```
            ┌───────────────────────┐        HTTP RPC
 业务服务 ──▶│ NotifyClientHelper    │─────────────────▶┌────────────────────────┐
 (依赖        │ (uw-notify-client)   │                   │  uw-notify-center      │
  client SDK) └───────────────────────┘                   │  /rpc/notify/pushNotify│
                                                          └───────────┬────────────┘
                          目标用户不在线 ┌─────────────────┴────────────────┐
                  Redis Pub/Sub 广播     │                                  │
            ┌─────────────────────────────▼──────────────┐                   │
            │  本地连接池命中?  ──是──▶ SSE 推送给该用户    │                   │
            │       │否                                   │                   │
            │       └─(订阅侧 autoRelay=false,不再广播)    │                   │
            └────────────────────────────────────────────┘                   │
  浏览器 ───GET /user/notify/stream (SSE)─────────────────────────────────────┘
```

### 核心组件

| 模块 | 职责 |
|------|------|
| `WebNotifyService` | SSE 连接池（`userId → SseEmitter`）、消息投递、心跳保活、跨实例转发入口 |
| `NotifyRpcController` | `/rpc/notify/pushNotify`，服务间 RPC 入口（`autoRelay=true`） |
| `NotifyUserController` | `/user/notify/stream`，前端建立 SSE 长连接 |
| `RedisNotifyListener` | 订阅 `uw-notify` 通道，反序列化后本地投递（`autoRelay=false`，避免循环广播） |
| `NotifyCenterAutoConfiguration` | 装配 `RedisTemplate`（内部持有 Lettuce 连接工厂）、订阅容器（虚拟线程池派发） |
| `UwNotifyCenterProperties` | 配置：SSE 超时、心跳间隔、Redis 连接 |

### 跨实例投递流程

1. 业务服务通过 `NotifyClientHelper.pushNotify(...)` 发起 HTTP RPC 到 notify-center（经负载均衡落到某一实例）。
2. 该实例 `pushMsg(msg, autoRelay=true)`：若目标用户在本地连接池中，直接 SSE 推送。
3. 本地未命中时，按 `cluster-mode` 分流：
   - **多机模式（默认 `true`）**：向 `uw-notify` 通道发布广播，所有实例的 `RedisNotifyListener` 以 `autoRelay=false` 再次尝试本地投递——仅持有该用户连接的实例会成功，其余实例静默跳过。
   - **单机模式（`false`）**：直接视为用户不在线，不发起广播，也不产生自发自收。

> 注：Redis Pub/Sub 为 at-most-once 语义，目标用户不在线时消息不缓存、不重投，仅适合"在线实时触达"场景。

## 配置说明

配置前缀 `uw.notify.center`，典型 `bootstrap.yml` 片段：

```yaml
uw:
  notify:
    center:
      sse-timeout: 60000              # SSE 连接超时（毫秒），0=不过期
      sse-heartbeat-interval: 30000   # 心跳间隔（毫秒），实际下限 10s
      cluster-mode: true             # 多机集群模式：true=本地未命中走 Redis 广播；false=单机不广播
      redis:                          # notify-center 自用 Redis（Lettuce standalone）
        host: 127.0.0.1
        port: 6379
        database: 0
        lettuce:
          pool:
            max-active: 8
            max-idle: 8
            min-idle: 0
          shutdown-timeout: 100ms
```

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `sse-timeout` | `60000` | SSE 连接超时（毫秒） |
| `sse-heartbeat-interval` | `30000` | 心跳保活间隔，对超过该间隔未发送数据的连接发送 SSE 注释帧 |
| `cluster-mode` | `true` | 是否多机集群模式。`false`=单机部署，本地未命中即视为不在线，不发起 Redis 广播（也消除自发自收）；多实例部署**必须**为 `true`，否则跨实例用户收不到通知。默认 `true` 保证忘配时 fail-safe |
| `redis.*` | Spring Boot 默认 | notify-center 自用 Redis 连接参数 |

## 客户端接入

业务服务依赖 `uw-notify-client`，配置服务地址后即可静态调用：

```java
// userId 必须为全局唯一且 > 0；当前不支持广播
WebNotifyMsg msg = new WebNotifyMsg(userId, saasId,
        new WebNotifyMsg.NotifyBody("ORDER", "订单通知", "您的订单已支付", payload));

ResponseData result = NotifyClientHelper.pushNotify(msg);
```

`NotifyClientHelper` 通过 `uw-auth-client` 提供的共享 `RestClient`（带服务间鉴权拦截器）发起调用。

## 设计约束

- **仅支持定向投递**：SSE 连接池以全局唯一的 `userId` 为单维 key，不支持按 `userId` 或 `saasId` 维度的广播；`userId<=0` 的消息会被直接拒绝，避免误入 Redis Pub/Sub 造成放大流量。
- **userId 全局唯一**：跨所有运营商（saasId）唯一，因此连接池以裸 `userId` 为 key 不会跨租户串号；`saasId` 仅作为消息溯源字段，不参与投递路由。
- **异常文案安全**：对外响应（RPC / Client 返回）使用固定文案，不回传 `e.getMessage()`，避免泄露序列化框架等内部信息。

## 运行环境

- JDK 25（虚拟线程用于 Redis 订阅消息派发）
- Spring Boot + Spring Cloud（Nacos 注册与配置）
- Redis（standalone，Pub/Sub 通道 `uw-notify`）
