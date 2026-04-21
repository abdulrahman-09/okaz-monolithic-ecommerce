package com.am9.okazx.config;



import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Okazx E-Commerce API")
                        .version("1.0.0")
                        .description("""
                                REST API for the Okazx e-commerce platform.

                                **Roles:**
                                - `CUSTOMER` — browse products, manage cart, place & cancel own orders
                                
                                - `ADMIN` — full product management, view all orders, update order statuses

                                **Authentication:** Use `POST /api/v1/auth/login` to obtain a JWT Bearer token,
                                then click the **Authorize** button and enter: `Bearer <your_token>`
                                """)
                )
                .addSecurityItem(new SecurityRequirement().addList("Bearer Auth"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Auth",
                                new SecurityScheme()
                                        .name("Bearer Auth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}

