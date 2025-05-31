package com.example.eshop.controller;

import com.example.eshop.model.Points;
import com.example.eshop.model.User;
import com.example.eshop.service.PointsService;
import com.example.eshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/points")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class PointController {

  private final PointsService pointsService;
  private final UserService userService;

  @GetMapping
  public String showPoints(Model model, Authentication authentication) {
    User user = userService.findByEmail(authentication.getName())
        .orElseThrow(() -> new RuntimeException("User not found"));

    Points points = pointsService.getPointsByUser(user);
    BigDecimal deductibleAmount = pointsService.calculateDeductibleAmount(points.getBalance());

    model.addAttribute("points", points);
    model.addAttribute("deductibleAmount", deductibleAmount);
    return "user/points";
  }

  @PostMapping("/calculate-deduction")
  @ResponseBody
  public BigDecimal calculateDeduction(@RequestParam int points, Authentication authentication) {
    User user = userService.findByEmail(authentication.getName())
        .orElseThrow(() -> new RuntimeException("User not found"));

    // 验证积分是否足够
    if (!pointsService.hasEnoughPoints(user.getId(), points)) {
      return BigDecimal.ZERO;
    }

    return pointsService.calculateDeductibleAmount(points);
  }

  @PostMapping("/check-available")
  @ResponseBody
  public boolean checkPointsAvailable(@RequestParam int points, Authentication authentication) {
    User user = userService.findByEmail(authentication.getName())
        .orElseThrow(() -> new RuntimeException("User not found"));
    return pointsService.hasEnoughPoints(user.getId(), points);
  }
}