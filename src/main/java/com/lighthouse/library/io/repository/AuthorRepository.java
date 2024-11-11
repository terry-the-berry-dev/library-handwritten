package com.lighthouse.library.io.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.lighthouse.library.io.entity.AuthorEntity;
import com.lighthouse.library.io.entity.BookEntity;

@Repository
public interface AuthorRepository extends JpaRepository<AuthorEntity, Long>, JpaSpecificationExecutor<AuthorEntity> {

    Optional<AuthorEntity> findByNameAndDeletedFalse(String name);

    boolean existsByNameAndDeletedFalse(String name);
    boolean existsByAuthoredBooks(BookEntity book);
}
