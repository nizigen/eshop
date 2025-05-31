package com.example.eshop.dto;

import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
public class PageRequest {
  private int page = 0;
  private int size = 10;
  private String sortBy = "id";
  private Sort.Direction direction = Sort.Direction.DESC;

  public org.springframework.data.domain.PageRequest toPageRequest() {
    return org.springframework.data.domain.PageRequest.of(
        page,
        size,
        direction,
        sortBy);
  }

  public org.springframework.data.domain.PageRequest toPageRequest(String defaultSortBy) {
    return org.springframework.data.domain.PageRequest.of(
        page,
        size,
        direction,
        defaultSortBy);
  }

  public org.springframework.data.domain.PageRequest toPageRequest(String defaultSortBy,
      Sort.Direction defaultDirection) {
    return org.springframework.data.domain.PageRequest.of(
        page,
        size,
        direction != null ? direction : defaultDirection,
        sortBy != null ? sortBy : defaultSortBy);
  }
}