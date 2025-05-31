package com.example.eshop.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

@Controller
@RequestMapping("/auth")
public class CaptchaController {

  private static final Logger log = LoggerFactory.getLogger(CaptchaController.class);
  private static final String CAPTCHA_SESSION_KEY = "captchaCode";

  private final DefaultKaptcha captchaProducer;

  @Autowired
  public CaptchaController(DefaultKaptcha captchaProducer) {
    this.captchaProducer = captchaProducer;
  }

  @GetMapping(value = "/captcha", produces = MediaType.IMAGE_PNG_VALUE)
  public void getCaptcha(HttpServletResponse response, HttpSession session) throws Exception {
    // 清除session中的验证码
    session.removeAttribute(CAPTCHA_SESSION_KEY);

    // 生成验证码文本
    String capText = captchaProducer.createText();
    log.debug("Generated new captcha: {}", capText);

    // 将验证码文本保存到session
    session.setAttribute(CAPTCHA_SESSION_KEY, capText);

    // 生成验证码图片
    BufferedImage bi = captchaProducer.createImage(capText);

    // 设置响应头
    response.setContentType(MediaType.IMAGE_PNG_VALUE);
    response.setHeader("Cache-Control", "no-store, no-cache, no-transform, must-revalidate, max-age=0");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);
    response.setHeader("X-Content-Type-Options", "nosniff");

    // 将图片写入响应流
    ServletOutputStream out = response.getOutputStream();
    ImageIO.write(bi, "png", out);

    // 关闭输出流
    try {
      out.flush();
    } finally {
      out.close();
    }
  }

  /**
   * 验证验证码
   * 
   * @param captchaInput 用户输入的验证码
   * @param session      HttpSession
   * @return 验证码是否正确
   */
  public boolean validateCaptcha(String captchaInput, HttpSession session) {
    String captchaCode = (String) session.getAttribute(CAPTCHA_SESSION_KEY);
    log.debug("Validating captcha - Input: {}, Stored: {}", captchaInput, captchaCode);

    if (captchaCode == null) {
      log.warn("Captcha validation failed: No captcha code in session");
      return false;
    }

    // 验证后立即删除session中的验证码，防止重复使用
    session.removeAttribute(CAPTCHA_SESSION_KEY);

    boolean isValid = captchaCode.equalsIgnoreCase(captchaInput);
    log.debug("Captcha validation result: {}", isValid);
    return isValid;
  }
}