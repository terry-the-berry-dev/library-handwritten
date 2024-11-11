package com.lighthouse.library.view.controller;

import static com.lighthouse.library.view.model.CustomObjectMapper.validateName;
import static com.lighthouse.library.view.model.CustomObjectMapper.validatePassword;

import com.lighthouse.library.io.entity.AppUserEntity;
import com.lighthouse.library.io.repository.AppUserRepository;
import com.lighthouse.library.view.model.CustomObjectMapper;
import com.lighthouse.library.view.model.response.AppUser;

import jakarta.persistence.criteria.Predicate;

import lombok.NonNull;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final AppUserRepository appUserRepository;

    public UserController(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @GetMapping
    public ResponseEntity<Iterable<AppUser>> getAppUsers(
            @RequestParam Map<String, String> filters,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortOrder) {

        Iterable<AppUserEntity> allUsers =
                appUserRepository.findAll(filter(filters), PageRequest.of(page, size));

        List<AppUser> responseAppUsers = new ArrayList<>();
        for (AppUserEntity appUserEntity : allUsers) {
            responseAppUsers.add(CustomObjectMapper.map(appUserEntity));
        }

        return ResponseEntity.ok(responseAppUsers);
    }

    @PostMapping
    public ResponseEntity<AppUser> createAppUser(@RequestBody AppUser appUser) {
        AppUserEntity appUserEntity = CustomObjectMapper.map(appUser);

        if (appUserRepository.existsByUsernameAndDeletedFalse(appUserEntity.getUsername())) {
            throw new IllegalArgumentException(
                    "User with the username already exists: " + appUserEntity.getUsername());
        }

        appUserEntity = appUserRepository.save(appUserEntity);
        appUser = CustomObjectMapper.map(appUserEntity);

        return ResponseEntity.ok(appUser);
    }

    @GetMapping("/{username}")
    public ResponseEntity<AppUser> getAppUser(@PathVariable String username) {
        AppUserEntity appUserEntity =
                appUserRepository
                        .findByUsernameAndDeletedFalse(username)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Couldn't find user with username: " + username));

        AppUser appUser = CustomObjectMapper.map(appUserEntity);

        return ResponseEntity.ok(appUser);
    }

    @PatchMapping("/{username}")
    public ResponseEntity<AppUser> updateAppUser(
            @PathVariable String username, @RequestBody AppUser appUser) {

        AppUserEntity appUserEntity =
                appUserRepository
                        .findByUsernameAndDeletedFalse(username)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Couldn't find user with the username: "
                                                        + username));

        if (appUser.getUsername() != null) {
            validateName(appUser.getUsername());

            if (appUserRepository.existsByUsernameAndDeletedFalse(appUser.getUsername())) {
                throw new IllegalArgumentException(
                        "User with the username already exists: " + appUser.getUsername());
            }

            appUserEntity.setUsername(appUser.getUsername());
        }

        if (appUser.getPassword() != null) {
            validatePassword(appUser.getPassword());
            appUserEntity.setPassword(appUser.getPassword());
        }

        appUserEntity = appUserRepository.save(appUserEntity);
        AppUser updatedAppUser = CustomObjectMapper.map(appUserEntity);

        return ResponseEntity.ok(updatedAppUser);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<AppUser> deleteAppUser(@PathVariable String username) {

        AppUserEntity appUserEntity =
                appUserRepository
                        .findByUsernameAndDeletedFalse(username)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Couldn't find user with username: " + username));

        appUserEntity.setDeleted(true);
        appUserRepository.save(appUserEntity);

        AppUser appUser = CustomObjectMapper.map(appUserEntity);
        return ResponseEntity.ok(appUser);
    }

    public static Specification<AppUserEntity> filter(@NonNull final Map<String, String> filter) {

        Boolean deleted = null;
        if (filter.containsKey("deleted")) {
            String deletedStr = filter.get("deleted");

            if (!deletedStr.matches("true|false")) {
                throw new IllegalArgumentException("deleted parameter should be true or false");
            }

            deleted = Boolean.valueOf(deletedStr);
        }

        Boolean deletedFinal = deleted;

        return (root, query, cb) -> {
            ArrayList<Predicate> predicates = new ArrayList<>();

            if (filter.containsKey("username")) {
                String username = filter.get("username");
                predicates.add(cb.like(cb.lower(root.get("username")), username.toLowerCase()));
            }

            if (deletedFinal != null) {
                predicates.add(cb.equal(root.get("deleted"), deletedFinal));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
