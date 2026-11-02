package com.modernricemill.service;

import com.modernricemill.dto.ProductRequest;
import com.modernricemill.entity.Product;
import com.modernricemill.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    @Transactional
    public Product create(ProductRequest req) {
        Product p = new Product();
        apply(p, req);
        return productRepository.save(p);
    }

    @Transactional
    public Product update(Long id, ProductRequest req) {
        Product p = getById(id);
        apply(p, req);
        return productRepository.save(p);
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        productRepository.deleteById(id);
    }

    private void apply(Product p, ProductRequest req) {
        p.setName(req.getName().trim());
        p.setPrice(req.getPrice());
        p.setImageUrl(req.getImageUrl() != null ? req.getImageUrl().trim() : null);
        p.setDescription(req.getDescription() != null ? req.getDescription().trim() : null);
        p.setStockQuantity(req.getStockQuantity() != null ? req.getStockQuantity() : 0);
        p.setOriginalPrice(req.getOriginalPrice());
        p.setCategory(req.getCategory() != null ? req.getCategory().trim() : null);
    }
}
