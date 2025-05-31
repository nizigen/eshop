package com.example.eshop.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  @JsonBackReference
  private Product product;

  @Column(name = "image_url", nullable = false, length = 255)
  private String imageUrl;

  @Column(name = "image_path", nullable = false, length = 1024)
  private String imagePath;

  @Column(name = "is_primary")
  private Boolean isPrimary = false;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  public ProductImage(Product product, String imageUrl, String imagePath, Boolean isPrimary) {
    this.product = product;
    this.imageUrl = imageUrl;
    this.imagePath = imagePath;
    this.isPrimary = isPrimary != null ? isPrimary : false;
  }

  public String getDisplayUrl() {
    if (imageUrl != null && !imageUrl.trim().isEmpty()) {
      return imageUrl;
    }
    return imagePath != null ? "/products/" + imagePath : null;
  }

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}