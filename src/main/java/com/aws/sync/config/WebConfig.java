//package com.aws.sync.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//
//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//
//
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        // /home/file/**为前端URL访问路径 后面 file:xxxx为本地磁盘映射
////        registry.addResourceHandler("/home/file/**").addResourceLocations("file:imgs/" );
//        registry.addResourceHandler("notify/**").addResourceLocations("file:/root/notify/" );
//    }
//}