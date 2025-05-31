package com.example.eshop.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
  private Long id;
  private String name;
  private String description;
  private String iconUrl;
  private Integer displayOrder;
  private Boolean enabled;
  private Long productCount;
  private Long parentId;
  private String parentName;
  private List<CategoryDTO> children;
}