package org.ecommerce.v1.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce API")
                        .description("REST API for products, users, orders, categories, and inventory. Use the endpoints below to explore and test. GraphQL is available at /graphql with GraphiQL at /graphql (when enabled).")
                        .version("1.0"))
                .servers(List.of(
                        new Server().url("/").description("Current host")
                ));
    }
}
