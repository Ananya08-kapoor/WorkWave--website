package com.workwave.service;

import com.workwave.model.Job;
import com.workwave.model.JobStatus;
import com.workwave.model.JobType;
import com.workwave.model.Role;
import com.workwave.model.User;
import com.workwave.repository.JobRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JobService {

    private final JobRepository jobs;

    public JobService(JobRepository jobs) {
        this.jobs = jobs;
    }

    /** Public search: only APPROVED jobs, filtered by keyword / location / type. */
    public List<Job> search(String keyword, String location, JobType type) {
        String k = normalise(keyword);
        String l = normalise(location);
        return jobs.findByStatusOrderByPostedAtDesc(JobStatus.APPROVED).stream()
                .filter(j -> k == null
                        || contains(j.getTitle(), k)
                        || contains(j.getSkills(), k)
                        || contains(j.getCompanyName(), k)
                        || contains(j.getDescription(), k))
                .filter(j -> l == null || contains(j.getLocation(), l))
                .filter(j -> type == null || j.getJobType() == type)
                .toList();
    }

    public Job get(Long id) {
        return jobs.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
    }

    public Job getOwned(Long id, User employer) {
        Job job = get(id);
        if (!job.getEmployer().getId().equals(employer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This is not your job posting");
        }
        return job;
    }

    public List<Job> forEmployer(User employer) { return jobs.findByEmployerOrderByPostedAtDesc(employer); }
    public List<Job> byStatus(JobStatus status) { return jobs.findByStatusOrderByPostedAtDesc(status); }
    public List<Job> all() { return jobs.findAllByOrderByPostedAtDesc(); }
    public long count(JobStatus status) { return jobs.countByStatus(status); }

    /** Step 1 of the workflow: employer posts a job -> it waits for admin approval. */
    public Job create(User employer, Job job) {
        job.setEmployer(employer);
        job.setStatus(JobStatus.PENDING);
        return jobs.save(job);
    }

    /** Step 2 of the workflow: admin approves or rejects. */
    public void review(Long id, boolean approve, String remark) {
        Job job = get(id);
        job.setStatus(approve ? JobStatus.APPROVED : JobStatus.REJECTED);
        job.setAdminRemark(remark == null || remark.isBlank() ? null : remark.trim());
        jobs.save(job);
    }

    public void close(Long id, User employer) {
        Job job = getOwned(id, employer);
        job.setStatus(JobStatus.CLOSED);
        jobs.save(job);
    }

    // ---- skill matching (the "% match" badge) ----

    public Map<Long, Integer> matchMap(User user, List<Job> list) {
        Map<Long, Integer> map = new HashMap<>();
        if (user != null && user.getRole() == Role.JOB_SEEKER
                && user.getSkills() != null && !user.getSkills().isBlank()) {
            for (Job j : list) {
                if (j.getSkills() != null && !j.getSkills().isBlank()) {
                    map.put(j.getId(), matchPercent(user.getSkills(), j.getSkills()));
                }
            }
        }
        return map;
    }

    public static int matchPercent(String seekerSkills, String jobSkills) {
        Set<String> need = split(jobSkills);
        if (need.isEmpty()) return 0;
        Set<String> have = split(seekerSkills);
        long hits = need.stream().filter(have::contains).count();
        return (int) Math.round(100.0 * hits / need.size());
    }

    private static Set<String> split(String csv) {
        if (csv == null || csv.isBlank()) return Set.of();
        return Arrays.stream(csv.split(","))
                .map(s -> s.trim().toLowerCase())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private static String normalise(String s) {
        return (s == null || s.isBlank()) ? null : s.trim().toLowerCase();
    }

    private static boolean contains(String haystack, String needle) {
        return haystack != null && haystack.toLowerCase().contains(needle);
    }
}
