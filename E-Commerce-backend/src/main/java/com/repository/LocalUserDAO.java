package com.repository;

import org.springframework.data.repository.ListCrudRepository;

import com.model.LocalUser;

import java.util.Optional;
import java.util.UUID;

/**
 * Data Access Object for the LocalUser data.
 */
public interface LocalUserDAO extends ListCrudRepository<LocalUser, UUID> {

  Optional<LocalUser> findByUsernameIgnoreCase(String username);

  Optional<LocalUser> findByEmailIgnoreCase(String email);

}