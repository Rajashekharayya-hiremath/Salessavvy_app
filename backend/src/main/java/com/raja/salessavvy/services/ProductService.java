package com.raja.salessavvy.services;

import com.raja.salessavvy.entities.*;
import com.raja.salessavvy.repositories.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Product> getProductsByCategory(String categoryName) {

        System.out.println("SEARCHING CATEGORY = " + categoryName);

        if (categoryName != null && !categoryName.isEmpty()) {

            Optional<Category> categoryOpt =
                    categoryRepository.findByCategoryNameIgnoreCase(categoryName);

            System.out.println("FOUND CATEGORY = " + categoryOpt.isPresent());

            if (categoryOpt.isPresent()) {
                Category category = categoryOpt.get();

                System.out.println("CATEGORY ID = " + category.getCategoryId());

                return productRepository.findByCategory_CategoryId(
                        category.getCategoryId());
            } else {
                throw new RuntimeException("Category not found");
            }
        }

        return productRepository.findAll();
    }
    public List<String> getProductImages(Integer productId) {
        List<ProductImage> productImages = productImageRepository.findByProduct_ProductId(productId);
        List<String> imageUrls = new ArrayList<>();
        for (ProductImage image : productImages) {
            imageUrls.add(image.getImageUrl());
        }
        return imageUrls;
    }
}