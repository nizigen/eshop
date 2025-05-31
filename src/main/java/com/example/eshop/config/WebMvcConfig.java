package com.example.eshop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  private final FileStorageProperties fileStorageProperties;

  @Autowired
  public WebMvcConfig(FileStorageProperties fileStorageProperties) {
    this.fileStorageProperties = fileStorageProperties;
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    // 认证相关
    registry.addViewController("/auth/login").setViewName("auth/login");
    registry.addViewController("/auth/register").setViewName("auth/register");

    // 用户中心相关
    registry.addViewController("/user/orders").setViewName("user/order-list");
    registry.addViewController("/user/points").setViewName("user/points");
    registry.addViewController("/user/profile").setViewName("user/profile");

    // 钱包相关
    registry.addViewController("/user/wallet").setViewName("user/wallet");
    registry.addViewController("/user/wallet/recharge").setViewName("user/wallet-recharge");
    registry.addViewController("/user/wallet/payment-history").setViewName("user/wallet-payment-history");
    registry.addViewController("/user/wallet/recharge-history").setViewName("user/wallet-recharge-history");

    // 错误页面
    registry.addViewController("/error").setViewName("error/error");
    registry.addViewController("/error/403").setViewName("error/403");
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 配置静态资源访问
    registry.addResourceHandler("/static/**")
        .addResourceLocations("classpath:/static/")
        .setCachePeriod(3600);

    // 配置上传文件的访问路径
    String uploadPath = "file:" + fileStorageProperties.getUploadDir() + "/";

    // 配置所有上传文件的访问
    registry.addResourceHandler("/uploads/**")
        .addResourceLocations(uploadPath)
        .setCachePeriod(3600)
        .resourceChain(true)
        .addResolver(new PathResourceResolver());

    // 配置CSS、JS等静态资源
    registry.addResourceHandler("/css/**")
        .addResourceLocations("classpath:/static/css/")
        .setCachePeriod(3600);

    registry.addResourceHandler("/js/**")
        .addResourceLocations("classpath:/static/js/")
        .setCachePeriod(3600);

    registry.addResourceHandler("/images/**")
        .addResourceLocations("classpath:/static/images/")
        .setCachePeriod(3600);
  }
}