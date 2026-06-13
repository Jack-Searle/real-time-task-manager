
package com.jacksearle.backend.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("forward:/index.html");
        registry.addViewController("/register").setViewName("forward:/index.html");
        registry.addViewController("/verify-email").setViewName("forward:/index.html");
        registry.addViewController("/dashboard").setViewName("forward:/index.html");
        registry.addViewController("/dashboard/boards/{boardId}").setViewName("forward:/index.html");
        registry.addViewController("/dashboard/invites/{token}").setViewName("forward:/index.html");
    }
}
