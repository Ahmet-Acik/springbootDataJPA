package com.example.springdatajpa.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Boot JPA Learning API")
                        .version("1.0.0")
                        .description("A comprehensive Spring Boot application demonstrating JPA concepts including entities, repositories, services, and REST APIs. " +
                                   "This API provides full CRUD operations for Student, Course, Department, and Enrollment management.")
                        .contact(new Contact()
                                .name("Spring Boot JPA Team")
                                .email("support@springbootjpa.com")
                                .url("https://github.com/Ahmet-Acik/springbootDataJPA"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Development Server"),
                        new Server()
                                .url("https://api.springbootjpa.com")
                                .description("Production Server (if available)")
                ));
    }
}