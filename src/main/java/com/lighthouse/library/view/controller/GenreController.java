package com.lighthouse.library.view.controller;

import static com.lighthouse.library.view.model.CustomObjectMapper.validateGenre;

import com.lighthouse.library.io.entity.GenreEntity;
import com.lighthouse.library.io.repository.BookRepository;
import com.lighthouse.library.io.repository.GenreRepository;
import com.lighthouse.library.view.model.CustomObjectMapper;
import com.lighthouse.library.view.model.response.Genre;

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
@RequestMapping("/genres")
public class GenreController {

    private final GenreRepository genreRepository;
    private final BookRepository bookRepository;

    public GenreController(
            @NonNull GenreRepository genreRepository, @NonNull BookRepository bookRepository) {
        this.genreRepository = genreRepository;
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public ResponseEntity<List<Genre>> getGenres(
            @RequestParam Map<String, String> filters,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortOrder) {
        Iterable<GenreEntity> allGenres =
                genreRepository.findAll(filter(filters), PageRequest.of(page, size));

        List<Genre> responseGenres = new ArrayList<>();
        for (GenreEntity genreEntity : allGenres) {
            responseGenres.add(CustomObjectMapper.map(genreEntity));
        }

        return ResponseEntity.ok(responseGenres);
    }

    @PostMapping
    public ResponseEntity<Genre> createGenre(@RequestBody Genre genre) {
        GenreEntity genreEntity = CustomObjectMapper.map(genre);

        if (genreRepository.existsByNameAndDeletedFalse(genreEntity.getName())) {
            throw new IllegalArgumentException(
                    "A genre with a name already exists: " + genreEntity.getName());
        }

        genreEntity = genreRepository.save(genreEntity);
        genre = CustomObjectMapper.map(genreEntity);

        return ResponseEntity.ok(genre);
    }

    @GetMapping("/{name}")
    public ResponseEntity<Genre> getGenre(@PathVariable String name) {
        GenreEntity genreEntity =
                genreRepository
                        .findByNameAndDeletedFalse(name)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Couldn't find a genre with the name: " + name));

        Genre genre = CustomObjectMapper.map(genreEntity);

        return ResponseEntity.ok(genre);
    }

    @PatchMapping("/{name}")
    public ResponseEntity<Genre> updateGenre(@PathVariable String name, @RequestBody Genre genre) {

        GenreEntity genreEntity =
                genreRepository
                        .findByNameAndDeletedFalse(name)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Couldn't find a genre with the name: " + name));

        if (genre.getName() != null) {
            validateGenre(genre.getName());

            if (genreRepository.existsByNameAndDeletedFalse(genre.getName())) {
                throw new IllegalArgumentException(
                        "A genre with a name already exists: " + genre.getName());
            }

            genreEntity.setName(genre.getName());
        }

        genreEntity = genreRepository.save(genreEntity);
        genre = CustomObjectMapper.map(genreEntity);

        return ResponseEntity.ok(genre);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Genre> deleteGenre(@PathVariable String name) {

        GenreEntity genreEntity =
                genreRepository
                        .findByNameAndDeletedFalse(name)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Couldn't find a genre with the name: " + name));

        if (bookRepository.existsByGenresAndDeletedFalse(genreEntity)) {
            throw new IllegalArgumentException("The genre is referenced by a book");
        }

        genreEntity.setDeleted(true);
        genreRepository.save(genreEntity);
        Genre genre = CustomObjectMapper.map(genreEntity);

        return ResponseEntity.ok(genre);
    }

    public static Specification<GenreEntity> filter(@NonNull final Map<String, String> filter) {

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
