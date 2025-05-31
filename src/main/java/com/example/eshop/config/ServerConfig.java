package com.example.eshop.config;

import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ServerConfig implements ApplicationListener<WebServerInitializedEvent> {

  @Override
  public void onApplicationEvent(WebServerInitializedEvent event) {
    Environment env = event.getApplicationContext().getEnvironment();
    String protocol = "http";
    if (env.getProperty("server.ssl.key-store") != null) {
      protocol = "https";
    }
    int port = event.getWebServer().getPort();
    String contextPath = env.getProperty("server.servlet.context-path", "");

    System.out.println("\n----------------------------------------------------------");
    System.out.println("\t应用已启动! 访问以下地址:");
    System.out.println("\t本地访问: \t" + protocol + "://localhost:" + port + contextPath);
    System.out.println("\t外部访问: \t" + protocol + "://127.0.0.1:" + port + contextPath);
    System.out.println("----------------------------------------------------------\n");
  }
}