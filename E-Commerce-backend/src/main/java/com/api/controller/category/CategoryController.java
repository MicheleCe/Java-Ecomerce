package com.api.controller.category;

import com.model.Category;
import com.model.LocalUser;
import com.service.CategoryService;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@AuthenticationPrincipal LocalUser user, @RequestBody Category category) {
    	if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }Category createdCategory = categoryService.createCategory(category);
        
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }
    
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }
    
    @CrossOrigin
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<String> deleteCategory(@AuthenticationPrincipal LocalUser user, @PathVariable UUID categoryId) {
    	if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }try {
        	
            categoryService.deleteCategory(categoryId);
            return ResponseEntity.ok("Category deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting category: " + e.getMessage());
        }
    }

}
