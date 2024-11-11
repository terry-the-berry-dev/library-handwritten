package com.lighthouse.library.io.repository;

import com.lighthouse.library.io.entity.BookEntity;
import com.lighthouse.library.io.entity.GenreEntity;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository
        extends CrudRepository<BookEntity, Long>, JpaSpecificationExecutor<BookEntity> {

    Optional<BookEntity> findByTitleAndDeletedFalse(String title);

    boolean existsByTitleAndDeletedFalse(String title);

    boolean existsByGenresAndDeletedFalse(GenreEntity genre);
}
