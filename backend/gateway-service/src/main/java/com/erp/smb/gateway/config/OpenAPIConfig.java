package com.erp.smb.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {
  @Bean
  public OpenAPI gatewayOpenAPI() {
    return new OpenAPI().info(new Info()
      .title("Gateway API")
      .description("Reverse proxy endpoints and admin APIs")
      .version("1.0.0"));
  }
}
