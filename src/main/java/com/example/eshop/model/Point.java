package com.example.eshop.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "points")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Point implements Serializable {

  @Id
  @Column(name = "user_id")
  private Long userId;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "points_balance", nullable = false)
  private Integer pointsBalance = 0; // Default value from schema

  @UpdateTimestamp // Automatically managed by Hibernate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // Constructor to link to a User
  public Point(User user) {
    this.user = user;
    this.userId = user.getId();
    this.pointsBalance = 0;
  }
}