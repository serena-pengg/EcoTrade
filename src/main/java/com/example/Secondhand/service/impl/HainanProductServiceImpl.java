package com.example.Secondhand.service.impl;

import com.example.Secondhand.model.HainanProduct;
import com.example.Secondhand.repository.HainanProductRepository;
import com.example.Secondhand.service.HainanProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;
import jakarta.persistence.criteria.Predicate;
import jakarta.annotation.PostConstruct;

@Service
public class HainanProductServiceImpl implements HainanProductService {

    @Autowired
    private HainanProductRepository hainanProductRepository;

    @Override
    public List<HainanProduct> getAllProducts() {
        return hainanProductRepository.findAll();
    }

    @Override
    public HainanProduct getProductById(Long id) {
        return hainanProductRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Override
    public HainanProduct createProduct(HainanProduct product) {
        return hainanProductRepository.save(product);
    }

    @Override
    public HainanProduct updateProduct(Long id, HainanProduct product) {
        HainanProduct existingProduct = getProductById(id);
        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setImageUrl(product.getImageUrl());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setRecycleScore(product.getRecycleScore());
        existingProduct.setDurabilityScore(product.getDurabilityScore());
        existingProduct.setCarbonFootprint(product.getCarbonFootprint());
        existingProduct.setEcoScore(product.getEcoScore());
        return hainanProductRepository.save(existingProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        hainanProductRepository.deleteById(id);
    }

    @Override
    public List<HainanProduct> getProductsByCategory(String category) {
        return hainanProductRepository.findByCategory(category);
    }

    @Override
    public Page<HainanProduct> getProductsPaged(Pageable pageable) {
        return hainanProductRepository.findAll(pageable);
    }

    @Override
    public Page<HainanProduct> getFilteredProducts(String category, String ecoScore, String priceRange, Pageable pageable) {
        return hainanProductRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (category != null && !"All".equals(category)) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (ecoScore != null && !"all".equals(ecoScore)) {
                if ("excellent".equals(ecoScore)) {
                    predicates.add(cb.between(root.get("ecoScore"), 4.0, 5.0));
                } else if ("good".equals(ecoScore)) {
                    predicates.add(cb.between(root.get("ecoScore"), 3.0, 3.9));
                } else if ("fair".equals(ecoScore)) {
                    predicates.add(cb.between(root.get("ecoScore"), 2.0, 2.9));
                } else if ("poor".equals(ecoScore)) {
                    predicates.add(cb.between(root.get("ecoScore"), 0.0, 1.9));
                }
            }
            if (priceRange != null && !"all".equals(priceRange)) {
                if (priceRange.contains("+")) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("price"), 2000.0));
                } else {
                    String[] range = priceRange.split("-");
                    predicates.add(cb.between(root.get("price"), Double.valueOf(range[0]), Double.valueOf(range[1])));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }

    public void refreshAllEcoScores() {
        List<HainanProduct> products = hainanProductRepository.findAll();
        for (HainanProduct p : products) {
            // 触发 ecoScore 重新计算
            p.setRecycleScore(p.getRecycleScore());
            p.setDurabilityScore(p.getDurabilityScore());
            p.setCarbonFootprint(p.getCarbonFootprint());
            hainanProductRepository.save(p);
        }
    }

    @PostConstruct
    public void initEcoScores() {
        refreshAllEcoScores();
    }

} 