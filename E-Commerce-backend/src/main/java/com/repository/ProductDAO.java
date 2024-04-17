package com.repository;

import java.util.UUID;

import org.springframework.data.repository.ListCrudRepository;

import com.model.Product;

public interface ProductDAO extends ListCrudRepository<Product, UUID>{

}
