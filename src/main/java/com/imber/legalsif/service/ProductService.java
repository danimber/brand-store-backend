package com.imber.legalsif.service;

import com.imber.legalsif.model.Product;
import com.imber.legalsif.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;

  public List<Product> getAll(String category) {
    if (category != null && !category.isEmpty()) {
      return productRepository.findByCategory(category.toUpperCase());
    }
    return productRepository.findAll();
  }

  public Product getById(Long id) {
    return productRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Product not found: " + id));
  }
}
