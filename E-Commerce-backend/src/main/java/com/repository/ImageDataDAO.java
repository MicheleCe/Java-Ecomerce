package com.repository;


import java.util.UUID;

import org.springframework.data.repository.ListCrudRepository;

import com.model.ImageData;

public interface ImageDataDAO extends ListCrudRepository<ImageData, UUID>{
		

}
