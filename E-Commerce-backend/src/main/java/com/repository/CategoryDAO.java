package com.repository;


import java.util.UUID;

import org.springframework.data.repository.ListCrudRepository;

import com.model.Category;


public interface CategoryDAO extends ListCrudRepository<Category, UUID> {
	
}
