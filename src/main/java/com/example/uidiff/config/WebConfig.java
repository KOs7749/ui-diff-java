package com.example.uidiff.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // map /results/** -> thư mục run-results/ trong project
    registry.addResourceHandler("/results/**")
            .addResourceLocations("file:run-results/");
  }
}
