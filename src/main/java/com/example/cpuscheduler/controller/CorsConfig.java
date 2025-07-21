package com.example.cpuscheduler.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:8080",     // Docker frontend
                        "http://localhost:5177",     // Local Vite dev server
                        "http://localhost:3000",     // React dev server
                        "http://localhost:5173",     // Alternative Vite port
                        "http://127.0.0.1:8080",     // Docker frontend (IP)
                        "http://127.0.0.1:5177",     // Local Vite dev server (IP)
                        "http://frontend:80",        // Docker internal network
                        "http://cpuscheduler-frontend:80" // Docker container name
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}