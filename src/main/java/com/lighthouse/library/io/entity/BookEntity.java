package com.lighthouse.library.io.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "Book")
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @NonNull
    private Boolean deleted = false;

    @ManyToMany( fetch = FetchType.EAGER)
    @JoinTable(name = "BookToGenre")
    private List<GenreEntity> genres = new ArrayList<>();

    public BookEntity(@NonNull String title, @NonNull List<GenreEntity> genres) {
        this.title = title;
        this.genres = genres;
    }
}
