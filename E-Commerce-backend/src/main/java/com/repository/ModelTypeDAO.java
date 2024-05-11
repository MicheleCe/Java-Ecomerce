package com.repository;

import java.util.UUID;

import org.springframework.data.repository.ListCrudRepository;

import com.model.ModelType;

public interface ModelTypeDAO extends ListCrudRepository<ModelType, UUID> {

	ModelType findByName(String modelName);


	}
