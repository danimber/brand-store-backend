package com.imber.legalsif.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.imber.legalsif.model.Product;
import com.imber.legalsif.model.ProductImage;
import com.imber.legalsif.repository.ProductImageRepository;
import com.imber.legalsif.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryService {

  private final Cloudinary cloudinary;
  private final ProductRepository productRepository;
  private final ProductImageRepository productImageRepository;

  public CloudinaryService(
      @Value("${CLOUDINARY_CLOUD_NAME}") String cloudName,
      @Value("${CLOUDINARY_API_KEY}") String apiKey,
      @Value("${CLOUDINARY_API_SECRET}") String apiSecret,
      ProductRepository productRepository,
      ProductImageRepository productImageRepository
  ) {
    this.cloudinary = new Cloudinary(ObjectUtils.asMap(
        "cloud_name", cloudName,
        "api_key", apiKey,
        "api_secret", apiSecret
    ));
    this.productRepository = productRepository;
    this.productImageRepository = productImageRepository;
  }

  public Map<String, String> uploadImage(MultipartFile file) throws IOException {
    Map result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
    return Map.of(
        "url", (String) result.get("secure_url"),
        "publicId", (String) result.get("public_id")
    );
  }

  public Product createProduct(String name, String category, Double price,
                               String description, Boolean soldOut, Boolean isNew,
                               MultipartFile mainImage, List<MultipartFile> images) throws IOException {
    Map<String, String> main = uploadImage(mainImage);

    Product product = new Product();
    product.setName(name);
    product.setCategory(category);
    product.setPrice(price);
    product.setDescription(description);
    product.setSoldOut(soldOut);
    product.setIsNew(isNew);
    product.setImage(main.get("url"));
    product.setMainImagePublicId(main.get("publicId"));
    productRepository.save(product);

    if (images != null) {
      for (int i = 0; i < images.size(); i++) {
        Map<String, String> uploaded = uploadImage(images.get(i));
        ProductImage pi = new ProductImage();
        pi.setProduct(product);
        pi.setUrl(uploaded.get("url"));
        pi.setFileName(uploaded.get("url"));
        pi.setPublicId(uploaded.get("publicId"));
        pi.setSortOrder(i);
        productImageRepository.save(pi);
      }
    }

    return product;
  }

  public Product updateProduct(Long id, String name, String category, Double price,
                               String description, Boolean soldOut, Boolean isNew,
                               MultipartFile mainImage, List<MultipartFile> images) throws IOException {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Product not found"));

    if (name != null) product.setName(name);
    if (category != null) product.setCategory(category);
    if (price != null) product.setPrice(price);
    if (description != null) product.setDescription(description);
    if (soldOut != null) product.setSoldOut(soldOut);
    if (isNew != null) product.setIsNew(isNew);

    if (mainImage != null && !mainImage.isEmpty()) {
      if (product.getMainImagePublicId() != null) {
        try {
          cloudinary.uploader().destroy(product.getMainImagePublicId(), ObjectUtils.emptyMap());
        } catch (IOException ignored) {
        }
      }
      Map<String, String> main = uploadImage(mainImage);
      product.setImage(main.get("url"));
      product.setMainImagePublicId(main.get("publicId"));
    }

    productRepository.save(product);

    if (images != null) {
      for (int i = 0; i < images.size(); i++) {
        Map<String, String> uploaded = uploadImage(images.get(i));
        ProductImage pi = new ProductImage();
        pi.setProduct(product);
        pi.setUrl(uploaded.get("url"));
        pi.setFileName(uploaded.get("url"));
        pi.setPublicId(uploaded.get("publicId"));
        pi.setSortOrder(i);
        productImageRepository.save(pi);
      }
    }

    return product;
  }

  public void deleteImage(Long imageId) {
    ProductImage image = productImageRepository.findById(imageId)
        .orElseThrow(() -> new RuntimeException("Image not found"));
    try {
      if (image.getPublicId() != null) {
        cloudinary.uploader().destroy(image.getPublicId(), ObjectUtils.emptyMap());
      }
    } catch (IOException ignored) {
    }
    productImageRepository.delete(image);
  }

  public void deleteProduct(Long id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Product not found"));
    for (ProductImage image : product.getImages()) {
      try {
        if (image.getPublicId() != null) {
          cloudinary.uploader().destroy(image.getPublicId(), ObjectUtils.emptyMap());
        }
      } catch (IOException ignored) {
      }
    }
    try {
      if (product.getMainImagePublicId() != null) {
        cloudinary.uploader().destroy(product.getMainImagePublicId(), ObjectUtils.emptyMap());
      }
    } catch (IOException ignored) {
    }
    productImageRepository.deleteAll(product.getImages());
    productRepository.delete(product);
  }

  public void deleteMainImage(Long id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Product not found"));
    try {
      if (product.getMainImagePublicId() != null) {
        cloudinary.uploader().destroy(product.getMainImagePublicId(), ObjectUtils.emptyMap());
      }
    } catch (IOException ignored) {}
    product.setImage(null);
    product.setMainImagePublicId(null);
    productRepository.save(product);
  }
}