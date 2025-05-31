package com.example.eshop.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "points")
@Data
@NoArgsConstructor
public class Points {

  @Id
  private Long userId;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "user_id")
  @JsonIgnore
  private User user;

  @Column(name = "points_balance", nullable = false)
  private Integer balance = 0;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Points points = (Points) o;
    return userId != null && userId.equals(points.userId);
  }

  @Override
  public int hashCode() {
    return userId != null ? userId.hashCode() : 0;
  }
}