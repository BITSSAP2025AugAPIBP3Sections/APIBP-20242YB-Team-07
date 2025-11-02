package com.cooknect.gateway_service.configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerGateWayConfig {
    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cooknect Gateway API Documentation")
                        .description("Aggregated Swagger documentation for all Cooknect microservices")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Cooknect Dev Team")
                                .email("support@cooknect.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }

}
