package com.example.Secondhand.repository;

import com.example.Secondhand.model.HainanProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HainanProductRepository extends JpaRepository<HainanProduct, Long>, JpaSpecificationExecutor<HainanProduct> {
    List<HainanProduct> findByCategory(String category);
} 