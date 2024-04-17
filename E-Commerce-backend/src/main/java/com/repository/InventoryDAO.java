package com.repository;

import java.util.UUID;

import org.springframework.data.repository.ListCrudRepository;

import com.model.Inventory;

/**
 * Data Access Object for the Address data.
 */
public interface InventoryDAO extends ListCrudRepository<Inventory, UUID> {

  Inventory findByProduct_id(UUID id);

}
