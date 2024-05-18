package com.model;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * A product available for purchasing.
 */


@Entity
@Table(name = "product")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "short_description")
	private String shortDescription;

	@Column(name = "long_description")
	private String longDescription;

	@Column(name = "has_Variants")
	private Boolean hasVariants;

	@Column(name = "created_at", updatable = false)
	private String createdAt;
	
	@Column(name = "last_update")
	private String lastUpdate;
	
	@Column(name = "user_id", nullable = false)
	private UUID userId;
	
	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private ProductStatus status;
	
	@ManyToMany
	@JoinTable(name = "product_category", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
	private Set<Category> categories = new HashSet<>();

	@OneToMany(mappedBy = "product", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	private List<Inventory> inventory = new ArrayList<>();

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public String getLongDescription() {
		return longDescription;
	}

	public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}


	public ProductStatus getStatus() {
		return status;
	}

	public void setStatus(ProductStatus status) {
		this.status = status;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String formattedDateTime) {
		this.createdAt = formattedDateTime;
	}

	public String getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(String lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public Set<Category> getCategories() {
		return categories;
	}

	public void setCategories(Set<Category> categories) {
		this.categories = categories;
	}


	public List<Inventory> getInventory() {
		return inventory;
	}

	public void setInventory(List<Inventory> inventory) {
		this.inventory = inventory;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}
	
	public Boolean getHasVariants() {
		return hasVariants;
	}

	public void setHasVariants(Boolean hasVariant) {
		this.hasVariants = hasVariant;
	}


	
}