package com.api.controller.product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.model.DataChange;
import com.model.LocalUser;
import com.model.ModelType;
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
	 * 
	 * @param productService
	 */
	public ProductController(ProductService productService, ProductDAO productDAO,
			SimpMessagingTemplate simpMessagingTemplate) {
		this.productService = productService;
		this.productDAO = productDAO;
		this.simpMessagingTemplate = simpMessagingTemplate;
	}

	/**
	 * Gets the list of products available.
	 * 
	 * @return The list of products.
	 */
	@GetMapping
	public List<Product> getProducts() {
		return productService.getProducts();
	}

	/**
	 * Adds a new product.
	 * 
	 * @param product The product to add.
	 * @return The added product.
	 */
	@PostMapping
	public ResponseEntity<Product> addProduct(@AuthenticationPrincipal LocalUser user, @RequestBody Product product) {
		if (user == null) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		
		Product newProduct = product;
		newProduct.setUserId(user.getId());
		Product returnProduct = productService.addProduct(newProduct);
		simpMessagingTemplate.convertAndSend("/topic/product",
				new DataChange<>(DataChange.ChangeType.INSERT, returnProduct));
		return ResponseEntity.status(HttpStatus.CREATED).body(returnProduct);

	}

	/**
	 * Updates an existing product.
	 * 
	 * @param productId      The ID of the product to update.
	 * @param updatedProduct The updated product data.
	 * @return ResponseEntity indicating success or failure of the update operation.
	 */
	
	@CrossOrigin
	@PatchMapping("/{productId}")
	public ResponseEntity<?> updateProduct(@PathVariable UUID productId, @RequestBody Product updatedProduct) {
		Product updated = productService.updateProduct(productId, updatedProduct);
		if (updated == null) {
			return ResponseEntity.notFound().build();
		}
		System.out.println(updated);
		simpMessagingTemplate.convertAndSend("/topic/product", new DataChange<>(DataChange.ChangeType.UPDATE, updated));
		return ResponseEntity.ok(updated);
	}
	
	
    @GetMapping("/user")
    public List<Product> getProductsByUserId(@AuthenticationPrincipal LocalUser user) {
        return productService.findProductsByUserId(user.getId());
    }
    
    @CrossOrigin
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(@AuthenticationPrincipal LocalUser user, @PathVariable UUID productId) {
		if (user == null) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
        productService.deleteProduct(productId);
       
        Optional<Product> deletedProduct = productDAO.findById(productId);
        if (deletedProduct.isEmpty()) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/model")
    public List<ModelType> getProductsModel() {
        return productService.getModelTypes();
    }
    

}
