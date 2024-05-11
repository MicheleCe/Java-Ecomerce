package com.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.model.Category;
import com.repository.CategoryDAO;
import com.repository.ProductDAO;

import jakarta.transaction.Transactional;

@Service
public class CategoryService {

	private CategoryDAO categoryDAO;
	private ProductDAO productDAO;

	public CategoryService(CategoryDAO categoryDAO, ProductDAO productDAO) {
		super();
		this.categoryDAO = categoryDAO;
		this.productDAO = productDAO;
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
    
    @Transactional
    public void deleteCategory(UUID categoryId) {
        Category category = categoryDAO.findById(categoryId).orElse(null);
        if (category != null && !productDAO.isCategoryAssociatedWithProduct(categoryId)) {
            categoryDAO.delete(category);
        } else {
            throw new RuntimeException("Cannot delete category as it is associated with one or more products.");
        }
    }

}
