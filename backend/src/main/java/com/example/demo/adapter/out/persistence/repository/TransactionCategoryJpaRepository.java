package com.example.demo.adapter.out.persistence.repository;

import com.example.demo.adapter.out.persistence.entity.TransactionCategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for Transaction Category persistence
 */
@Repository
public interface TransactionCategoryJpaRepository extends JpaRepository<TransactionCategoryJpaEntity, UUID> {

    Optional<TransactionCategoryJpaEntity> findByName(String name);

    boolean existsByName(String name);

    List<TransactionCategoryJpaEntity> findByActive(boolean active);

    List<TransactionCategoryJpaEntity> findByType(TransactionCategoryJpaEntity.CategoryType type);

    List<TransactionCategoryJpaEntity> findByTypeAndActive(TransactionCategoryJpaEntity.CategoryType type, boolean active);
}
