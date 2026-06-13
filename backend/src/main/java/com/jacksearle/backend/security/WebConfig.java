package com.jacksearle.backend.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/{path:^(?!api|actuator|ws|assets).*}")
                .setViewName("forward:/index.html");
        registry.addViewController("/dashboard/{path:^(?!api|actuator|ws|assets).*}")
                .setViewName("forward:/index.html");
        registry.addViewController("/dashboard/**/{path:^(?!api|actuator|ws|assets).*}")
                .setViewName("forward:/index.html");
    }
}
