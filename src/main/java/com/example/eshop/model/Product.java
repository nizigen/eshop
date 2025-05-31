package com.example.eshop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.eshop.model.Category;
import com.example.eshop.model.ItemCondition;
import com.example.eshop.model.ProductStatus;
import com.example.eshop.model.Seller;
import com.example.eshop.model.User;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_prd_seller_id", columnList = "seller_id"),
    @Index(name = "idx_prd_name", columnList = "name"),
    @Index(name = "idx_prd_status", columnList = "status"),
    @Index(name = "idx_prd_category_id", columnList = "category_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull(message = "Seller cannot be null")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id", nullable = false)
  @JsonBackReference("seller-products")
  @JsonIgnore
  private Seller seller;

  @NotBlank(message = "Product name cannot be blank")
  @Size(max = 255)
  @Column(nullable = false, length = 255)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  @JsonBackReference("category-products")
  private Category category;

  @Column(columnDefinition = "TEXT")
  private String description;

  @NotNull(message = "Original price cannot be null")
  @PositiveOrZero(message = "Original price must be zero or positive")
  @Column(name = "original_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal originalPrice;

  @NotNull(message = "Discount price cannot be null")
  @PositiveOrZero(message = "Discount price must be zero or positive")
  @Column(name = "discount_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal discountPrice;

  @Size(max = 50)
  @Column(length = 50)
  private String size;

  @NotNull(message = "Item condition cannot be null")
  @Enumerated(EnumType.STRING)
  @Column(name = "item_condition")
  private ItemCondition itemCondition;

  @NotNull(message = "Quantity cannot be null")
  @PositiveOrZero(message = "Quantity must be zero or positive")
  @Column(nullable = false)
  private Integer quantity;

  @NotNull(message = "Status cannot be null")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProductStatus status = ProductStatus.PENDING_APPROVAL; // Default from schema

  @Column(nullable = false)
  private Boolean negotiable = false; // Default from schema

  @Column(name = "sales_count", nullable = false)
  private Integer salesCount = 0; // Default from schema

  @Column(name = "average_rating", precision = 3, scale = 2)
  private BigDecimal averageRating;

  @Column(name = "created_at", updatable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

  @UpdateTimestamp // Automatically managed by Hibernate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  // Relationships
  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference("product-images")
  private Set<ProductImage> images = new HashSet<>();

  @OneToMany(mappedBy = "product")
  @JsonIgnore
  private Set<Review> reviews = new HashSet<>();

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnore
  private List<OrderItem> orderItems = new ArrayList<>();

  // Add convenience methods for managing images if needed
  public void addImage(ProductImage image) {
    images.add(image);
    image.setProduct(this);
  }

  public void removeImage(ProductImage image) {
    images.remove(image);
    image.setProduct(null);
  }

  // Convenience method to get primary image
  public ProductImage getPrimaryImage() {
    if (images == null || images.isEmpty()) {
      return null;
    }
    return images.stream()
        .filter(img -> img.getIsPrimary() != null && img.getIsPrimary())
        .findFirst()
        .orElse(images.stream().findFirst().orElse(null)); // Return first image if no primary is explicitly set
  }

  public BigDecimal getPrice() {
    return discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0
        ? discountPrice
        : originalPrice;
  }

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();

    if (discountPrice == null || discountPrice.compareTo(BigDecimal.ZERO) <= 0) {
      discountPrice = originalPrice;
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();

    if (discountPrice == null || discountPrice.compareTo(BigDecimal.ZERO) <= 0) {
      discountPrice = originalPrice;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Product product = (Product) o;
    return id != null && id.equals(product.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}