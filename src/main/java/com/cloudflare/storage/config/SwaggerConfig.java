package com.cloudflare.storage.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Cloudflare R2 Access Token");

        SecurityRequirement bearerRequirement = new SecurityRequirement()
                .addList("Bearer Authentication");

        return new OpenAPI()
                .info(new Info()
                        .title("Cloudflare R2 存储API")
                        .version("1.0.0")
                        .description("Cloudflare R2 对象存储管理API文档")
                        .license(new License()
                                .name("Cloudflare R2 使用协议")
                                .url("https://developers.cloudflare.com/r2/"))
                        .contact(new Contact()
                                .name("Cloudflare Support")
                                .email("support@cloudflare.com")
                                .url("https://developers.cloudflare.com/r2/")) )
                .externalDocs(new ExternalDocumentation()
                        .description("Cloudflare R2 官方文档")
                        .url("https://developers.cloudflare.com/r2/"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", bearerScheme))
                .addSecurityItem(bearerRequirement);
    }
}
