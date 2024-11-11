package com.lighthouse.library.view.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

import com.lighthouse.library.io.entity.BookEntity;
import com.lighthouse.library.io.entity.LenderEntity;
import com.lighthouse.library.io.entity.LibraryEntity;
import com.lighthouse.library.io.repository.BookRepository;
import com.lighthouse.library.io.repository.LenderRepository;
import com.lighthouse.library.io.repository.LibraryRepository;
import com.lighthouse.library.view.model.CustomObjectMapper;
import com.lighthouse.library.view.model.response.Library;

import jakarta.persistence.criteria.Predicate;
import lombok.NonNull;

@RestController
@RequestMapping("/libraries")
public class LibraryController {

    private final LibraryRepository libraryRepository;
    private final LenderRepository lenderRepository;
    private final BookRepository bookRepository;

    public LibraryController(
            @NonNull LenderRepository lenderRepository, 
            @NonNull LibraryRepository libraryRepository,
            @NonNull BookRepository bookRepository) {
        this.lenderRepository = lenderRepository;
        this.libraryRepository = libraryRepository;
        this.bookRepository = bookRepository;
    }


    @GetMapping
    public ResponseEntity<List<Library>> getLibraries(
            @RequestParam Map<String, String> filters,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortOrder) {

        Iterable<LibraryEntity> allLibraries = libraryRepository.findAll(filter(filters), PageRequest.of(page, size));

        List<Library> libraries = new ArrayList<>();
        for (LibraryEntity lenderEntity : allLibraries) {
            libraries.add(CustomObjectMapper.map(lenderEntity));
        }

        return ResponseEntity.ok(libraries);
    }

    @PostMapping
    public ResponseEntity<Library> createLibrary(@RequestBody Library library) {
        LibraryEntity libraryEntity = CustomObjectMapper.map(library);

        if (libraryRepository.existsByNameAndDeletedFalse(libraryEntity.getName())) {
            throw new IllegalArgumentException(
                    "A library with the name already exists: " + libraryEntity.getName());
        }

        ArrayList<BookEntity> books = new ArrayList<BookEntity>(); 
        ArrayList<LenderEntity> lenders = new ArrayList<>(); 
        libraryEntity.setBooks(books);
        libraryEntity.setLenders(lenders);

        for (String bookTitle : library.getBooks()) {
            BookEntity book = bookRepository.findByTitleAndDeletedFalse(bookTitle)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Couldn't find a book with the title: " + bookTitle));
            books.add(book);
        }

        for (String lenderName : library.getLenders()) {
            LenderEntity lender = lenderRepository.findByNameAndDeletedFalse(lenderName)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Couldn't find a lender with the name: " + lenderName));
            lenders.add(lender);
        }

        libraryEntity = libraryRepository.save(libraryEntity);
        library = CustomObjectMapper.map(libraryEntity);

        return ResponseEntity.ok(library);
    }

    @GetMapping("/{name}")
    public ResponseEntity<Library> getLibrary(@PathVariable String name) {
        LibraryEntity libraryEntity =
                libraryRepository
                        .findByNameAndDeletedFalse(name)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Couldn't find a library with the name: " + name));

        Library lender = CustomObjectMapper.map(libraryEntity);

        return ResponseEntity.ok(lender);
    }

    @PatchMapping("/{name}")
    public ResponseEntity<Library> updateLibrary(
            @PathVariable String name, @RequestBody Library library) {

        LibraryEntity libraryEntity =
        libraryRepository
                        .findByNameAndDeletedFalse(name)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Couldn't find a library with the name: " + name));

        if (library.getName() != null) {
           CustomObjectMapper.validateLibrary(library.getName());

            if (libraryRepository.existsByNameAndDeletedFalse(library.getName())) {
                throw new IllegalArgumentException(
                        "A library with the name already exists: " + library.getName());
            }

            libraryEntity.setName(library.getName());
        }

        if (!library.getBooks().isEmpty()) {
            libraryEntity.getBooks().clear();

            for (String bookTitle : library.getBooks()) {
                BookEntity book = bookRepository.findByTitleAndDeletedFalse(bookTitle)
                    .orElseThrow(() -> new IllegalArgumentException(
                                "A Book with the title doesn't exist: " + bookTitle));

                libraryEntity.getBooks().add(book);
            }
        }

        if (!library.getLenders().isEmpty()) {
            libraryEntity.getLenders().clear();

            for (String lenderName : library.getBooks()) {
                LenderEntity lender = lenderRepository.findByNameAndDeletedFalse(lenderName)
                    .orElseThrow(() -> new IllegalArgumentException(
                                "A lender with the name doesn't exist: " + lenderName));

                libraryEntity.getLenders().add(lender);
            }
        }

        libraryEntity = libraryRepository.save(libraryEntity);
        library = CustomObjectMapper.map(libraryEntity);

        return ResponseEntity.ok(library);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Library> deleteLibrary(@PathVariable String name) {

        LibraryEntity libraryEntity =
                libraryRepository
                        .findByNameAndDeletedFalse(name)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Couldn't find a library with the name: " + name));

        libraryEntity.setDeleted(true);
        libraryRepository.save(libraryEntity);

        Library library = CustomObjectMapper.map(libraryEntity);

        return ResponseEntity.ok(library);
    }

    public static Specification<LibraryEntity> filter(@NonNull final Map<String, String> filter) {

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
                predicates.add(cb.like(cb.lower(root.get("name")), username.toLowerCase()));
            }

            if (deletedFinal != null) {
                predicates.add(cb.equal(root.get("deleted"), deletedFinal));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
