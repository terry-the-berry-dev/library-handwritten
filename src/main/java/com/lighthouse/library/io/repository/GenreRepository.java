package com.lighthouse.library.io.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.lighthouse.library.io.entity.GenreEntity;

@Repository
public interface GenreRepository extends CrudRepository<GenreEntity, Long>, JpaSpecificationExecutor<GenreEntity> {

    Optional<GenreEntity> findByNameAndDeletedFalse(String name);

    boolean existsByNameAndDeletedFalse(String name);
}
