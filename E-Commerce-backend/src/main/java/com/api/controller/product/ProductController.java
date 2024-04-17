package com.api.controller.product;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.model.DataChange;
import com.model.Product;
import com.repository.ProductDAO;
import com.service.ProductService;

/**
 * Controller to handle the creation, updating & viewing of products.
 */
@RestController
@RequestMapping("/product")
public class ProductController {

  /** The Product Service. */
	private ProductDAO productDAO;
	private SimpMessagingTemplate simpMessagingTemplate;
  private ProductService productService;

  /**
   * Constructor for spring injection.
   * @param productService
   */
  public ProductController(ProductService productService, ProductDAO productDAO, SimpMessagingTemplate simpMessagingTemplate) {
    this.productService = productService;
    this.productDAO = productDAO;
    this.simpMessagingTemplate = simpMessagingTemplate;
  }

  /**
   * Gets the list of products available.
   * @return The list of products.
   */
  @GetMapping
  public List<Product> getProducts() {
    return productService.getProducts();
  }
  
  /**
   * Adds a new product.
   * @param product The product to add.
   * @return The added product.
   */
  @PostMapping
  public ResponseEntity<Product> addProduct(@RequestBody Product product) {
	        Product newProduct = productService.addProduct(product);
	        simpMessagingTemplate.convertAndSend("/topic/product",
	                new DataChange<>(DataChange.ChangeType.INSERT, newProduct));
	        return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);

	}
  
  
  /**
   * Updates an existing product.
   * @param productId The ID of the product to update.
   * @param updatedProduct The updated product data.
   * @return ResponseEntity indicating success or failure of the update operation.
   */
  
  @PatchMapping("/{productId}")
  public ResponseEntity<?> updateProduct(@PathVariable UUID productId, @RequestBody Product updatedProduct) {
      // Update the product using ProductService
      Product updated = productService.updateProduct(productId, updatedProduct);
      if (updated == null) {
          // If product is not found, return 404
          return ResponseEntity.notFound().build();
      }
      // Send WebSocket notification for product update
      simpMessagingTemplate.convertAndSend("/topic/product", new DataChange<>(DataChange.ChangeType.UPDATE, updated));
      // Return the updated product
      return ResponseEntity.ok(updated);
  }
  
  
  
  
  
  
  

}
