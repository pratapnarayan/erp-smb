package com.erp.smb.order.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {
  @Bean
  public OpenAPI orderOpenAPI() {
    final String schemeName = "bearerAuth";
    return new OpenAPI()
        .info(new Info().title("Order Service API").description("Sales order lifecycle").version("1.0.0"))
        .components(new Components().addSecuritySchemes(schemeName, new SecurityScheme()
            .name(schemeName).type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList(schemeName));
  }
}
