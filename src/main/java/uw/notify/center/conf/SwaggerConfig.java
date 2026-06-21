package uw.notify.center.conf;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * OpenAPI / Swagger 文档配置，仅在 {@code debug}/{@code dev} profile 下生效。
 * <p>
 * 附加 Bearer Token 鉴权方案，并按 controller 包路径划分分组，便于在 Swagger UI 中分模块浏览。
 *
 * @author axeon
 */
@Configuration
@Profile({"debug","dev"})
public class SwaggerConfig {

    /**
     * 应用名称
     */
    @Value("${project.name}")
    private String appName;

    /**
     * 应用版本
     */
    @Value("${project.version}")
    private String appVersion;

    /**
     * 配置全局 OpenAPI 文档：附加 Bearer Token 鉴权方案、应用标题与联系方式。
     *
     * @return 用于增强所有分组的 OpenApiCustomizer
     */
    @Bean
    public OpenApiCustomizer customOpenAPI() {
        return openApi -> openApi
                .addSecurityItem(new SecurityRequirement().addList("AuthToken"))
                .components(openApi.getComponents().addSecuritySchemes("AuthToken", new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").in(SecurityScheme.In.HEADER)))
                .info(new Info().title(appName).version(appVersion)
                        .contact(new Contact().name("axeon").email("23231269@qq.com")));
    }

    /**
     * user API 接口分组，扫描 {@code uw.notify.center.controller.user} 包。
     *
     * @param customOpenAPI 全局 OpenApiCustomizer
     * @return userApi 分组配置
     */
    @Bean
    public GroupedOpenApi userApi(OpenApiCustomizer customOpenAPI) {
        return GroupedOpenApi.builder()
                .group("userApi")
                .packagesToScan("uw.notify.center.controller.user")
                .addOpenApiCustomizer(customOpenAPI)
                .build();
    }

}
