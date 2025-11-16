package com.example.demo.adapter.out.persistence;

import com.example.demo.adapter.out.persistence.entity.TransactionCategoryJpaEntity;
import com.example.demo.adapter.out.persistence.mapper.CategoryMapper;
import com.example.demo.adapter.out.persistence.repository.TransactionCategoryJpaRepository;
import com.example.demo.application.ports.out.CategoryRepository;
import com.example.demo.domain.TransactionCategory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA implementation of CategoryRepository (Output Port)
 * This is a persistence adapter in the hexagonal architecture
 */
@Repository
@Primary
public class JpaCategoryRepository implements CategoryRepository {

    private final TransactionCategoryJpaRepository jpaRepository;
    private final CategoryMapper mapper;

    public JpaCategoryRepository(TransactionCategoryJpaRepository jpaRepository, CategoryMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public TransactionCategory save(TransactionCategory category) {
        TransactionCategoryJpaEntity entity = mapper.toEntity(category);
        TransactionCategoryJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public TransactionCategory update(TransactionCategory category) {
        TransactionCategoryJpaEntity entity = mapper.toEntity(category);
        TransactionCategoryJpaEntity updated = jpaRepository.save(entity);
        return mapper.toDomain(updated);
    }

    @Override
    public Optional<TransactionCategory> findById(Long id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Optional<TransactionCategory> findByName(String name) {
        return jpaRepository.findByName(name.toUpperCase())
            .map(mapper::toDomain);
    }

    @Override
    public List<TransactionCategory> findAll() {
        return jpaRepository.findAll().stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<TransactionCategory> findAllActive() {
        return jpaRepository.findByActive(true).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<TransactionCategory> findByType(TransactionCategory.CategoryType type) {
        TransactionCategoryJpaEntity.CategoryType entityType =
            TransactionCategoryJpaEntity.CategoryType.valueOf(type.name());

        return jpaRepository.findByType(entityType).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<TransactionCategory> findActiveByType(TransactionCategory.CategoryType type) {
        TransactionCategoryJpaEntity.CategoryType entityType =
            TransactionCategoryJpaEntity.CategoryType.valueOf(type.name());

        return jpaRepository.findByTypeAndActive(entityType, true).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name.toUpperCase());
    }

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }
}
