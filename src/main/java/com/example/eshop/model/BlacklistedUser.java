package com.example.eshop.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "blacklisted_users", uniqueConstraints = {
    @UniqueConstraint(name = "uk_blacklist_user_by", columnNames = { "user_id", "blacklisted_by_type",
        "blacklisted_by_id" })
}, indexes = {
    @Index(name = "idx_blacklist_user", columnList = "user_id")
// Index on blacklisted_by_id might be useful too
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistedUser implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // The user who is blacklisted

  @Enumerated(EnumType.STRING)
  @Column(name = "blacklisted_by_type", nullable = false)
  private BlacklistedByType blacklistedByType;

  // ID of the user (seller or admin) who initiated the blacklist
  @Column(name = "blacklisted_by_id", nullable = false)
  private Long blacklistedById;

  // Optional: Define a relationship to the blacklister if needed
  // @ManyToOne(fetch = FetchType.LAZY)
  // @JoinColumn(name = "blacklisted_by_id", insertable=false, updatable=false) //
  // Need to map based on type?
  // private User blacklister;

  @Lob
  private String reason;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}