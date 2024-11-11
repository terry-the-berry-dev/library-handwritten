package com.lighthouse.library.view.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** AppUser */
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

    private String username;

    private String password;
    private boolean deleted;
}
