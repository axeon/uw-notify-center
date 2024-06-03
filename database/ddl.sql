-- tinyurl.sys_seq definition

CREATE TABLE `sys_seq` (
                           `seq_name` varchar(200) NOT NULL COMMENT '序列名',
                           `seq_id` bigint DEFAULT NULL COMMENT '当前序列id',
                           `seq_desc` varchar(200) DEFAULT NULL COMMENT '序列描述',
                           `increment_num` int DEFAULT NULL COMMENT '每次递增大小',
                           `create_date` datetime(3) DEFAULT NULL COMMENT '建立日期',
                           `last_update` datetime(3) DEFAULT NULL COMMENT '最后更新日期',
                           PRIMARY KEY (`seq_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='SYS序列';


-- notify.notify_msg definition

CREATE TABLE `notify_msg` (
                              `id` bigint NOT NULL COMMENT '主键ID',
                              `user_id` bigint NOT NULL COMMENT '用户ID',
                              `saas_id` bigint NOT NULL COMMENT '运营商编号',
                              `mch_id` bigint DEFAULT NULL COMMENT '商户编号',
                              `user_type` int DEFAULT NULL COMMENT '用户类型',
                              `object_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '通知类型',
                              `object_id` bigint DEFAULT NULL COMMENT '业务关联ID',
                              `notify_subject` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '消息内容',
                              `notify_content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '消息内容',
                              `action_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '跳转链接',
                              `create_date` datetime NOT NULL COMMENT '创建时间',
                              `sent_date` datetime DEFAULT NULL COMMENT '发送时间',
                              `state` int NOT NULL COMMENT '消息状态(0-待发送  1-已发送)',
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息推送web端记录';