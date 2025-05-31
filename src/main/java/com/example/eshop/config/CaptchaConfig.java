package com.example.eshop.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class CaptchaConfig {

  @Bean
  public DefaultKaptcha captchaProducer() {
    DefaultKaptcha captcha = new DefaultKaptcha();
    Properties properties = new Properties();

    // 验证码图片宽度
    properties.setProperty("kaptcha.image.width", "150");
    // 验证码图片高度
    properties.setProperty("kaptcha.image.height", "50");
    // 验证码字体大小
    properties.setProperty("kaptcha.textproducer.font.size", "38");
    // 验证码字体颜色
    properties.setProperty("kaptcha.textproducer.font.color", "0,102,204");
    // 验证码字体
    properties.setProperty("kaptcha.textproducer.font.names", "Arial,Courier,宋体");
    // 验证码字符长度
    properties.setProperty("kaptcha.textproducer.char.length", "4");
    // 验证码字符间距
    properties.setProperty("kaptcha.textproducer.char.space", "6");
    // 验证码背景颜色渐变开始
    properties.setProperty("kaptcha.background.clear.from", "white");
    // 验证码背景颜色渐变结束
    properties.setProperty("kaptcha.background.clear.to", "white");
    // 验证码噪点颜色
    properties.setProperty("kaptcha.noise.color", "180,180,180");
    // 验证码边框颜色
    properties.setProperty("kaptcha.border.color", "220,220,220");
    // 验证码边框厚度
    properties.setProperty("kaptcha.border.thickness", "2");

    Config config = new Config(properties);
    captcha.setConfig(config);

    return captcha;
  }
}