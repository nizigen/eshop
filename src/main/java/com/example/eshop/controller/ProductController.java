package com.example.eshop.controller;

import com.example.eshop.model.Product;
import com.example.eshop.model.Seller;
import com.example.eshop.model.Review;
import com.example.eshop.service.ProductService;
import com.example.eshop.service.UserService;
import com.example.eshop.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

  private final ProductService productService;
  private final UserService userService;

  @Autowired
  public ProductController(ProductService productService, UserService userService) {
    this.productService = productService;
    this.userService = userService;
  }

  @GetMapping("/{id}")
  public String getProductDetail(@PathVariable Long id, Model model) {
    Product product = productService.getProductWithImagesAndReviews(id);
    if (!(product.getSeller() instanceof Seller)) {
      throw new ResourceNotFoundException("Product seller not found");
    }
    Seller seller = (Seller) product.getSeller();
    List<Review> reviews = product.getReviews() != null ? new ArrayList<>(product.getReviews()) : new ArrayList<>();
    model.addAttribute("product", product);
    model.addAttribute("reviews", reviews);
    model.addAttribute("sellerProductCount", productService.countProductsBySeller(seller));
    return "product/product-detail";
  }

  @GetMapping
  public String listProducts(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(defaultValue = "default") String sortBy,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      Model model) {

    PageRequest pageRequest = PageRequest.of(page, size, getSort(sortBy));
    Page<Product> products;

    if (keyword != null && !keyword.isEmpty()) {
      products = productService.searchProducts(keyword, pageRequest);
    } else if (categoryId != null) {
      products = productService.getProductsByCategory(categoryId, pageRequest);
    } else {
      products = productService.getActiveProducts(pageRequest);
    }

    model.addAttribute("products", products);
    model.addAttribute("keyword", keyword);
    model.addAttribute("selectedCategoryId", categoryId);
    model.addAttribute("sortBy", sortBy);
    return "home";
  }

  private Sort getSort(String sortBy) {
    return switch (sortBy) {
      case "price_asc" -> Sort.by("discountPrice").ascending();
      case "price_desc" -> Sort.by("discountPrice").descending();
      case "sales" -> Sort.by("salesCount").descending();
      case "rating" -> Sort.by("averageRating").descending();
      default -> Sort.by("createdAt").descending();
    };
  }
}