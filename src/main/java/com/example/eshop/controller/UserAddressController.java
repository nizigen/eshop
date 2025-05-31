package com.example.eshop.controller;

import com.example.eshop.model.UserAddress;
import com.example.eshop.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/profile/addresses")
public class UserAddressController {

  @Autowired
  private UserAddressService userAddressService;

  @GetMapping("/add")
  public String showAddForm(Model model) {
    model.addAttribute("address", new UserAddress());
    return "profile/address-form";
  }

  @GetMapping("/{id}/edit")
  public String showEditForm(@PathVariable Long id, Model model) {
    UserAddress address = userAddressService.getAddressById(id);
    model.addAttribute("address", address);
    return "profile/address-form";
  }

  @PostMapping("/add")
  public String addAddress(@ModelAttribute UserAddress address, RedirectAttributes redirectAttributes) {
    try {
      userAddressService.addAddress(address);
      redirectAttributes.addFlashAttribute("success", "地址添加成功");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "地址添加失败：" + e.getMessage());
    }
    return "redirect:/profile#address";
  }

  @PostMapping("/{id}/update")
  public String updateAddress(@PathVariable Long id, @ModelAttribute UserAddress address,
      RedirectAttributes redirectAttributes) {
    try {
      address.setId(id);
      userAddressService.updateAddress(address);
      redirectAttributes.addFlashAttribute("success", "地址更新成功");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("error", "地址更新失败：" + e.getMessage());
    }
    return "redirect:/profile#address";
  }

  @PostMapping("/{id}/delete")
  @ResponseBody
  public Map<String, Object> deleteAddress(@PathVariable Long id) {
    try {
      userAddressService.deleteAddress(id);
      return Map.of("success", true);
    } catch (Exception e) {
      return Map.of("success", false, "message", e.getMessage());
    }
  }

  @PostMapping("/{id}/set-default")
  @ResponseBody
  public Map<String, Object> setDefaultAddress(@PathVariable Long id) {
    try {
      userAddressService.setDefaultAddress(id);
      return Map.of("success", true);
    } catch (Exception e) {
      return Map.of("success", false, "message", e.getMessage());
    }
  }

  @GetMapping("/list")
  @ResponseBody
  public Map<String, Object> listAddresses() {
    try {
      return Map.of("success", true, "addresses", userAddressService.getCurrentUserAddresses());
    } catch (Exception e) {
      return Map.of("success", false, "message", e.getMessage());
    }
  }

  @GetMapping("/{id}")
  @ResponseBody
  public Map<String, Object> getAddress(@PathVariable Long id) {
    try {
      UserAddress address = userAddressService.getAddressById(id);
      return Map.of("success", true, "address", address);
    } catch (Exception e) {
      return Map.of("success", false, "message", e.getMessage());
    }
  }
}