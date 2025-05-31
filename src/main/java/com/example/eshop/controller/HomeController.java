package com.example.eshop.controller;

import com.example.eshop.model.Product;
import com.example.eshop.model.Category;
import com.example.eshop.dto.CategoryDTO;
import com.example.eshop.service.ProductService;
import com.example.eshop.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.List;

@Controller
public class HomeController {

  private static final Logger log = LoggerFactory.getLogger(HomeController.class);
  private final ProductService productService;
  private final CategoryService categoryService;

  @Autowired
  public HomeController(ProductService productService, CategoryService categoryService) {
    this.productService = productService;
    this.categoryService = categoryService;
  }

  @GetMapping("/")
  public String home(Model model,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String sortBy,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    log.info("Loading home page with categoryId: {}, keyword: {}, sortBy: {}, page: {}", categoryId, keyword, sortBy,
        page);

    // 获取所有分类
    List<CategoryDTO> categories = categoryService.findAllCategoriesWithProductCount();
    model.addAttribute("categories", categories);
    model.addAttribute("selectedCategoryId", categoryId);

    // 创建排序对象
    Sort sort;
    if (sortBy != null) {
      switch (sortBy) {
        case "price_asc":
          sort = Sort.by(Sort.Direction.ASC, "discountPrice");
          break;
        case "price_desc":
          sort = Sort.by(Sort.Direction.DESC, "discountPrice");
          break;
        case "sales":
          sort = Sort.by(Sort.Direction.DESC, "salesCount");
          break;
        case "rating":
          sort = Sort.by(Sort.Direction.DESC, "averageRating");
          break;
        default:
          sort = Sort.by(Sort.Direction.DESC, "createdAt");
      }
    } else {
      sort = Sort.by(Sort.Direction.DESC, "createdAt");
    }

    // 创建分页请求
    PageRequest pageRequest = PageRequest.of(page, size, sort);

    // 获取商品列表
    Page<Product> products;
    if (keyword != null && !keyword.trim().isEmpty()) {
      products = productService.searchProducts(keyword, pageRequest);
    } else if (categoryId != null) {
      products = productService.getProductsByCategory(categoryId, pageRequest);
    } else {
      products = productService.getActiveProducts(pageRequest);
    }

    model.addAttribute("products", products);
    log.info("Successfully loaded home page data");

    return "home";
  }
}