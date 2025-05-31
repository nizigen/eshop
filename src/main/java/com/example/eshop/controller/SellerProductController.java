package com.example.eshop.controller;

import com.example.eshop.exception.ResourceNotFoundException;
import com.example.eshop.exception.UnauthorizedActionException;
import com.example.eshop.model.Category;
import com.example.eshop.model.Product;
import com.example.eshop.model.Seller;
import com.example.eshop.model.User;
import com.example.eshop.service.CategoryService;
import com.example.eshop.service.ProductService;
import com.example.eshop.service.UserService;
import jakarta.validation.Valid; // For validation
import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // For validation results
import org.springframework.web.bind.annotation.*; // For PostMapping, RequestParam
import org.springframework.web.multipart.MultipartFile; // For file uploads
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/seller/products")
@PreAuthorize("hasRole('ROLE_SELLER')") // Add ROLE_ prefix
public class SellerProductController {

  // Logger moved to top
  private static final Logger log = LoggerFactory.getLogger(SellerProductController.class);

  private final ProductService productService;
  private final UserService userService;
  private final CategoryService categoryService;

  @Autowired
  public SellerProductController(ProductService productService, UserService userService,
      CategoryService categoryService) {
    this.productService = productService;
    this.userService = userService;
    this.categoryService = categoryService;
  }

  // Utility method to get the current seller user
  private Seller getCurrentSeller(Authentication authentication) {
    String email = authentication.getName();
    User user = userService.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    if (!(user instanceof Seller)) {
      throw new UnauthorizedActionException("User is not a seller: " + email);
    }
    return (Seller) user;
  }

  // Common method to add categories to the model for forms
  private void addCategoriesToModel(Model model) {
    List<Category> categories = categoryService.findAllCategories();
    model.addAttribute("categories", categories);
  }

  // List products owned by the current seller
  @GetMapping
  public String listSellerProducts(Model model, Authentication authentication) {
    Seller seller = getCurrentSeller(authentication);
    List<Product> products = productService.findProductsBySeller(seller);
    model.addAttribute("products", products);
    return "seller/list-products"; // Thymeleaf template path
  }

  // Show form for adding a new product
  @GetMapping("/new")
  public String showCreateProductForm(Model model, Authentication authentication) {
    Product product = new Product();
    product.setSeller(getCurrentSeller(authentication));
    model.addAttribute("product", product);
    addCategoriesToModel(model);
    return "seller/form-product";
  }

  // Show form for editing an existing product
  @GetMapping("/edit/{id}")
  public String showEditProductForm(@PathVariable Long id, Model model, Authentication authentication,
      RedirectAttributes redirectAttributes) {
    Seller seller = getCurrentSeller(authentication);
    try {
      // Find product ensuring it belongs to the current seller
      Product product = productService.findProductByIdAndSeller(id, seller)
          .orElseThrow(() -> new ResourceNotFoundException("Product not found or not owned by seller: " + id));
      model.addAttribute("product", product);
      addCategoriesToModel(model); // Add categories for dropdown
      return "seller/form-product";
    } catch (ResourceNotFoundException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/seller/products";
    }
  }

  // Handle new product form submission
  @PostMapping("/save")
  public String saveProduct(@ModelAttribute("product") Product product,
      BindingResult bindingResult,
      @RequestParam("newImageFiles") List<MultipartFile> imageFiles,
      Authentication authentication,
      RedirectAttributes redirectAttributes,
      Model model) {

    Seller seller = getCurrentSeller(authentication);
    product.setSeller(seller);

    // 手动验证
    if (product.getName() == null || product.getName().trim().isEmpty()) {
      bindingResult.rejectValue("name", "error.product", "Product name cannot be blank");
    }
    if (product.getCategory() == null) {
      bindingResult.rejectValue("category", "error.product", "Category cannot be null");
    }
    if (product.getOriginalPrice() == null) {
      bindingResult.rejectValue("originalPrice", "error.product", "Original price cannot be null");
    }
    if (product.getItemCondition() == null) {
      bindingResult.rejectValue("itemCondition", "error.product", "Item condition cannot be null");
    }
    if (product.getQuantity() == null) {
      bindingResult.rejectValue("quantity", "error.product", "Quantity cannot be null");
    }

    if (bindingResult.hasErrors()) {
      log.warn("Validation errors while creating product: {}", bindingResult.getAllErrors());
      addCategoriesToModel(model);
      model.addAttribute("product", product);
      return "seller/form-product";
    }

    try {
      productService.createProduct(product, imageFiles, seller);
      redirectAttributes.addFlashAttribute("success", "商品发布成功！等待管理员审核。");
      return "redirect:/seller/products";
    } catch (Exception e) {
      log.error("Error creating product: {}", e.getMessage(), e);
      addCategoriesToModel(model);
      model.addAttribute("product", product);
      model.addAttribute("error", "商品发布失败：" + e.getMessage());
      return "seller/form-product";
    }
  }

  // Handle product update form submission
  @PostMapping("/update/{id}")
  public String updateProduct(@PathVariable Long id,
      @ModelAttribute("product") Product productDetails,
      BindingResult bindingResult,
      @RequestParam(name = "newImageFiles", required = false) List<MultipartFile> newImageFiles,
      @RequestParam(name = "imagesToDelete", required = false) List<Long> imagesToDelete,
      @RequestParam(name = "primaryImageId", required = false) Long primaryImageId,
      Authentication authentication,
      RedirectAttributes redirectAttributes,
      Model model) {

    Seller seller = getCurrentSeller(authentication);
    productDetails.setSeller(seller);

    if (bindingResult.hasErrors()) {
      log.warn("Validation errors while updating product {}: {}", id, bindingResult.getAllErrors());
      addCategoriesToModel(model);
      productDetails.setImages(productService.findProductById(id).map(Product::getImages).orElse(null));
      model.addAttribute("product", productDetails);
      return "seller/form-product";
    }

    try {
      productService.updateProduct(id, productDetails, newImageFiles, imagesToDelete, primaryImageId, seller);
      redirectAttributes.addFlashAttribute("success", "商品更新成功！");
      return "redirect:/seller/products";
    } catch (ResourceNotFoundException | UnauthorizedActionException e) {
      log.warn("Error updating product {}: {}", id, e.getMessage());
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/seller/products";
    } catch (Exception e) {
      log.error("Error updating product {}: {}", id, e.getMessage(), e);
      addCategoriesToModel(model);
      Product currentProductState = productService.findProductById(id).orElse(productDetails);
      currentProductState.setCategory(
          productDetails.getCategory() != null ? productDetails.getCategory() : currentProductState.getCategory());
      model.addAttribute("product", currentProductState);
      model.addAttribute("error", "商品更新失败：" + e.getMessage());
      return "seller/form-product";
    }
  }

  // Handle request to unlist a product
  @PostMapping("/unlist/{id}")
  public String unlistProduct(@PathVariable Long id,
      Authentication authentication,
      RedirectAttributes redirectAttributes) {
    Seller seller = getCurrentSeller(authentication);
    try {
      productService.unlistProduct(id, seller);
      redirectAttributes.addFlashAttribute("success", "商品已下架。");
    } catch (ResourceNotFoundException | UnauthorizedActionException e) {
      log.warn("Error unlisting product {}: {}", id, e.getMessage());
      redirectAttributes.addFlashAttribute("error", e.getMessage());
    } catch (Exception e) {
      log.error("Unexpected error unlisting product {}: {}", id, e.getMessage(), e);
      redirectAttributes.addFlashAttribute("error", "下架商品时发生错误。");
    }
    return "redirect:/seller/products";
  }

  // Handle request to relist a product
  @PostMapping("/relist/{id}")
  public String relistProduct(@PathVariable Long id,
      Authentication authentication,
      RedirectAttributes redirectAttributes) {
    Seller seller = getCurrentSeller(authentication);
    try {
      productService.relistProduct(id, seller);
      redirectAttributes.addFlashAttribute("successMessage", "商品已提交重新上架申请，等待审核。");
    } catch (ResourceNotFoundException | UnauthorizedActionException e) {
      log.warn("Error re-listing product {}: {}", id, e.getMessage());
      redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    } catch (Exception e) {
      log.error("Unexpected error re-listing product {}: {}", id, e.getMessage(), e);
      redirectAttributes.addFlashAttribute("errorMessage", "重新上架商品时发生错误。");
    }
    return "redirect:/seller/products";
  }

}