package com.lighthouse.library.view.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Lender {
    private String name;
    private boolean deleted;
    private List<String> lendedBooks = new ArrayList<>();
}
