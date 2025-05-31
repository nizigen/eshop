package com.example.eshop.controller;

import com.example.eshop.dto.ProductSearchDto;
import com.example.eshop.model.Product;
import com.example.eshop.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/products/search")
public class ProductSearchController {

  private static final Logger log = LoggerFactory.getLogger(ProductSearchController.class);
  private final ProductService productService;

  @Autowired
  public ProductSearchController(ProductService productService) {
    this.productService = productService;
  }

  @GetMapping
  public String searchProducts(@ModelAttribute ProductSearchDto searchDto, Model model) {
    log.info("Searching products with parameters: {}", searchDto);

    // 如果关键词为空，设置为空字符串避免SQL错误
    if (searchDto.getKeyword() == null) {
      searchDto.setKeyword("");
    }

    // 执行搜索
    Page<Product> productPage = productService.searchProducts(searchDto);

    // 添加结果到模型
    model.addAttribute("products", productPage.getContent());
    model.addAttribute("currentPage", productPage.getNumber());
    model.addAttribute("totalPages", productPage.getTotalPages());
    model.addAttribute("totalItems", productPage.getTotalElements());
    model.addAttribute("searchDto", searchDto);

    // 添加排序选项
    model.addAttribute("sortOptions", new String[] {
        "default", "price_asc", "price_desc", "sales", "rating"
    });

    return "product/search-results";
  }
}