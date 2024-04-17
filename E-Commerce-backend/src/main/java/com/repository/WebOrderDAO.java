package com.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.ListCrudRepository;

import com.model.LocalUser;
import com.model.WebOrder;

public interface WebOrderDAO extends ListCrudRepository<WebOrder, UUID> {

	List<WebOrder> findByUser(LocalUser user);

}
