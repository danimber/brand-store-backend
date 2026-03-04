package com.imber.legalsif.controller;


import com.imber.legalsif.model.Product;
import com.imber.legalsif.model.ProductImage;
import com.imber.legalsif.repository.ProductImageRepository;
import com.imber.legalsif.repository.ProductRepository;
import com.imber.legalsif.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "https://a1c7-45-130-247-192.ngrok-free.app")
@AllArgsConstructor
public class ProductController {

  private ProductService productService;
  private ProductRepository productRepository;
  private ProductImageRepository productImageRepository;

  // GET /api/products
  // GET /api/products?category=T-SHIRTS
  @GetMapping
  public List<Product> getAll(@RequestParam(required = false) String category) {
    return productService.getAll(category);
  }

  // GET /api/products/1
  @GetMapping("/{id}")
  public Product getById(@PathVariable Long id) {
    return productService.getById(id);
  }

  @PostMapping
  public ResponseEntity<?> createProduct(
      @RequestParam String name,
      @RequestParam String category,
      @RequestParam Double price,
      @RequestParam String description,
      @RequestParam(defaultValue = "false") Boolean soldOut,
      @RequestParam(defaultValue = "false") Boolean isNew,
      @RequestParam("mainImage") MultipartFile mainImage,
      @RequestParam(value = "images", required = false) List<MultipartFile> images
  ) throws IOException {

    // Создать папку
    Path uploadDir = Paths.get("uploads");
    if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);

    // Сохранить главную картинку
    String mainExt = mainImage.getOriginalFilename().substring(mainImage.getOriginalFilename().lastIndexOf("."));
    String mainFileName = UUID.randomUUID() + mainExt;
    Files.copy(mainImage.getInputStream(), uploadDir.resolve(mainFileName), StandardCopyOption.REPLACE_EXISTING);

    // Создать товар
    Product product = new Product();
    product.setName(name);
    product.setCategory(category);
    product.setPrice(price);
    product.setDescription(description);
    product.setSoldOut(soldOut);
    product.setIsNew(isNew);
    product.setImage("/uploads/" + mainFileName);
    productRepository.save(product);

    // Сохранить доп картинки
    if (images != null) {
      for (int i = 0; i < images.size(); i++) {
        MultipartFile img = images.get(i);
        String ext = img.getOriginalFilename().substring(img.getOriginalFilename().lastIndexOf("."));
        String fileName = UUID.randomUUID() + ext;
        Files.copy(img.getInputStream(), uploadDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

        ProductImage productImage = new ProductImage();
        productImage.setProduct(product);
        productImage.setUrl("/uploads/" + fileName);
        productImage.setFileName(fileName);
        productImage.setSortOrder(i);
        productImageRepository.save(productImage);
      }
    }

    return ResponseEntity.ok(product);
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> updateProduct(
      @PathVariable Long id,
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) Double price,
      @RequestParam(required = false) String description,
      @RequestParam(required = false) Boolean soldOut,
      @RequestParam(required = false) Boolean isNew,
      @RequestParam(required = false) MultipartFile mainImage,
      @RequestParam(required = false) List<MultipartFile> images
  ) throws IOException {

    Product product = productRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Product not found"));

    if (name != null)        product.setName(name);
    if (category != null)    product.setCategory(category);
    if (price != null)       product.setPrice(price);
    if (description != null) product.setDescription(description);
    if (soldOut != null)     product.setSoldOut(soldOut);
    if (isNew != null)       product.setIsNew(isNew);

    // Новая главная картинка
    if (mainImage != null && !mainImage.isEmpty()) {
      Path uploadDir = Paths.get("uploads");
      if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);
      String ext = mainImage.getOriginalFilename().substring(mainImage.getOriginalFilename().lastIndexOf("."));
      String fileName = UUID.randomUUID() + ext;
      Files.copy(mainImage.getInputStream(), uploadDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
      product.setImage("/uploads/" + fileName);
    }

    productRepository.save(product);

    // Доп картинки
    if (images != null) {
      Path uploadDir = Paths.get("uploads");
      if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);
      for (int i = 0; i < images.size(); i++) {
        MultipartFile img = images.get(i);
        String ext = img.getOriginalFilename().substring(img.getOriginalFilename().lastIndexOf("."));
        String fileName = UUID.randomUUID() + ext;
        Files.copy(img.getInputStream(), uploadDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        ProductImage productImage = new ProductImage();
        productImage.setProduct(product);
        productImage.setUrl("/uploads/" + fileName);
        productImage.setFileName(fileName);
        productImage.setSortOrder(i);
        productImageRepository.save(productImage);
      }
    }

    return ResponseEntity.ok(product);
  }

  // Удалить доп картинку
  @DeleteMapping("/image/{imageId}")
  public ResponseEntity<?> deleteImage(@PathVariable Long imageId) throws IOException {
    ProductImage image = productImageRepository.findById(imageId)
        .orElseThrow(() -> new RuntimeException("Image not found"));
    Path filePath = Paths.get("uploads", image.getFileName());
    Files.deleteIfExists(filePath);
    productImageRepository.delete(image);
    return ResponseEntity.ok(java.util.Map.of("deleted", true));
  }
}
