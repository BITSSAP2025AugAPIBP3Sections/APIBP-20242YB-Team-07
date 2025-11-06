package com.cooknect.user_service.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.util.List;

@Configuration
public class UserServiceSwaggerConfig {

    final String securitySchemeName = "bearerAuth";

    @Bean
    public OpenAPI userServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Recipe Service")
                        .description("This is the REST API for User Service")
                        .version("v0.0.1")
                        .license(new License().name("Apache 2.0")))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ))
                .servers(List.of(
                        new Server().url("http://localhost:8089").description("Gateway URL")
                ));
    }

}
 