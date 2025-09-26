package com.example.uidiff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.out-dir}")
    private String outDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // map /results/** -> file từ ./run-results/**
        String loc = "file:" + (outDir.endsWith("/") ? outDir : outDir + "/");
        registry.addResourceHandler("/results/**").addResourceLocations(loc);
    }
}
