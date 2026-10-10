package com.workwave.web;

import com.workwave.model.User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

/** Makes "currentUser" available in every Thymeleaf template (used by the navbar). */
@ControllerAdvice
public class GlobalModelAdvice {

    private final AuthHelper auth;

    public GlobalModelAdvice(AuthHelper auth) {
        this.auth = auth;
    }

    @ModelAttribute("currentUser")
    public User currentUser(Principal principal) {
        return auth.current(principal);
    }
}
