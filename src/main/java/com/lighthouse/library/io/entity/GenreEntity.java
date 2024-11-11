package com.lighthouse.library.io.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.io.Serializable;

@Entity(name = "Genre")
@Table(name = "genres")
@Getter
@Setter
@NoArgsConstructor
public class GenreEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    @NonNull
    private Boolean deleted = false;

    public GenreEntity(@NonNull String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "GenreEntity [id=" + id + ", name=" + name + "]";
    }
}
