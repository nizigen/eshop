package com.example.eshop.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

  @RequestMapping("/error")
  public String handleError(HttpServletRequest request, Model model) {
    Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
    String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

    // 如果是 favicon.ico 的请求，直接返回空白页面
    if (requestUri != null && requestUri.endsWith("favicon.ico")) {
      return "blank";
    }

    if (status != null) {
      int statusCode = Integer.parseInt(status.toString());

      model.addAttribute("status", statusCode);
      model.addAttribute("message", message != null ? message : getDefaultMessageForStatus(statusCode));
      model.addAttribute("timestamp", new java.util.Date());

      if (statusCode == HttpStatus.NOT_FOUND.value()) {
        return "error/404";
      } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
        return "error/500";
      }
    }

    return "error/error";
  }

  private String getDefaultMessageForStatus(int status) {
    switch (status) {
      case 400:
        return "Bad Request";
      case 401:
        return "Unauthorized";
      case 403:
        return "Forbidden";
      case 404:
        return "Page Not Found";
      case 405:
        return "Method Not Allowed";
      case 500:
        return "Internal Server Error";
      case 502:
        return "Bad Gateway";
      case 503:
        return "Service Unavailable";
      default:
        return "Something went wrong";
    }
  }
}