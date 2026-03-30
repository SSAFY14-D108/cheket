package com.ssafy.cheket.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String authSchemeName = "bearerAuth";
        String queueSchemeName = "queueToken";

        return new OpenAPI().components(new Components()
            .addSecuritySchemes(authSchemeName,
                new SecurityScheme().name(authSchemeName).type(SecurityScheme.Type.HTTP).scheme("bearer")
                    .bearerFormat("JWT"))
            .addSecuritySchemes(queueSchemeName, new SecurityScheme().name("Queue-Token")
                .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER)));
    }

}
