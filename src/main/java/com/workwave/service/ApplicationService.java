package com.workwave.service;

import com.workwave.model.Application;
import com.workwave.model.ApplicationStatus;
import com.workwave.model.Job;
import com.workwave.model.JobStatus;
import com.workwave.model.User;
import com.workwave.repository.ApplicationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository apps;

    public ApplicationService(ApplicationRepository apps) {
        this.apps = apps;
    }

    /** Step 3 of the workflow: job seeker applies. */
    public Application apply(Job job, User candidate, String coverLetter, String resumeLink) {
        if (job.getStatus() != JobStatus.APPROVED) {
            throw new IllegalStateException("This job is not open for applications.");
        }
        if (apps.existsByJobAndCandidate(job, candidate)) {
            throw new IllegalStateException("You have already applied to this job.");
        }
        String link = resumeLink == null ? "" : resumeLink.trim();
        if (!link.isEmpty() && !link.startsWith("http://") && !link.startsWith("https://")) {
            throw new IllegalStateException("Resume link must start with http:// or https://");
        }
        Application a = new Application();
        a.setJob(job);
        a.setCandidate(candidate);
        a.setCoverLetter(coverLetter == null ? "" : coverLetter.trim());
        a.setResumeLink(link.isEmpty() ? null : link);
        return apps.save(a);
    }

    /** Steps 4 + 5: employer reviews the application and updates its status. */
    public Application updateStatus(Long applicationId, User employer, ApplicationStatus status, String note) {
        Application a = apps.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        if (!a.getJob().getEmployer().getId().equals(employer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your job posting");
        }
        a.setStatus(status);
        a.setEmployerNote(note == null || note.isBlank() ? null : note.trim());
        a.setUpdatedAt(LocalDateTime.now());
        return apps.save(a);
    }

    public List<Application> forCandidate(User candidate) { return apps.findByCandidateOrderByAppliedAtDesc(candidate); }
    public List<Application> forJob(Job job) { return apps.findByJobOrderByAppliedAtDesc(job); }
    public long countForJob(Job job) { return apps.countByJob(job); }
    public long countForEmployer(User employer) { return apps.countByJobEmployer(employer); }
    public long countAll() { return apps.count(); }
}
