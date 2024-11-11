package com.lighthouse.library.view.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Author */
@Getter
@Setter
public class Author {
    private String name;
    private List<String> authoredBooks = new ArrayList<>();
    private boolean deleted;
}
