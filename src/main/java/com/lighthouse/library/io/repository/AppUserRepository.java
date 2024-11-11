package com.lighthouse.library.io.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.lighthouse.library.io.entity.AppUserEntity;

import lombok.NonNull;

@Repository
public interface AppUserRepository extends JpaRepository<AppUserEntity, Long>,
       JpaSpecificationExecutor<AppUserEntity> {

    Optional<AppUserEntity> findByUsernameAndDeletedFalse(String username);
    boolean existsByUsernameAndDeletedFalse(@NonNull String string);
}
