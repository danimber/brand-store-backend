package com.imber.legalsif.controller;

import com.imber.legalsif.model.Product;
import com.imber.legalsif.service.CloudinaryService;
import com.imber.legalsif.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@AllArgsConstructor
public class ProductController {

  private final ProductService productService;
  private final CloudinaryService cloudinaryService;

  @GetMapping
  public List<Product> getAll(@RequestParam(required = false) String category) {
    return productService.getAll(category);
  }

  @GetMapping("/{id}")
  public Product getById(@PathVariable Long id) {
    return productService.getById(id);
  }

  @PostMapping
  public ResponseEntity<Product> createProduct(
      @RequestParam String name,
      @RequestParam String category,
      @RequestParam Double price,
      @RequestParam String description,
      @RequestParam(defaultValue = "false") Boolean soldOut,
      @RequestParam(defaultValue = "false") Boolean isNew,
      @RequestParam("mainImage") MultipartFile mainImage,
      @RequestParam(value = "images", required = false) List<MultipartFile> images
  ) throws IOException {
    return ResponseEntity.ok(cloudinaryService.createProduct(
        name, category, price, description, soldOut, isNew, mainImage, images
    ));
  }

  @PutMapping("/{id}")
  public ResponseEntity<Product> updateProduct(
      @PathVariable Long id,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) Double price,
      @RequestParam(required = false) String description,
      @RequestParam(required = false) Boolean soldOut,
      @RequestParam(required = false) Boolean isNew,
      @RequestParam(required = false) MultipartFile mainImage,
      @RequestParam(required = false) List<MultipartFile> images,
      @RequestParam(required = false) List<Long> deleteImageIds
  ) throws IOException {
    return ResponseEntity.ok(cloudinaryService.updateProduct(
        id, name, category, price, description, soldOut, isNew, mainImage, images, deleteImageIds
    ));
  }

  @DeleteMapping("/image/{imageId}")
  public ResponseEntity<Map<String, Boolean>> deleteImage(@PathVariable Long imageId) {
    cloudinaryService.deleteImage(imageId);
    return ResponseEntity.ok(Map.of("deleted", true));
  }

  @DeleteMapping("/{id}/main-image")
  public ResponseEntity<Map<String, Boolean>> deleteMainImage(@PathVariable Long id) {
    cloudinaryService.deleteMainImage(id);
    return ResponseEntity.ok(Map.of("deleted", true));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, Boolean>> deleteProduct(@PathVariable Long id) {
    cloudinaryService.deleteProduct(id);
    return ResponseEntity.ok(Map.of("deleted", true));
  }
}