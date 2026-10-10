package com.workwave.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobType jobType;

    private String salary;

    @Column(length = 500)
    private String skills;               // comma separated

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.PENDING;

    @ManyToOne(optional = false)
    private User employer;

    private String adminRemark;
    private LocalDateTime postedAt = LocalDateTime.now();

    public List<String> getSkillList() {
        if (skills == null || skills.isBlank()) return List.of();
        return Arrays.stream(skills.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    public String getCompanyName() {
        String c = employer.getCompanyName();
        return (c == null || c.isBlank()) ? employer.getName() : c;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public JobType getJobType() { return jobType; }
    public void setJobType(JobType jobType) { this.jobType = jobType; }
    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }
    public User getEmployer() { return employer; }
    public void setEmployer(User employer) { this.employer = employer; }
    public String getAdminRemark() { return adminRemark; }
    public void setAdminRemark(String adminRemark) { this.adminRemark = adminRemark; }
    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }
}
