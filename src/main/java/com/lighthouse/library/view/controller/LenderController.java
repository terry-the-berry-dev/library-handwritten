package com.lighthouse.library.view.controller;

import static com.lighthouse.library.view.model.CustomObjectMapper.validateName;

import com.lighthouse.library.io.entity.BookEntity;
import com.lighthouse.library.io.entity.LenderEntity;
import com.lighthouse.library.io.repository.BookRepository;
import com.lighthouse.library.io.repository.LenderRepository;
import com.lighthouse.library.io.repository.LibraryRepository;
import com.lighthouse.library.view.model.CustomObjectMapper;
import com.lighthouse.library.view.model.response.Lender;

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
@RequestMapping("/lenders")
public class LenderController {

    private final LenderRepository lenderRepository;
    private final BookRepository bookRepository;
    private final LibraryRepository libraryRepository;

    public LenderController(
            @NonNull LenderRepository lenderRepository,
            @NonNull BookRepository bookRepository,
            @NonNull LibraryRepository libRepository) {
        this.lenderRepository = lenderRepository;
        this.bookRepository = bookRepository;
        this.libraryRepository = libRepository;
    }

    @GetMapping
    public ResponseEntity<List<Lender>> getLenders(
            @RequestParam Map<String, String> filters,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortOrder) {

        Iterable<LenderEntity> allLenders =
                lenderRepository.findAll(filter(filters), PageRequest.of(page, size));

        List<Lender> responseLenders = new ArrayList<>();
        for (LenderEntity lenderEntity : allLenders) {
            responseLenders.add(CustomObjectMapper.map(lenderEntity));
        }

        return ResponseEntity.ok(responseLenders);
    }

    @PostMapping
    public ResponseEntity<Lender> createLender(@RequestBody Lender lender) {
        LenderEntity lenderEntity = CustomObjectMapper.map(lender);

        if (lenderRepository.existsByNameAndDeletedFalse(lenderEntity.getName())) {
            throw new IllegalArgumentException(
                    "A lender with the name already exists: " + lenderEntity.getName());
        }

        ArrayList<BookEntity> books = new ArrayList<BookEntity>();
        for (String bookTitle : lender.getLendedBooks()) {
            BookEntity book =
                    bookRepository
                            .findByTitleAndDeletedFalse(bookTitle)
                            .orElseThrow(
                                    () ->
                                            new IllegalArgumentException(
                                                    "Couldn't find a book with the title: "
                                                            + bookTitle));
            books.add(book);
        }

        lenderEntity.setLendedBooks(books);
        lenderEntity = lenderRepository.save(lenderEntity);
        lender = CustomObjectMapper.map(lenderEntity);

        return ResponseEntity.ok(lender);
    }

    @GetMapping("/{name}")
    public ResponseEntity<Lender> getLender(@PathVariable String name) {
        LenderEntity lenderEntity =
                lenderRepository
                        .findByNameAndDeletedFalse(name)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Couldn't find a lender with the name: " + name));

        Lender lender = CustomObjectMapper.map(lenderEntity);

        return ResponseEntity.ok(lender);
    }

    @PatchMapping("/{name}")
    public ResponseEntity<Lender> updateLender(
            @PathVariable String name, @RequestBody Lender lender) {

        LenderEntity lenderEntity =
                lenderRepository
                        .findByNameAndDeletedFalse(name)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Couldn't find a lender with the name: " + name));

        if (lender.getName() != null) {
            validateName(lender.getName());

            if (lenderRepository.existsByNameAndDeletedFalse(lender.getName())) {
                throw new IllegalArgumentException(
                        "A lender with the name already exists: " + lender.getName());
            }

            lenderEntity.setName(lender.getName());
        }

        if (!lender.getLendedBooks().isEmpty()) {
            lenderEntity.getLendedBooks().clear();

            for (String bookTitle : lender.getLendedBooks()) {
                BookEntity authoredBook =
                        bookRepository
                                .findByTitleAndDeletedFalse(bookTitle)
                                .orElseThrow(
                                        () ->
                                                new IllegalArgumentException(
                                                        "A Book with the title doesn't exist: "
                                                                + bookTitle));

                lenderEntity.getLendedBooks().add(authoredBook);
            }
        }

        lenderEntity = lenderRepository.save(lenderEntity);
        lender = CustomObjectMapper.map(lenderEntity);

        return ResponseEntity.ok(lender);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Lender> deleteLender(@PathVariable String name) {

        LenderEntity lenderEntity =
                lenderRepository
                        .findByNameAndDeletedFalse(name)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Couldn't find a lender with the name: " + name));

        if (libraryRepository.existsByLenders(lenderEntity)) {
            throw new IllegalArgumentException("The lender is referenced by a library");
        }

        lenderEntity.setDeleted(true);
        lenderRepository.save(lenderEntity);
        Lender lender = CustomObjectMapper.map(lenderEntity);

        return ResponseEntity.ok(lender);
    }

    public static Specification<LenderEntity> filter(@NonNull final Map<String, String> filter) {

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

            if (filter.containsKey("name")) {
                String username = filter.get("name");
                predicates.add(
                        cb.like(cb.lower(root.get("name")), "%" + username.toLowerCase() + "%"));
            }

            if (deletedFinal != null) {
                predicates.add(cb.equal(root.get("deleted"), deletedFinal));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
