package com.lighthouse.library.security;

import com.lighthouse.library.io.entity.AppUserEntity;
import com.lighthouse.library.io.entity.AuthorEntity;
import com.lighthouse.library.io.entity.BookEntity;
import com.lighthouse.library.io.entity.GenreEntity;
import com.lighthouse.library.io.entity.LenderEntity;
import com.lighthouse.library.io.entity.LibraryEntity;
import com.lighthouse.library.io.repository.AppUserRepository;
import com.lighthouse.library.io.repository.AuthorRepository;
import com.lighthouse.library.io.repository.BookRepository;
import com.lighthouse.library.io.repository.GenreRepository;
import com.lighthouse.library.io.repository.LenderRepository;
import com.lighthouse.library.io.repository.LibraryRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

@Configuration
public class ApplicationConfiguration {
    private final AppUserRepository userRepository;

    public ApplicationConfiguration(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    UserDetailsService userDetailsService() {

        return username -> {
            AppUserEntity user =
                    userRepository
                            .findByUsernameAndDeletedFalse(username)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            return new User(
                    user.getUsername(),
                    user.getPassword(),
                    List.of(new SimpleGrantedAuthority("USER")));
        };
    }

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    ApplicationRunner loadUsers(
            AppUserRepository appUserRepository,
            BookRepository bookRepository,
            GenreRepository genreRepository,
            AuthorRepository authRepository,
            LenderRepository lenderRepository,
            LibraryRepository libraryRepository,
            BCryptPasswordEncoder encoder,
            @Value("${admin.username}") String adminUsername,
            @Value("${admin.password}") String adminPass) {

        return args -> {
            appUserRepository.save(
                    new AppUserEntity(adminUsername, encoder.encode(adminPass), "ADMIN"));
            appUserRepository.save(new AppUserEntity("user", encoder.encode("pass")));

            List<GenreEntity> book1Genres =
                    List.of(
                            new GenreEntity("Dystopian Fiction"),
                            new GenreEntity("Political Fiction"));
            List<GenreEntity> book2Genres =
                    List.of(new GenreEntity("Southern Gothic"), new GenreEntity("Legal Drama"));
            List<GenreEntity> book3Genres =
                    List.of(new GenreEntity("Tragedy"), new GenreEntity("Classic"));

            genreRepository.saveAll(book1Genres);
            genreRepository.saveAll(book2Genres);
            genreRepository.saveAll(book3Genres);

            BookEntity book1 = bookRepository.save(new BookEntity("1984", book1Genres));
            BookEntity book2 =
                    bookRepository.save(new BookEntity("To Kill a Mockingbird", book2Genres));
            BookEntity book3 = bookRepository.save(new BookEntity("The Great Gatsby", book3Genres));

            authRepository.save(new AuthorEntity("George Orwell", List.of(book1)));
            authRepository.save(new AuthorEntity("Harper Lee", List.of(book2, book3)));
            authRepository.save(new AuthorEntity("F. Scott Fitzgerald", List.of()));

            LenderEntity lender1 = lenderRepository.save(new LenderEntity("Smith", List.of()));
            LenderEntity lender2 = lenderRepository.save(new LenderEntity("Mike", List.of(book1)));
            LenderEntity lender3 =
                    lenderRepository.save(new LenderEntity("Jake", List.of(book2, book3)));

            libraryRepository.save(
                    new LibraryEntity("Library of Congress", List.of(lender1), List.of(book1)));
            libraryRepository.save(
                    new LibraryEntity(
                            "Bodleian Library", List.of(lender2, lender3), List.of(book2, book3)));
            libraryRepository.save(new LibraryEntity("Vatican Library", List.of(), List.of()));
        };
    }
}
