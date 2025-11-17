package com.example.demo.adapter.out.persistence.repository;

import com.example.demo.adapter.out.persistence.entity.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for Account persistence
 * Uses standard JPA repository - database agnostic
 */
@Repository
public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, UUID> {

    Optional<AccountJpaEntity> findByAccountNumber(String accountNumber);

    List<AccountJpaEntity> findByUserId(UUID userId);

    boolean existsByAccountNumber(String accountNumber);
}
