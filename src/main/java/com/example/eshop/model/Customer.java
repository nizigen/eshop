package com.example.eshop.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.OneToMany;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true) // Include superclass fields in equals/hashCode
@DiscriminatorValue("ROLE_CUSTOMER")
public class Customer extends User {

  // @NotBlank(message = "Bank account number is mandatory")
  // @Pattern(regexp = "^[0-9]{16}$", message = "Bank account number must be 16
  // digits")
  // private String bankAccountNumber;

  @OneToMany(mappedBy = "customer")
  @JsonIgnore
  private List<Order> orders;

  // Constructor matching the superclass constructor
  public Customer(String username, String name, String password, String phone, String email, String city,
      Gender gender) {
    super(username, name, password, phone, email, city, gender, Role.ROLE_CUSTOMER);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Customer customer = (Customer) o;
    return getId() != null && getId().equals(customer.getId());
  }

  @Override
  public int hashCode() {
    return getId() != null ? getId().hashCode() : 0;
  }
}