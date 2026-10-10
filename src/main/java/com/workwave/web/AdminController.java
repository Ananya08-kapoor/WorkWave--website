package com.workwave.web;

import com.workwave.model.JobStatus;
import com.workwave.model.Role;
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

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final JobService jobService;
    private final ApplicationService appService;
    private final UserRepository users;

    public AdminController(JobService jobService, ApplicationService appService, UserRepository users) {
        this.jobService = jobService;
        this.appService = appService;
        this.users = users;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pending", jobService.byStatus(JobStatus.PENDING));
        model.addAttribute("allJobs", jobService.all());
        model.addAttribute("pendingCount", jobService.count(JobStatus.PENDING));
        model.addAttribute("liveCount", jobService.count(JobStatus.APPROVED));
        model.addAttribute("userCount", users.count());
        model.addAttribute("applicationCount", appService.countAll());
        return "admin/dashboard";
    }

    @PostMapping("/jobs/{id}/approve")
    public String approve(@PathVariable Long id,
                          @RequestParam(required = false) String remark,
                          RedirectAttributes redirect) {
        jobService.review(id, true, remark);
        redirect.addFlashAttribute("success", "Job approved and published.");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/jobs/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam(required = false) String remark,
                         RedirectAttributes redirect) {
        jobService.review(id, false, remark);
        redirect.addFlashAttribute("success", "Job rejected.");
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/users")
    public String userList(Model model) {
        model.addAttribute("users", users.findAllByOrderByCreatedAtDesc());
        model.addAttribute("employerCount", users.countByRole(Role.EMPLOYER));
        model.addAttribute("seekerCount", users.countByRole(Role.JOB_SEEKER));
        return "admin/users";
    }
}
