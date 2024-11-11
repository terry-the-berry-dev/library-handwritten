package com.lighthouse.library.view.controller;

import static com.lighthouse.library.view.model.CustomObjectMapper.validateName;

import com.lighthouse.library.io.entity.AuthorEntity;
import com.lighthouse.library.io.entity.BookEntity;
import com.lighthouse.library.io.repository.AuthorRepository;
import com.lighthouse.library.io.repository.BookRepository;
import com.lighthouse.library.view.model.CustomObjectMapper;
import com.lighthouse.library.view.model.response.Author;

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

/** AuthorController */
@RestController
@RequestMapping("/authors")
public class AuthorController {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public AuthorController(
            @NonNull BookRepository bookRepository, @NonNull AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }

    @GetMapping
    public ResponseEntity<List<Author>> getAuthors(
            @RequestParam Map<String, String> filters,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortOrder) {

        Iterable<AuthorEntity> allAuthors =
                authorRepository.findAll(filter(filters), PageRequest.of(page, size));

        List<Author> responseAuthors = new ArrayList<>();
        for (AuthorEntity bookEntity : allAuthors) {
            responseAuthors.add(CustomObjectMapper.map(bookEntity));
        }

        return ResponseEntity.ok(responseAuthors);
    }

    @PostMapping
    public ResponseEntity<Author> createAuthor(@RequestBody Author author) {
        AuthorEntity authorEntity = CustomObjectMapper.map(author);

        if (authorRepository.existsByNameAndDeletedFalse(authorEntity.getName())) {
            throw new IllegalArgumentException(
                    "An author with the name already exists: " + authorEntity.getName());
        }

        List<BookEntity> authoredBooks = authorEntity.getAuthoredBooks();

        for (String bookTitle : author.getAuthoredBooks()) {
            BookEntity authoredBook =
                    bookRepository
                            .findByTitleAndDeletedFalse(bookTitle)
                            .orElseThrow(
                                    () ->
                                            new IllegalArgumentException(
                                                    "A Book with the title doesn't exist: "
                                                            + bookTitle));

            authoredBooks.add(authoredBook);
        }

        authorEntity.setAuthoredBooks(authoredBooks);
        authorEntity = authorRepository.save(authorEntity);
        author = CustomObjectMapper.map(authorEntity);

        return ResponseEntity.ok(author);
    }

    @GetMapping("/{name}")
    public ResponseEntity<Author> getAuthor(@PathVariable String name) {
        AuthorEntity authorEntity =
                authorRepository
                        .findByNameAndDeletedFalse(name)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Couldn't find an author with the name: " + name));

        Author author = CustomObjectMapper.map(authorEntity);

        return ResponseEntity.ok(author);
    }

    @PatchMapping("/{name}")
    public ResponseEntity<Author> updateAuthor(
            @PathVariable String name, @RequestBody Author author) {

        AuthorEntity authorEntity =
                authorRepository
                        .findByNameAndDeletedFalse(name)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Couldn't find an author with the name: " + name));

        if (author.getName() != null) {
            validateName(author.getName());

            if (authorRepository.existsByNameAndDeletedFalse(author.getName())) {
                throw new IllegalArgumentException(
                        "An Author with the name already exists: " + author.getName());
            }

            authorEntity.setName(author.getName());
        }

        if (!author.getAuthoredBooks().isEmpty()) {
            authorEntity.getAuthoredBooks().clear();

            for (String bookTitle : author.getAuthoredBooks()) {
                BookEntity authoredBook =
                        bookRepository
                                .findByTitleAndDeletedFalse(bookTitle)
                                .orElseThrow(
                                        () ->
                                                new IllegalArgumentException(
                                                        "A Book with the title doesn't exist: "
                                                                + bookTitle));

                authorEntity.getAuthoredBooks().add(authoredBook);
            }
        }

        authorEntity = authorRepository.save(authorEntity);
        author = CustomObjectMapper.map(authorEntity);

        return ResponseEntity.ok(author);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Author> deleteAuthor(@PathVariable String name) {

        AuthorEntity authorEntity =
                authorRepository
                        .findByNameAndDeletedFalse(name)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Couldn't find an author with the name: " + name));

        authorRepository.delete(authorEntity);
        Author author = CustomObjectMapper.map(authorEntity);

        return ResponseEntity.ok(author);
    }

    public static Specification<AuthorEntity> filter(@NonNull final Map<String, String> filter) {

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
