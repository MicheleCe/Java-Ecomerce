package com.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.ListCrudRepository;

import com.model.LocalUser;
import com.model.VerificationToken;

/**
 * Data Access Object for the VerificationToken data.
 */
public interface VerificationTokenDAO extends ListCrudRepository<VerificationToken, UUID> {

  Optional<VerificationToken> findByToken(String token);

  void deleteByUser(LocalUser user);
  
  List<VerificationToken> findByUser_IdOrderByIdDesc(UUID id);

}
