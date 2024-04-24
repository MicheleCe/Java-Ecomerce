package com.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.model.Category;
import com.model.Inventory;
import com.model.Product;
import com.repository.CategoryDAO;
import com.repository.InventoryDAO;
import com.repository.ProductDAO;

import jakarta.transaction.Transactional;

/**
 * Service for handling product actions.
 */
@Service
public class ProductService {

    private ProductDAO productDAO;
    private InventoryDAO inventoryDAO;
    private CategoryDAO categoryDAO;
    private ImageDataService imageDataService;
    
    
    
    @Value("${encryption.salt.rounds}")
    private int saltRounds;

    /**
     * Constructor for spring injection.
     * 
     * @param productDAO
     */
    public ProductService(ProductDAO productDAO, InventoryDAO inventoryDAO, CategoryDAO categoryDAO, ImageDataService imageDataService) {
        this.productDAO = productDAO;
        this.inventoryDAO = inventoryDAO;
        this.categoryDAO = categoryDAO;
        this.imageDataService = imageDataService;
    }

    /**
     * Gets all the products available.
     * 
     * @return The list of products.
     */
    public List<Product> getProducts() {
        return productDAO.findAll();
    }

    /**
     * Adds a new product.
     * 
     * @param product The product to add.
     * @return The added product.
     */
    @Transactional
    public Product addProduct(Product product) {

        Product newProduct = new Product();
        newProduct.setName(product.getName());
        newProduct.setShortDescription(product.getShortDescription());
        newProduct.setLongDescription(product.getLongDescription()); 
        newProduct.setStatus(product.getStatus());
        newProduct.setUserId(product.getUserId());
        if (product.getCreatedAt() == null) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
            String formattedDateTime = now.format(formatter);
            newProduct.setCreatedAt(formattedDateTime);
        }
        
        newProduct = productDAO.save(newProduct);

        for (Category category : product.getCategories()) {
            Optional<Category> existingCategoryOptional = categoryDAO.findById(category.getId());
            if (existingCategoryOptional.isPresent()) {
                Category existingCategory = existingCategoryOptional.get();
                existingCategory.getProducts().add(newProduct);
                newProduct.getCategories().add(existingCategory);
            } else {
                System.out.println("Category not found: " + category.getId());
            }
        }

        List<Inventory> newInventories = new ArrayList<>();
        for (Inventory inventory : product.getInventory()) {
        	if (inventory.getQuantity() > 0) {
        		Inventory newInventory = new Inventory();
        		newInventory.setProduct(newProduct);
        		newInventory.setQuantity(inventory.getQuantity());
        		newInventory.setColor(inventory.getColor());
        		newInventory.setModel(inventory.getModel());
        		newInventory.setPrice(inventory.getPrice());
        		newInventories.add(newInventory);
        	}
        }

        newInventories = inventoryDAO.saveAll(newInventories);
        
        newProduct.setInventory(newInventories);

        return productDAO.save(newProduct);
    }



    /**
     * Updates an existing product.
     * 
     * @param productId     The ID of the product to update.
     * @param updatedProduct The updated product data.
     * @return The updated product, or null if the product does not exist.
     */
    @Transactional
    public Product updateProduct(UUID productId, Product updatedProduct) {
        Optional<Product> existingProductOptional = productDAO.findById(productId);
        if (existingProductOptional.isPresent()) {
            Product existingProduct = existingProductOptional.get();
            existingProduct.setName(updatedProduct.getName());
            existingProduct.setShortDescription(updatedProduct.getShortDescription());
            existingProduct.setLongDescription(updatedProduct.getLongDescription());

            existingProduct.setCategories(updatedProduct.getCategories());
            
            if (updatedProduct.getLastUpdate() == null) {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
                String formattedDateTime = now.format(formatter);
                existingProduct.setLastUpdate(formattedDateTime);
            }

            if (updatedProduct.getInventory() != null) {
                List<Inventory> existingInventories = existingProduct.getInventory();
                for (Inventory updatedInventory : updatedProduct.getInventory()) {
                    boolean found = false;
                    for (Inventory existingInventory : existingInventories) {
                        if (existingInventory.getId().equals(updatedInventory.getId())) {
                            if (updatedInventory.getQuantity() == 0) {
                                existingInventories.remove(existingInventory);
                                inventoryDAO.delete(existingInventory);
                            } else {
                                existingInventory.setQuantity(updatedInventory.getQuantity());
                                existingInventory.setColor(updatedInventory.getColor());
                                existingInventory.setModel(updatedInventory.getModel());
                                existingInventory.setPrice(updatedInventory.getPrice());
                            }
                            found = true;
                            break;
                        }
                    }
                    if (!found && updatedInventory.getQuantity() != 0) {
                        Inventory newInventory = new Inventory();
                        newInventory.setProduct(existingProduct);
                        newInventory.setQuantity(updatedInventory.getQuantity());
                        newInventory.setColor(updatedInventory.getColor());
                        newInventory.setModel(updatedInventory.getModel());
                        newInventory.setPrice(updatedInventory.getPrice());
                        existingInventories.add(newInventory);
                        inventoryDAO.save(newInventory); 
                    }
                }
            }
            return productDAO.save(existingProduct);
        } else {
            return null;
        }
    }

    
    @Transactional
    public void addProductToCategory(UUID categoryId, Product product) {
    	
        Optional<Category> existingCategoryOptional = categoryDAO.findById(categoryId);
        if (existingCategoryOptional == null) {
            throw new IllegalArgumentException("Category not found");
        }
        
        Category categoryData = existingCategoryOptional.get();
        
        categoryData.getProducts().add(product);
        product.getCategories().add(categoryData);
        
        categoryDAO.save(categoryData);
        productDAO.save(product);
    }
    
    
    
    @Transactional
    public void deleteProduct(UUID productId) {
        Optional<Product> existingProductOptional = productDAO.findById(productId);
        if (existingProductOptional.isPresent()) {
        	imageDataService.deleteProductFolder(productId);
            Product existingProduct = existingProductOptional.get();
            productDAO.delete(existingProduct);
        }
    }
    
    public List<Product> findProductsByUserId(UUID userId) {
        return productDAO.findByUserId(userId);
    }
}
