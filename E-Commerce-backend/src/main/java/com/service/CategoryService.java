package com.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.model.Category;
import com.repository.CategoryDAO;

import jakarta.transaction.Transactional;

@Service
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
	
    @Transactional
    public Category findCategoryByName(String categoryName) {
        return categoryDAO.findByName(categoryName);
    }
    
    @Transactional
    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

}
