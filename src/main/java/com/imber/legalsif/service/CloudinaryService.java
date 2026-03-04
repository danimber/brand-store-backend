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
        "api_key",    apiKey,
        "api_secret", apiSecret
    ));
    this.productRepository = productRepository;
    this.productImageRepository = productImageRepository;
  }

  public String uploadImage(MultipartFile file) throws IOException {
    Map result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
    return (String) result.get("secure_url");
  }

  public Product createProduct(String name, String category, Double price,
                               String description, Boolean soldOut, Boolean isNew,
                               MultipartFile mainImage, List<MultipartFile> images) throws IOException {
    Product product = new Product();
    product.setName(name);
    product.setCategory(category);
    product.setPrice(price);
    product.setDescription(description);
    product.setSoldOut(soldOut);
    product.setIsNew(isNew);
    product.setImage(uploadImage(mainImage));
    productRepository.save(product);

    if (images != null) {
      for (int i = 0; i < images.size(); i++) {
        ProductImage pi = new ProductImage();
        pi.setProduct(product);
        String url = uploadImage(images.get(i));
        pi.setUrl(url);
        pi.setFileName(url);
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

    if (name != null)        product.setName(name);
    if (category != null)    product.setCategory(category);
    if (price != null)       product.setPrice(price);
    if (description != null) product.setDescription(description);
    if (soldOut != null)     product.setSoldOut(soldOut);
    if (isNew != null)       product.setIsNew(isNew);

    if (mainImage != null && !mainImage.isEmpty()) {
      product.setImage(uploadImage(mainImage));
    }

    productRepository.save(product);

    if (images != null) {
      for (int i = 0; i < images.size(); i++) {
        ProductImage pi = new ProductImage();
        pi.setProduct(product);
        String url = uploadImage(images.get(i));
        pi.setUrl(url);
        pi.setFileName(url);
        pi.setSortOrder(i);
        productImageRepository.save(pi);
      }
    }

    return product;
  }

  public void deleteImage(Long imageId) {
    ProductImage image = productImageRepository.findById(imageId)
        .orElseThrow(() -> new RuntimeException("Image not found"));
    productImageRepository.delete(image);
  }
}