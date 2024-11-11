package com.lighthouse.library.view.controller;

import static com.lighthouse.library.view.model.CustomObjectMapper.validateBookTitle;

import com.lighthouse.library.io.entity.BookEntity;
import com.lighthouse.library.io.entity.GenreEntity;
import com.lighthouse.library.io.repository.AuthorRepository;
import com.lighthouse.library.io.repository.BookRepository;
import com.lighthouse.library.io.repository.GenreRepository;
import com.lighthouse.library.io.repository.LenderRepository;
import com.lighthouse.library.io.repository.LibraryRepository;
import com.lighthouse.library.view.model.CustomObjectMapper;
import com.lighthouse.library.view.model.response.Book;
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
@RequestMapping("/books")
public class BookController {

    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;
    private final LibraryRepository libraryRepository;
    private final AuthorRepository authorRepository;
    private final LenderRepository lenderRepository;

    public BookController(
            @NonNull BookRepository bookRepository,
            @NonNull GenreRepository genreRepository,
            @NonNull LibraryRepository libraryRepository,
            @NonNull AuthorRepository authorRepository,
            @NonNull LenderRepository lenderRepository) {

        this.bookRepository = bookRepository;
        this.genreRepository = genreRepository;
        this.libraryRepository = libraryRepository;
        this.authorRepository = authorRepository;
        this.lenderRepository = lenderRepository;
    }

    @GetMapping
    public ResponseEntity<List<Book>> getBooks(
            @RequestParam Map<String, String> filters,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortOrder) {

        Iterable<BookEntity> allBooks =
                bookRepository.findAll(filter(filters), PageRequest.of(page, size));

        List<Book> responseBooks = new ArrayList<>();
        for (BookEntity bookEntity : allBooks) {
            responseBooks.add(CustomObjectMapper.map(bookEntity));
        }

        return ResponseEntity.ok(responseBooks);
    }

    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        BookEntity bookEntity = CustomObjectMapper.map(book);

        if (bookRepository.existsByTitleAndDeletedFalse(bookEntity.getTitle())) {
            throw new IllegalArgumentException(
                    "A Book with a title already exists: " + bookEntity.getTitle());
        }

        List<GenreEntity> genres = bookEntity.getGenres();

        for (int i = 0; i < genres.size(); i++) {
            GenreEntity mappedGenreEntity = genres.get(i);
            GenreEntity genreEntity =
                    genreRepository
                            .findByNameAndDeletedFalse(mappedGenreEntity.getName())
                            .orElseGet(() -> genreRepository.save(mappedGenreEntity));

            genres.set(i, genreEntity);
        }

        bookEntity = bookRepository.save(bookEntity);
        book = CustomObjectMapper.map(bookEntity);

        return ResponseEntity.ok(book);
    }

    @GetMapping("/{title}")
    public ResponseEntity<Book> getBook(@PathVariable String title) {
        BookEntity bookEntity =
                bookRepository
                        .findByTitleAndDeletedFalse(title)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Couldn't find a book with the title: " + title));

        Book book = CustomObjectMapper.map(bookEntity);

        return ResponseEntity.ok(book);
    }

    @PatchMapping("/{title}")
    public ResponseEntity<Book> updateBook(@PathVariable String title, @RequestBody Book book) {

        BookEntity bookEntity =
                bookRepository
                        .findByTitleAndDeletedFalse(title)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Couldn't find a book with the title: " + title));

        if (book.getTitle() != null) {
            validateBookTitle(book.getTitle());

            if (bookRepository.existsByTitleAndDeletedFalse(book.getTitle())) {
                throw new IllegalArgumentException(
                        "A Book with the title already exists: " + book.getTitle());
            }

            bookEntity.setTitle(book.getTitle());
        }

        if (!book.getGenres().isEmpty()) {
            bookEntity.getGenres().clear();

            for (Genre genre : book.getGenres()) {
                GenreEntity mappedGenreEntity = CustomObjectMapper.map(genre);
                GenreEntity genreEntity =
                        genreRepository
                                .findByNameAndDeletedFalse(mappedGenreEntity.getName())
                                .orElseGet(() -> genreRepository.save(mappedGenreEntity));

                bookEntity.getGenres().add(genreEntity);
            }
        }

        bookEntity = bookRepository.save(bookEntity);
        book = CustomObjectMapper.map(bookEntity);

        return ResponseEntity.ok(book);
    }

    @DeleteMapping("/{title}")
    public ResponseEntity<Book> deleteBook(@PathVariable String title) {

        BookEntity bookEntity =
                bookRepository
                        .findByTitleAndDeletedFalse(title)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Couldn't find a book with the title: " + title));

        if (libraryRepository.existsByBooks(bookEntity)) {
            throw new IllegalArgumentException("The book is referenced by a library");
        }

        if (authorRepository.existsByAuthoredBooks(bookEntity)) {
            throw new IllegalArgumentException("The book is referenced by an author");
        }

        if (lenderRepository.existsByLendedBooks(bookEntity)) {
            throw new IllegalArgumentException("The book is referenced by a lender");
        }

        bookRepository.delete(bookEntity);

        Book book = CustomObjectMapper.map(bookEntity);

        return ResponseEntity.ok(book);
    }

    public static Specification<BookEntity> filter(@NonNull final Map<String, String> filter) {

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

            if (filter.containsKey("title")) {
                String title = filter.get("title");
                predicates.add(
                        cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }

            if (deletedFinal != null) {
                predicates.add(cb.equal(root.get("deleted"), deletedFinal));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
