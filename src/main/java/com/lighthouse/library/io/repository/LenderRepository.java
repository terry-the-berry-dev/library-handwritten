package com.lighthouse.library.io.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.lighthouse.library.io.entity.BookEntity;
import com.lighthouse.library.io.entity.LenderEntity;

@Repository
public interface LenderRepository extends CrudRepository<LenderEntity, Long>, JpaSpecificationExecutor<LenderEntity>  {

    Optional<LenderEntity> findByNameAndDeletedFalse(String name);

    boolean existsByNameAndDeletedFalse(String name);
    boolean existsByLendedBooks(BookEntity book);
}
