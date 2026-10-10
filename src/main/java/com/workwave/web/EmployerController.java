package com.workwave.web;

import com.workwave.model.Application;
import com.workwave.model.ApplicationStatus;
import com.workwave.model.Job;
import com.workwave.model.JobStatus;
import com.workwave.model.JobType;
import com.workwave.model.User;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/employer")
public class EmployerController {

    private final JobService jobService;
    private final ApplicationService appService;
    private final AuthHelper auth;

    public EmployerController(JobService jobService, ApplicationService appService, AuthHelper auth) {
        this.jobService = jobService;
        this.appService = appService;
        this.auth = auth;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User me = auth.require(principal);
        List<Job> myJobs = jobService.forEmployer(me);

        Map<Long, Long> counts = new HashMap<>();
        for (Job j : myJobs) counts.put(j.getId(), appService.countForJob(j));

        model.addAttribute("jobs", myJobs);
        model.addAttribute("counts", counts);
        model.addAttribute("totalJobs", myJobs.size());
        model.addAttribute("liveJobs", myJobs.stream().filter(j -> j.getStatus() == JobStatus.APPROVED).count());
        model.addAttribute("pendingJobs", myJobs.stream().filter(j -> j.getStatus() == JobStatus.PENDING).count());
        model.addAttribute("totalApplicants", appService.countForEmployer(me));
        return "employer/dashboard";
    }

    @GetMapping("/jobs/new")
    public String newJobForm(Model model) {
        model.addAttribute("types", JobType.values());
        return "employer/post-job";
    }

    @PostMapping("/jobs")
    public String postJob(@RequestParam String title,
                          @RequestParam String location,
                          @RequestParam JobType jobType,
                          @RequestParam(required = false) String salary,
                          @RequestParam(required = false) String skills,
                          @RequestParam String description,
                          Principal principal, RedirectAttributes redirect) {
        if (title.isBlank() || location.isBlank() || description.isBlank()) {
            redirect.addFlashAttribute("flashError", "Title, location and description are required.");
            return "redirect:/employer/jobs/new";
        }
        Job job = new Job();
        job.setTitle(title.trim());
        job.setLocation(location.trim());
        job.setJobType(jobType);
        job.setSalary(salary == null || salary.isBlank() ? null : salary.trim());
        job.setSkills(skills == null ? null : skills.trim());
        job.setDescription(description.trim());
        jobService.create(auth.require(principal), job);

        redirect.addFlashAttribute("success", "Job submitted! It will go live once an admin approves it.");
        return "redirect:/employer/dashboard";
    }

    @PostMapping("/jobs/{id}/close")
    public String close(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        jobService.close(id, auth.require(principal));
        redirect.addFlashAttribute("success", "Job closed. It no longer accepts applications.");
        return "redirect:/employer/dashboard";
    }

    @GetMapping("/jobs/{id}/applications")
    public String applications(@PathVariable Long id, Model model, Principal principal) {
        Job job = jobService.getOwned(id, auth.require(principal));
        model.addAttribute("job", job);
        model.addAttribute("applications", appService.forJob(job));
        model.addAttribute("statuses", ApplicationStatus.values());
        return "employer/applications";
    }

    @PostMapping("/applications/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam ApplicationStatus status,
                               @RequestParam(required = false) String note,
                               Principal principal, RedirectAttributes redirect) {
        Application a = appService.updateStatus(id, auth.require(principal), status, note);
        redirect.addFlashAttribute("success",
                a.getCandidate().getName() + " is now marked \"" + status.getLabel() + "\".");
        return "redirect:/employer/jobs/" + a.getJob().getId() + "/applications";
    }
}
