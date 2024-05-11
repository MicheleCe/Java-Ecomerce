package com.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

import com.model.Category;
import com.model.Product;

public interface ProductDAO extends ListCrudRepository<Product, UUID>{
	
	List<Product> findByUserId(UUID userId);

	@Query("SELECT COUNT(p) > 0 FROM Product p JOIN p.categories c WHERE c.id = :categoryId")
    boolean isCategoryAssociatedWithProduct(UUID categoryId);
}
