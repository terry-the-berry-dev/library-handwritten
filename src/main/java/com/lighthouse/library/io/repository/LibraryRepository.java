package com.lighthouse.library.io.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.lighthouse.library.io.entity.BookEntity;
import com.lighthouse.library.io.entity.LenderEntity;
import com.lighthouse.library.io.entity.LibraryEntity;

@Repository
public interface LibraryRepository extends CrudRepository<LibraryEntity, Long>, JpaSpecificationExecutor<LibraryEntity> {

    Optional<LibraryEntity> findByNameAndDeletedFalse(String name);

    boolean existsByNameAndDeletedFalse(String name);
    boolean existsByLenders(LenderEntity lender);
    boolean existsByBooks(BookEntity book);
}
