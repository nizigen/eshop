package com.example.eshop.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "uploads", indexes = {
    @Index(name = "idx_upload_user_id", columnList = "user_id"),
    @Index(name = "idx_upload_type", columnList = "type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Upload implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, length = 36)
  private String filename; // UUID as filename

  @Column(length = 255)
  private String originalFilename; // Original filename from user

  @Column(nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private UploadType type = UploadType.GENERAL; // GENERAL, ID_CARD, LICENSE

  @Column(length = 100)
  private String mimeType;

  @Column
  private Long fileSize;

  @Column(length = 255)
  private String path; // Relative path in uploads directory

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  public enum UploadType {
    GENERAL, // General uploads like product images
    ID_CARD, // ID card images in idcards/
    LICENSE // Business license images in licenses/
  }

  // Helper method to get full path
  public String getFullPath() {
    if (type == UploadType.ID_CARD) {
      return "idcards/" + filename;
    } else if (type == UploadType.LICENSE) {
      return "licenses/" + filename;
    }
    return filename;
  }
}