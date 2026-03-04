package com.imber.legalsif.controller;

import com.imber.legalsif.model.Product;
import com.imber.legalsif.model.ProductImage;
import com.imber.legalsif.repository.ProductImageRepository;
import com.imber.legalsif.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class ImageUploadController {

  private final ProductRepository productRepository;
  private final ProductImageRepository productImageRepository;

  @PostMapping("/product/{productId}")
  public ResponseEntity<?> uploadImage(
      @PathVariable Long productId,
      @RequestParam("file") MultipartFile file,
      @RequestParam(defaultValue = "0") Integer sortOrder
  ) throws IOException {

    // Найти товар
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new RuntimeException("Product not found"));

    // Создать папку uploads если нет
    Path uploadDir = Paths.get("uploads");
    if (!Files.exists(uploadDir)) {
      Files.createDirectories(uploadDir);
    }

    // Уникальное имя файла
    String ext = file.getOriginalFilename()
        .substring(file.getOriginalFilename().lastIndexOf("."));
    String fileName = UUID.randomUUID() + ext;

    // Сохранить файл
    Path filePath = uploadDir.resolve(fileName);
    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

    // Сохранить в БД
    ProductImage image = new ProductImage();
    image.setProduct(product);
    image.setUrl("/uploads/" + fileName);
    image.setFileName(fileName);
    image.setSortOrder(sortOrder);
    productImageRepository.save(image);

    return ResponseEntity.ok().body(java.util.Map.of(
        "url", "/uploads/" + fileName,
        "id", image.getId()
    ));
  }

  @DeleteMapping("/image/{imageId}")
  public ResponseEntity<?> deleteImage(@PathVariable Long imageId) throws IOException {
    ProductImage image = productImageRepository.findById(imageId)
        .orElseThrow(() -> new RuntimeException("Image not found"));

    // Удалить файл
    Path filePath = Paths.get("uploads", image.getFileName());
    Files.deleteIfExists(filePath);

    // Удалить из БД
    productImageRepository.delete(image);

    return ResponseEntity.ok().body(java.util.Map.of("deleted", true));
  }

  @PostMapping("/{productId}/main-image")
  public ResponseEntity<?> uploadMainImage(
      @PathVariable Long productId,
      @RequestParam("file") MultipartFile file
  ) throws IOException {

    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new RuntimeException("Product not found"));

    Path uploadDir = Paths.get("uploads");
    if (!Files.exists(uploadDir)) {
      Files.createDirectories(uploadDir);
    }

    String ext = file.getOriginalFilename()
        .substring(file.getOriginalFilename().lastIndexOf("."));
    String fileName = UUID.randomUUID() + ext;

    Path filePath = uploadDir.resolve(fileName);
    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

    product.setImage("/uploads/" + fileName);
    productRepository.save(product);

    return ResponseEntity.ok().body(java.util.Map.of(
        "url", "/uploads/" + fileName
    ));
  }
}