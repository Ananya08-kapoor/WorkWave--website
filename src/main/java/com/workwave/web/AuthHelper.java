package com.workwave.web;

import com.workwave.model.User;
import com.workwave.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

/** Small helper: turns the logged-in Principal into our User entity. */
@Component
public class AuthHelper {

    private final UserRepository users;

    public AuthHelper(UserRepository users) {
        this.users = users;
    }

    public User current(Principal principal) {
        return principal == null ? null : users.findByEmail(principal.getName()).orElse(null);
    }

    public User require(Principal principal) {
        User u = current(principal);
        if (u == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return u;
    }
}
