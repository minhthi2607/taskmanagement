package com.project.taskmanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Value("${app.upload-dir:uploads/}")
    private String uploadDir;



    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.boot.web.servlet.FilterRegistrationBean<jakarta.servlet.Filter> attachmentDownloadFilter() {
        org.springframework.boot.web.servlet.FilterRegistrationBean<jakarta.servlet.Filter> registrationBean = new org.springframework.boot.web.servlet.FilterRegistrationBean<>();
        
        registrationBean.setFilter(new jakarta.servlet.Filter() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response, jakarta.servlet.FilterChain chain) 
                    throws java.io.IOException, jakarta.servlet.ServletException {
                if (response instanceof jakarta.servlet.http.HttpServletResponse) {
                    jakarta.servlet.http.HttpServletResponse httpResponse = (jakarta.servlet.http.HttpServletResponse) response;
                    httpResponse.setHeader("Content-Disposition", "attachment");
                    httpResponse.setHeader("X-Content-Type-Options", "nosniff");
                }
                chain.doFilter(request, response);
            }
        });
        
        registrationBean.addUrlPatterns("/uploads/attachments/*");
        return registrationBean;
    }
}
