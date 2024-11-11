package com.lighthouse.library.view.controller;

import com.lighthouse.library.io.entity.AppUserEntity;
import com.lighthouse.library.io.repository.AppUserRepository;
import com.lighthouse.library.security.JwtService;
import com.lighthouse.library.view.model.CustomObjectMapper;
import com.lighthouse.library.view.model.response.AppUser;

import lombok.NonNull;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {

    private final JwtService jwtService;
    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationController(
            @NonNull JwtService jwtService,
            @NonNull BCryptPasswordEncoder encoder,
            @NonNull AppUserRepository appUserRepository,
            @NonNull AuthenticationManager authenticationManager) {
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.appUserRepository = appUserRepository;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/signup")
    public ResponseEntity<AppUser> register(@RequestBody AppUser appUser) {
        AppUserEntity appUserEntity = CustomObjectMapper.map(appUser);

        if (appUserRepository.existsByUsernameAndDeletedFalse(appUserEntity.getUsername())) {
            throw new IllegalArgumentException(
                    "User with the username already exists: " + appUserEntity.getUsername());
        }

        appUserEntity.setPassword(encoder.encode(appUserEntity.getPassword()));
        appUserEntity = appUserRepository.save(appUserEntity);
        appUser = CustomObjectMapper.map(appUserEntity);

        return ResponseEntity.ok(appUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody AppUser appUser) {

        CustomObjectMapper.validateName(appUser.getUsername());
        CustomObjectMapper.validatePassword(appUser.getPassword());

        try {
            Authentication authenticate =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    appUser.getUsername(), appUser.getPassword()));
            UserDetails userDetails = (UserDetails) authenticate.getPrincipal();

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, jwtService.generateToken(userDetails))
                    .body(authenticate.getPrincipal());

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
