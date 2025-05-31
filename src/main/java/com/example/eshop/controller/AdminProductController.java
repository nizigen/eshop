package com.example.eshop.controller;

import com.example.eshop.dto.ApiResponse;
import com.example.eshop.model.Product;
import com.example.eshop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

  @Autowired
  private ProductService productService;

  @GetMapping("/products")
  public String listProducts(Model model,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Product> products = productService.findAllProducts(pageRequest);
    model.addAttribute("products", products);
    return "admin/list-products";
  }

  @GetMapping("/products/pending")
  public String listPendingProducts(Model model,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Product> products = productService.findPendingProducts(pageRequest);
    model.addAttribute("products", products);
    return "admin/pending-products";
  }

  @PostMapping("/products/approve/{id}")
  @ResponseBody
  public ApiResponse approveProduct(@PathVariable Long id) {
    try {
      productService.approveProduct(id);
      return ApiResponse.success("商品审核通过成功");
    } catch (Exception e) {
      return ApiResponse.error(e.getMessage());
    }
  }

  @PostMapping("/products/reject/{id}")
  @ResponseBody
  public ApiResponse rejectProduct(@PathVariable Long id, @RequestBody RejectRequest request) {
    try {
      productService.rejectProduct(id, request.getReason());
      return ApiResponse.success("商品已拒绝");
    } catch (Exception e) {
      return ApiResponse.error(e.getMessage());
    }
  }

  @PostMapping("/products/delete/{id}")
  public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      productService.deleteProduct(id);
      redirectAttributes.addFlashAttribute("successMessage", "商品删除成功");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    }
    return "redirect:/admin/products";
  }
}

class RejectRequest {
  private String reason;

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }
}