package com.cooknect.recipe_service.config;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecipeSwaggerConfig {

    @Bean
    public OpenAPI RecipeServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Recipe Service")
                        .description("This is the REST API for Recipe Service ")
                        .version("v0.0.1")
                        .license(new License().name("Apache 2.0")));
    }

}
