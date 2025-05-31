package com.example.eshop.controller;

import com.example.eshop.dto.CouponRequest;
import com.example.eshop.dto.CouponResponse;
import com.example.eshop.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/coupons")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCouponController {

  private final CouponService couponService;

  // 列表页
  @GetMapping
  public String listCoupons(Model model) {
    model.addAttribute("coupons", couponService.getAllCoupons());
    return "admin/list-coupon";
  }

  // 新建表单页
  @GetMapping("/new")
  public String showCreateForm(Model model) {
    model.addAttribute("couponRequest", new CouponRequest());
    return "admin/coupon-form";
  }

  // 新建提交
  @PostMapping
  public String createCoupon(@ModelAttribute("couponRequest") @Valid CouponRequest couponRequest,
      RedirectAttributes redirectAttributes) {
    couponService.createCoupon(couponRequest);
    redirectAttributes.addFlashAttribute("successMessage", "优惠券创建成功！");
    return "redirect:/admin/coupons";
  }

  // 编辑表单页
  @GetMapping("/{id}/edit")
  @ResponseBody
  public CouponResponse showEditForm(@PathVariable Long id) {
    return couponService.getCoupon(id);
  }

  // 编辑提交
  @PostMapping("/{id}/edit")
  public String updateCoupon(@PathVariable Long id, @ModelAttribute("couponRequest") @Valid CouponRequest couponRequest,
      RedirectAttributes redirectAttributes) {
    couponService.updateCoupon(id, couponRequest);
    redirectAttributes.addFlashAttribute("successMessage", "优惠券更新成功！");
    return "redirect:/admin/coupons";
  }

  // 删除
  @PostMapping("/{id}/delete")
  public String deleteCoupon(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    couponService.deleteCoupon(id);
    redirectAttributes.addFlashAttribute("successMessage", "优惠券已删除！");
    return "redirect:/admin/coupons";
  }
}