package com.workwave.web;

import com.workwave.model.Role;
import com.workwave.model.User;
import com.workwave.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthController(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(@RequestParam(required = false) String role, Model model) {
        model.addAttribute("selectedRole", "EMPLOYER".equalsIgnoreCase(role) ? "EMPLOYER" : "JOB_SEEKER");
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String role,
                           @RequestParam(required = false) String companyName,
                           @RequestParam(required = false) String headline,
                           @RequestParam(required = false) String skills,
                           Model model, RedirectAttributes redirect) {

        String cleanEmail = email.trim().toLowerCase();
        String error = null;
        if (name.isBlank()) {
            error = "Please enter your name.";
        } else if (password.length() < 6) {
            error = "Password must be at least 6 characters.";
        } else if (users.existsByEmail(cleanEmail)) {
            error = "An account with this email already exists.";
        } else if (!role.equals("EMPLOYER") && !role.equals("JOB_SEEKER")) {
            error = "Please choose a valid account type.";   // ADMIN can never be self-registered
        } else if (role.equals("EMPLOYER") && (companyName == null || companyName.isBlank())) {
            error = "Employers must enter a company name.";
        }

        if (error != null) {
            model.addAttribute("flashError", error);
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            model.addAttribute("selectedRole", role);
            return "register";
        }

        User u = new User();
        u.setName(name.trim());
        u.setEmail(cleanEmail);
        u.setPassword(encoder.encode(password));
        u.setRole(Role.valueOf(role));
        if (u.getRole() == Role.EMPLOYER) {
            u.setCompanyName(companyName.trim());
        } else {
            u.setHeadline(headline == null ? null : headline.trim());
            u.setSkills(skills == null ? null : skills.trim());
        }
        users.save(u);

        redirect.addFlashAttribute("success", "Account created! Log in to continue.");
        return "redirect:/login";
    }
}
