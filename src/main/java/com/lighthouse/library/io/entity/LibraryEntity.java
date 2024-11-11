package com.lighthouse.library.io.entity;

import java.util.ArrayList;
import java.util.List;

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

@Entity(name = "Library")
@Table(name = "libraries")
@Getter
@Setter
@NoArgsConstructor
public class LibraryEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @NonNull
    private Boolean deleted = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "LibraryToLender")
    private List<LenderEntity> lenders = new ArrayList<>();

    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "LibraryToBook")
    private List<BookEntity> books = new ArrayList<>();

    public LibraryEntity(@NonNull String name, @NonNull List<LenderEntity> lenders, @NonNull List<BookEntity> books) {
        this.name = name;
        this.lenders = lenders;
        this.books = books;
    }



}
