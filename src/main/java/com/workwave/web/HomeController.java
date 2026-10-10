package com.workwave.web;

import com.workwave.model.Job;
import com.workwave.model.JobStatus;
import com.workwave.model.JobType;
import com.workwave.model.Role;
import com.workwave.model.User;
import com.workwave.repository.ApplicationRepository;
import com.workwave.repository.UserRepository;
import com.workwave.service.JobService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@Controller
public class HomeController {

    private final JobService jobService;
    private final UserRepository users;
    private final ApplicationRepository apps;
    private final AuthHelper auth;

    public HomeController(JobService jobService, UserRepository users,
                          ApplicationRepository apps, AuthHelper auth) {
        this.jobService = jobService;
        this.users = users;
        this.apps = apps;
        this.auth = auth;
    }

    @GetMapping("/")
    public String home(Model model, Principal principal) {
        User me = auth.current(principal);
        List<Job> latest = jobService.search(null, null, null).stream().limit(6).toList();
        model.addAttribute("jobs", latest);
        model.addAttribute("matches", jobService.matchMap(me, latest));
        model.addAttribute("types", JobType.values());
        model.addAttribute("liveJobs", jobService.count(JobStatus.APPROVED));
        model.addAttribute("employers", users.countByRole(Role.EMPLOYER));
        model.addAttribute("seekers", users.countByRole(Role.JOB_SEEKER));
        return "index";
    }

    @GetMapping("/jobs")
    public String search(@RequestParam(required = false) String keyword,
                         @RequestParam(required = false) String location,
                         @RequestParam(required = false) JobType type,
                         Model model, Principal principal) {
        User me = auth.current(principal);
        List<Job> results = jobService.search(keyword, location, type);
        model.addAttribute("jobs", results);
        model.addAttribute("matches", jobService.matchMap(me, results));
        model.addAttribute("types", JobType.values());
        model.addAttribute("keyword", keyword);
        model.addAttribute("location", location);
        model.addAttribute("type", type);
        return "jobs";
    }

    @GetMapping("/jobs/{id}")
    public String detail(@PathVariable Long id, Model model, Principal principal) {
        Job job = jobService.get(id);
        User me = auth.current(principal);

        boolean privileged = me != null
                && (me.getRole() == Role.ADMIN || job.getEmployer().getId().equals(me.getId()));
        if (job.getStatus() != JobStatus.APPROVED && !privileged) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);   // hide unapproved jobs from the public
        }

        boolean seeker = me != null && me.getRole() == Role.JOB_SEEKER;
        model.addAttribute("job", job);
        model.addAttribute("alreadyApplied", seeker && apps.existsByJobAndCandidate(job, me));
        if (seeker && me.getSkills() != null && !me.getSkills().isBlank()) {
            model.addAttribute("match", JobService.matchPercent(me.getSkills(), job.getSkills()));
        }
        return "job-detail";
    }
}
