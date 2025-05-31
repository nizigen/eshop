package com.example.eshop.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("ROLE_ADMIN")
public class Admin extends User {

  // Constructor matching the superclass constructor
  public Admin(String username, String name, String password, String phone, String email, String city, Gender gender) {
    super(username, name, password, phone, email, city, gender, Role.ROLE_ADMIN);
    // Explicitly set status to ACTIVE for Admin upon creation
    this.setStatus(UserStatus.ACTIVE);
  }

  // Add admin-specific fields here if needed in the future
}