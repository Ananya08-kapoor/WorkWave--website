package com.workwave.web;

import com.workwave.model.Application;
import com.workwave.model.ApplicationStatus;
import com.workwave.model.Job;
import com.workwave.model.User;
import com.workwave.repository.UserRepository;
import com.workwave.service.ApplicationService;
import com.workwave.service.JobService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/seeker")
public class SeekerController {

    private final JobService jobService;
    private final ApplicationService appService;
    private final UserRepository users;
    private final AuthHelper auth;

    public SeekerController(JobService jobService, ApplicationService appService,
                            UserRepository users, AuthHelper auth) {
        this.jobService = jobService;
        this.appService = appService;
        this.users = users;
        this.auth = auth;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User me = auth.require(principal);
        List<Application> list = appService.forCandidate(me);

        model.addAttribute("me", me);
        model.addAttribute("applications", list);
        model.addAttribute("steps", List.of(ApplicationStatus.APPLIED, ApplicationStatus.UNDER_REVIEW,
                ApplicationStatus.SHORTLISTED, ApplicationStatus.HIRED));
        model.addAttribute("total", list.size());
        model.addAttribute("inReview", list.stream().filter(a -> a.getStatus() == ApplicationStatus.UNDER_REVIEW).count());
        model.addAttribute("shortlisted", list.stream().filter(a -> a.getStatus() == ApplicationStatus.SHORTLISTED).count());
        model.addAttribute("hired", list.stream().filter(a -> a.getStatus() == ApplicationStatus.HIRED).count());
        return "seeker/dashboard";
    }

    @PostMapping("/jobs/{id}/apply")
    public String apply(@PathVariable Long id,
                        @RequestParam String coverLetter,
                        @RequestParam(required = false) String resumeLink,
                        Principal principal, RedirectAttributes redirect) {
        Job job = jobService.get(id);
        try {
            appService.apply(job, auth.require(principal), coverLetter, resumeLink);
        } catch (IllegalStateException ex) {
            redirect.addFlashAttribute("flashError", ex.getMessage());
            return "redirect:/jobs/" + id;
        }
        redirect.addFlashAttribute("success", "Application sent to " + job.getCompanyName() + "! Track it below.");
        return "redirect:/seeker/dashboard";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam(required = false) String headline,
                                @RequestParam(required = false) String skills,
                                Principal principal, RedirectAttributes redirect) {
        User me = auth.require(principal);
        me.setHeadline(headline == null ? null : headline.trim());
        me.setSkills(skills == null ? null : skills.trim());
        users.save(me);
        redirect.addFlashAttribute("success", "Profile updated. Skill-match scores are refreshed.");
        return "redirect:/seeker/dashboard";
    }
}
