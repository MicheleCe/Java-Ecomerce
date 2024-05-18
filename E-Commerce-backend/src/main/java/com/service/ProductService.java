package com.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.model.Category;
import com.model.Inventory;
import com.model.ModelType;
import com.model.Product;
import com.repository.CategoryDAO;
import com.repository.InventoryDAO;
import com.repository.ModelTypeDAO;
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
    private ModelTypeDAO modelTypeDAO;
    private ImageDataService imageDataService;
    
    
    
    @Value("${encryption.salt.rounds}")
    private int saltRounds;

    /**
     * Constructor for spring injection.
     * 
     * @param productDAO
     */
    public ProductService(ProductDAO productDAO, InventoryDAO inventoryDAO, CategoryDAO categoryDAO, ImageDataService imageDataService, ModelTypeDAO modelTypeDAO) {
        this.productDAO = productDAO;
        this.inventoryDAO = inventoryDAO;
        this.categoryDAO = categoryDAO;
        this.imageDataService = imageDataService;
        this.modelTypeDAO = modelTypeDAO;
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
        mapProductFields(product, newProduct);
        setCreatedAtIfNull(newProduct);
        newProduct = productDAO.save(newProduct);
        mapCategories(product, newProduct);
        mapInventories(product, newProduct);
        return productDAO.save(newProduct);
    }

    @Transactional
    public Product updateProduct(UUID productId, Product updatedProduct) {
        Optional<Product> existingProductOptional = productDAO.findById(productId);
        if (existingProductOptional.isPresent()) {
            Product existingProduct = existingProductOptional.get();
            mapProductFields(updatedProduct, existingProduct);
            setLastUpdateIfNull(existingProduct);
            existingProduct.getCategories().clear();
            mapCategories(updatedProduct, existingProduct);
            updateInventories(updatedProduct, existingProduct);
            return productDAO.save(existingProduct);
        } else {
            return null;
        }
    }

    private void mapProductFields(Product source, Product target) {
        target.setName(source.getName());
        target.setShortDescription(source.getShortDescription());
        target.setLongDescription(source.getLongDescription());
        target.setStatus(source.getStatus());
        target.setUserId(source.getUserId());
        target.setHasVariants(source.getHasVariants());
    }

    private void setCreatedAtIfNull(Product product) {
        if (product.getCreatedAt() == null) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
            String formattedDateTime = now.format(formatter);
            product.setCreatedAt(formattedDateTime);
        }
    }

    private void setLastUpdateIfNull(Product product) {
        if (product.getLastUpdate() == null) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
            String formattedDateTime = now.format(formatter);
            product.setLastUpdate(formattedDateTime);
        }
    }

    private void mapCategories(Product source, Product target) {
        for (Category category : source.getCategories()) {
            Optional<Category> existingCategoryOptional = categoryDAO.findById(category.getId());
            if (existingCategoryOptional.isPresent()) {
                Category existingCategory = existingCategoryOptional.get();
                existingCategory.getProducts().add(target);
                target.getCategories().add(existingCategory);
            } else {
                System.out.println("Category not found: " + category.getId());
            }
        }
    }

    private void mapInventories(Product source, Product target) {
        List<Inventory> newInventories = new ArrayList<>();
        for (Inventory inventory : source.getInventory()) {
            if (inventory.getQuantity() > 0) {
                Inventory newInventory = new Inventory();
                newInventory.setProduct(target);
                newInventory.setQuantity(inventory.getQuantity());
                newInventory.setColor(inventory.getColor());
                newInventory.setPrice(inventory.getPrice());
                newInventory.setSelectedModel(inventory.getSelectedModel());
                Set<ModelType> modelTypes = new HashSet<>();
                for (ModelType modelType : inventory.getModelTypes()) {
                    modelTypes.add(manageModelType(modelType));
                }
                newInventory.setModelTypes(modelTypes);
                newInventories.add(newInventory);
            }
        }
        newInventories = inventoryDAO.saveAll(newInventories);
        target.setInventory(newInventories);
    }
    
    private ModelType manageModelType(ModelType modelType) {
        Optional<ModelType> existingModelTypeOptional = Optional.ofNullable(modelTypeDAO.findByName(modelType.getName()));
        if (existingModelTypeOptional.isPresent()) {
            return existingModelTypeOptional.get();
        } else {
            return modelTypeDAO.save(modelType);
        }
    }
    

    private void updateInventories(Product source, Product target) {
        List<Inventory> existingInventories = target.getInventory();
        for (Inventory updatedInventory : source.getInventory()) {
            boolean found = false;
            for (Inventory existingInventory : existingInventories) {
                if (existingInventory.getId().equals(updatedInventory.getId())) {
                    if (updatedInventory.getQuantity() == 0) {
                        existingInventories.remove(existingInventory);
                        inventoryDAO.delete(existingInventory);
                        imageDataService.deleteInventoryFolder(source.getId(), existingInventory.getId(), "gallery");
                        imageDataService.deleteInventoryFolder(source.getId(), existingInventory.getId(), "thumbnail");
                    } else {
                        existingInventory.setQuantity(updatedInventory.getQuantity());
                        existingInventory.setColor(updatedInventory.getColor());
                        existingInventory.setPrice(updatedInventory.getPrice());
                        existingInventory.setSelectedModel(updatedInventory.getSelectedModel());
                        Set<ModelType> modelTypes = new HashSet<>();
                        for (ModelType modelType : updatedInventory.getModelTypes()) {
                            modelTypes.add(manageModelType(modelType));
                        }
                        existingInventory.setModelTypes(modelTypes);
                    }
                    found = true;
                    break;
                }
            }
            if (!found && updatedInventory.getQuantity() != 0) { 
                Inventory newInventory = new Inventory();
                newInventory.setProduct(target);
                newInventory.setQuantity(updatedInventory.getQuantity());
                newInventory.setColor(updatedInventory.getColor());
                newInventory.setPrice(updatedInventory.getPrice());
                newInventory.setSelectedModel(updatedInventory.getSelectedModel());
                Set<ModelType> modelTypes = new HashSet<>();
                for (ModelType modelType : updatedInventory.getModelTypes()) {
                    modelTypes.add(manageModelType(modelType));
                }
                newInventory.setModelTypes(modelTypes);
                existingInventories.add(newInventory);
                inventoryDAO.save(newInventory);
            }
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
    


	public List<ModelType> getModelTypes() {
		return modelTypeDAO.findAll();
	}


}