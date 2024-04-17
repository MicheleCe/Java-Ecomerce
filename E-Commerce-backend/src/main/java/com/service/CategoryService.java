package com.service;

import com.model.Category;
import com.repository.CategoryDAO;

import jakarta.transaction.Transactional;

public class CategoryService {

	private CategoryDAO categoryDAO;

	public CategoryService(CategoryDAO categoryDAO) {
		super();
		this.categoryDAO = categoryDAO;
	}

	@Transactional
	public Category createCategory(Category category) {
		Category newCategory = new Category();
		newCategory.setName(category.getName());
		return categoryDAO.save(newCategory);
	}

}
