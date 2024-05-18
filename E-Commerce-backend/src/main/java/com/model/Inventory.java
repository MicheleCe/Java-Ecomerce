package com.model;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Inventory of a product that available for purchase.
 */
@Entity
@Table(name = "inventory")
public class Inventory {

	/** Unique id for the inventory. */
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false, unique = true)
	private UUID id;
	/** The product this inventory is of. */
	@JsonIgnore
	@ManyToOne(optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	/** The quantity in stock. */
	@Column(name = "quantity", nullable = false)
	private Integer quantity;
	
	/** The price of the product. */
	@Column(name = "price", nullable = false)
	private Double price;

	@Column(name = "long_description")
	private String VariantDescription;

	@Column(name = "selected_model")
	private String selectedModel;

	@Column(name = "color")
	private String color;
	
	
    @ManyToMany
    @JoinTable(
            name = "inventory_model_type",
            joinColumns = @JoinColumn(name = "inventory_id"),
			inverseJoinColumns = @JoinColumn(name = "model_type_id")
    )
	private Set<ModelType> modelTypes;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}
	
	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	
	

	public Set<ModelType> getModelTypes() {
		return modelTypes;
	}

	public void setModelTypes(Set<ModelType> modelTypes) {
		this.modelTypes = modelTypes;
	}

	public String getVariantDescription() {
		return VariantDescription;
	}

	public void setVariantDescription(String variantDescription) {
		VariantDescription = variantDescription;
	}

	public String getSelectedModel() {
		return selectedModel;
	}

	public void setSelectedModel(String selectedModel) {
		this.selectedModel = selectedModel;
	}


	
}
