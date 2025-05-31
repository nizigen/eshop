package com.example.eshop.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PageResponse<T> {
  private List<T> content;
  private int pageNumber;
  private int pageSize;
  private long totalElements;
  private int totalPages;
  private boolean first;
  private boolean last;
  private boolean empty;

  public PageResponse(Page<T> page) {
    this.content = page.getContent();
    this.pageNumber = page.getNumber();
    this.pageSize = page.getSize();
    this.totalElements = page.getTotalElements();
    this.totalPages = page.getTotalPages();
    this.first = page.isFirst();
    this.last = page.isLast();
    this.empty = page.isEmpty();
  }

  public static <T> PageResponse<T> of(Page<T> page) {
    return new PageResponse<>(page);
  }
}