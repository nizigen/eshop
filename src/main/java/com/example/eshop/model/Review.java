package com.example.eshop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", indexes = {
    @Index(name = "idx_review_user_id", columnList = "user_id"),
    @Index(name = "idx_review_product_id", columnList = "product_id"),
    @Index(name = "idx_review_seller_id", columnList = "seller_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Unique constraint on order_item_id ensures one review per item
  @ManyToOne
  @JoinColumn(name = "order_item_id", nullable = false)
  @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
  private OrderItem orderItem;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
  private User user; // Reviewer (Buyer)

  @ManyToOne
  @JoinColumn(name = "product_id", nullable = false)
  @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
  private Product product;

  @ManyToOne
  @JoinColumn(name = "seller_id", nullable = false)
  @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
  private User seller; // Seller being reviewed

  @Column(name = "product_rating")
  @Min(1)
  @Max(5)
  private Integer productRating; // 1-5

  @Column(name = "product_comment")
  private String productComment;

  @Column(name = "seller_service_rating")
  private Integer sellerServiceRating; // 1-5

  @Column(name = "seller_service_comment")
  private String sellerServiceComment;

  @Column(name = "seller_reply")
  private String sellerReply;

  @Column(name = "seller_reply_time")
  private LocalDateTime sellerReplyTime;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Product getProduct() {
    return product;
  }

  public void setProduct(Product product) {
    this.product = product;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Integer getProductRating() {
    return productRating;
  }

  public void setProductRating(Integer productRating) {
    this.productRating = productRating;
  }

  public String getProductComment() {
    return productComment;
  }

  public void setProductComment(String productComment) {
    this.productComment = productComment;
  }

  public Integer getSellerServiceRating() {
    return sellerServiceRating;
  }

  public void setSellerServiceRating(Integer sellerServiceRating) {
    this.sellerServiceRating = sellerServiceRating;
  }

  public String getSellerServiceComment() {
    return sellerServiceComment;
  }

  public void setSellerServiceComment(String sellerServiceComment) {
    this.sellerServiceComment = sellerServiceComment;
  }

  public String getSellerReply() {
    return sellerReply;
  }

  public void setSellerReply(String sellerReply) {
    this.sellerReply = sellerReply;
  }

  public LocalDateTime getSellerReplyTime() {
    return sellerReplyTime;
  }

  public void setSellerReplyTime(LocalDateTime sellerReplyTime) {
    this.sellerReplyTime = sellerReplyTime;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}