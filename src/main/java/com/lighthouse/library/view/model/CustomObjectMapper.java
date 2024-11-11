package com.lighthouse.library.view.model;

import com.lighthouse.library.io.entity.AppUserEntity;
import com.lighthouse.library.io.entity.AuthorEntity;
import com.lighthouse.library.io.entity.BookEntity;
import com.lighthouse.library.io.entity.GenreEntity;
import com.lighthouse.library.io.entity.LenderEntity;
import com.lighthouse.library.io.entity.LibraryEntity;
import com.lighthouse.library.view.model.response.AppUser;
import com.lighthouse.library.view.model.response.Author;
import com.lighthouse.library.view.model.response.Book;
import com.lighthouse.library.view.model.response.Genre;
import com.lighthouse.library.view.model.response.Lender;
import com.lighthouse.library.view.model.response.Library;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

/** CustomObjectMapper */
public class CustomObjectMapper {

    public static AppUser map(@NonNull AppUserEntity appUserEntity) {

        validateName(appUserEntity.getUsername());
        validatePassword(appUserEntity.getPassword());

        AppUser appUser = new AppUser();
        appUser.setUsername(appUserEntity.getUsername());
        appUser.setPassword(appUserEntity.getPassword());
        appUser.setDeleted(appUserEntity.getDeleted());

        return appUser;
    }

    public static AppUserEntity map(@NonNull AppUser appUser) {

        validateName(appUser.getUsername());
        validatePassword(appUser.getPassword());

        AppUserEntity appUserEntity = new AppUserEntity();
        appUserEntity.setUsername(appUser.getUsername());
        appUserEntity.setPassword(appUser.getPassword());
        appUserEntity.setDeleted(appUser.isDeleted());

        return appUserEntity;
    }

    public static Book map(@NonNull BookEntity bookEntity) {

        validateBookTitle(bookEntity.getTitle());

        Book book = new Book();
        List<Genre> genres = new ArrayList<>();

        for (GenreEntity genreEntity : bookEntity.getGenres()) {
            genres.add(map(genreEntity));
        }

        book.setTitle(bookEntity.getTitle());
        book.setDeleted(bookEntity.getDeleted());
        book.setGenres(genres);

        return book;
    }

    public static BookEntity map(@NonNull Book book) {

        validateBookTitle(book.getTitle());

        BookEntity bookEntity = new BookEntity();
        List<GenreEntity> genres = new ArrayList<>();

        for (Genre genre : book.getGenres()) {
            genres.add(map(genre));
        }

        bookEntity.setTitle(book.getTitle());
        bookEntity.setDeleted(book.isDeleted());
        bookEntity.setGenres(genres);

        return bookEntity;
    }

    public static Genre map(@NonNull GenreEntity genreEntity) {

        validateGenre(genreEntity.getName());

        Genre genre = new Genre();
        genre.setName(genreEntity.getName());
        genre.setDelted(genreEntity.getDeleted());

        return genre;
    }

    public static GenreEntity map(@NonNull Genre genre) {

        validateGenre(genre.getName());

        GenreEntity genreEntity = new GenreEntity();
        genreEntity.setName(genre.getName());
        genreEntity.setDeleted(genre.isDelted());

        return genreEntity;
    }

    public static Author map(@NonNull AuthorEntity authorEntity) {

        validateName(authorEntity.getName());

        Author author = new Author();
        ArrayList<String> authoredBookTitles = new ArrayList<>();

        author.setName(authorEntity.getName());
        author.setDeleted(authorEntity.getDeleted());
        author.setAuthoredBooks(authoredBookTitles);

        for (BookEntity book : authorEntity.getAuthoredBooks()) {
            validateBookTitle(book.getTitle());
            authoredBookTitles.add(book.getTitle());
        }

        return author;
    }

    public static AuthorEntity map(@NonNull Author author) {

        validateName(author.getName());

        AuthorEntity authorEntity = new AuthorEntity();
        ArrayList<BookEntity> authoredBooks = new ArrayList<>();

        authorEntity.setName(author.getName());
        authorEntity.setDeleted(author.isDeleted());
        authorEntity.setAuthoredBooks(authoredBooks);

        for (String bookTitle : author.getAuthoredBooks()) {
            validateBookTitle(bookTitle);
            authoredBooks.add(new BookEntity(bookTitle, new ArrayList<>()));
        }

        return authorEntity;
    }

    public static Lender map(@NonNull LenderEntity lenderEntity) {

        validateName(lenderEntity.getName());

        Lender lender = new Lender();
        ArrayList<String> bookTitles = new ArrayList<>();

        lender.setName(lenderEntity.getName());
        lender.setDeleted(lenderEntity.getDeleted());
        lender.setLendedBooks(bookTitles);

        for (BookEntity bookEntity : lenderEntity.getLendedBooks()) {
            bookTitles.add(bookEntity.getTitle());
        }

        return lender;
    }

    public static LenderEntity map(@NonNull Lender lender) {

        validateName(lender.getName());

        LenderEntity lenderEntity = new LenderEntity();
        ArrayList<BookEntity> books = new ArrayList<>();

        lenderEntity.setName(lender.getName());
        lenderEntity.setDeleted(lender.isDeleted());
        lenderEntity.setLendedBooks(books);

        for (String title : lender.getLendedBooks()) {
            validateBookTitle(title);
            books.add(new BookEntity(title, new ArrayList<>()));
        }

        return lenderEntity;
    }

    public static Library map(@NonNull LibraryEntity libraryEntity) {

        validateLibrary(libraryEntity.getName());

        Library library = new Library();
        ArrayList<String> bookTitles = new ArrayList<>();
        ArrayList<String> lenderNames = new ArrayList<>();

        library.setName(libraryEntity.getName());
        library.setDeleted(libraryEntity.getDeleted());
        library.setBooks(bookTitles);
        library.setLenders(lenderNames);

        for (BookEntity bookEntity : libraryEntity.getBooks()) {
            bookTitles.add(bookEntity.getTitle());
        }
        for (LenderEntity lenderEntity : libraryEntity.getLenders()) {
            lenderNames.add(lenderEntity.getName());
        }

        return library;
    }

    public static LibraryEntity map(@NonNull Library library) {

        validateLibrary(library.getName());

        LibraryEntity libraryEntity = new LibraryEntity();
        ArrayList<BookEntity> books = new ArrayList<>();
        ArrayList<LenderEntity> lenders = new ArrayList<>();

        libraryEntity.setName(library.getName());
        libraryEntity.setDeleted(library.isDeleted());
        libraryEntity.setBooks(books);
        libraryEntity.setLenders(lenders);

        for (String title : library.getBooks()) {
            validateBookTitle(title);
            books.add(new BookEntity(title, new ArrayList<>()));
        }
        for (String name : library.getLenders()) {
            validateName(name);
            lenders.add(new LenderEntity(name, new ArrayList<>()));
        }

        return libraryEntity;
    }

    public static void validateName(String username) {
        validateName(username, "Name");
    }

    private static void validateName(String string, @NonNull String name) {
        if (string == null) {
            throw new IllegalArgumentException("The %s should be present".formatted(name));
        }

        if (string.isBlank()) {
            throw new IllegalArgumentException("The %s cannot be blank".formatted(name));
        }

        if (string.length() < 4 || string.length() > 20) {
            throw new IllegalArgumentException(
                    "The %s length should not be less than 4 characters and no more than 20"
                            .formatted(name));
        }
    }

    public static void validatePassword(String password) {
        validatePassword(password, "Password");
    }

    private static void validatePassword(String string, @NonNull String name) {
        if (string == null) {
            throw new IllegalArgumentException("The %s should be present".formatted(name));
        }

        if (string.isBlank()) {
            throw new IllegalArgumentException("The %s cannot be blank".formatted(name));
        }

        if (string.length() < 4 || string.length() > 60) {
            throw new IllegalArgumentException(
                    "The %s length should not be less than 4 characters and no more than 20"
                            .formatted(name));
        }
    }

    public static void validateBookTitle(String title) {
        validateBookTitleOrGenreOrLibraryName(title, "Book Title");
    }

    public static void validateGenre(String genre) {
        validateBookTitleOrGenreOrLibraryName(genre, "Book Genre");
    }

    public static void validateLibrary(String library) {
        validateBookTitleOrGenreOrLibraryName(library, "Library Name");
    }

    private static void validateBookTitleOrGenreOrLibraryName(String string, @NonNull String name) {
        if (string == null) {
            throw new IllegalArgumentException("The %s should be present".formatted(name));
        }

        if (string.isBlank()) {
            throw new IllegalArgumentException("The %s cannot be blank".formatted(name));
        }

        if (string.length() < 3 || string.length() > 40) {
            throw new IllegalArgumentException(
                    "The %s length should not be less than 3 characters and no more than 40"
                            .formatted(name));
        }
    }
}
