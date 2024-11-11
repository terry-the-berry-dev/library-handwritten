package com.lighthouse.library.view.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Book */
@Getter
@Setter
public class Book {
    private String title;
    private boolean deleted;
    private List<Genre> genres = new ArrayList<>();
}
