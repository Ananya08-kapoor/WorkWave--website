package com.workwave.config;

import com.workwave.model.*;
import com.workwave.repository.ApplicationRepository;
import com.workwave.repository.JobRepository;
import com.workwave.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Seeds demo accounts and jobs on the very first start. */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository users;
    private final JobRepository jobs;
    private final ApplicationRepository apps;
    private final PasswordEncoder encoder;

    public DataInitializer(UserRepository users, JobRepository jobs,
                           ApplicationRepository apps, PasswordEncoder encoder) {
        this.users = users;
        this.jobs = jobs;
        this.apps = apps;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (users.count() > 0) return;

        user("Portal Admin", "admin@workwave.com", "admin123", Role.ADMIN, null, null, null);
        User tech = user("Riya Sharma", "hr@techcorp.com", "employer123", Role.EMPLOYER, "TechCorp Solutions", null, null);
        User green = user("Arjun Mehta", "hr@greenleaf.com", "employer123", Role.EMPLOYER, "GreenLeaf Labs", null, null);
        User cand = user("Demo Candidate", "candidate@workwave.com", "seeker123", Role.JOB_SEEKER, null,
                "Final-year B.Tech student", "Java, Spring Boot, SQL, Git");

        Job first = job(tech, "Java Backend Developer", "Noida", JobType.FULL_TIME, "₹6–10 LPA",
                "Java, Spring Boot, SQL, REST, Git",
                "Build and maintain REST services for our hiring-tech platform.\n\nYou will work with a small, friendly team, review code, and ship features every sprint.",
                JobStatus.APPROVED);
        job(tech, "Frontend Intern", "Remote", JobType.INTERNSHIP, "₹15,000/month",
                "HTML, CSS, JavaScript, React",
                "Help us craft delightful interfaces. Great first step for students who love design and code.",
                JobStatus.APPROVED);
        job(tech, "DevOps Engineer", "Bengaluru", JobType.FULL_TIME, "₹12–18 LPA",
                "Docker, Kubernetes, AWS, Linux, CI/CD",
                "Own our CI/CD pipelines and cloud infrastructure. On-call rotation shared across the team.",
                JobStatus.APPROVED);
        job(green, "Environmental Data Analyst", "Delhi NCR", JobType.FULL_TIME, "₹5–8 LPA",
                "Python, Statistics, Excel, Environmental Chemistry",
                "Analyse air and water quality datasets and turn them into clear reports for our clients.",
                JobStatus.APPROVED);
        job(green, "Lab Technician", "Hyderabad", JobType.CONTRACT, "₹30,000/month",
                "Chemistry, Lab safety, Documentation",
                "Support our materials lab with sample preparation, testing, and record keeping.",
                JobStatus.APPROVED);
        job(green, "Embedded Systems Trainee", "Pune", JobType.PART_TIME, "₹20,000/month",
                "C++, Electronics, Arduino, Circuits",
                "Prototype sensor boards for field monitoring. Ideal for students of electrical and electronics.",
                JobStatus.APPROVED);
        job(tech, "Full Stack Developer", "Remote", JobType.REMOTE, "₹8–14 LPA",
                "Java, Spring Boot, React, SQL",
                "Work across the stack on a product used by thousands of recruiters. (Waiting for admin approval.)",
                JobStatus.PENDING);
        job(green, "Sustainability Consultant", "Mumbai", JobType.FULL_TIME, "₹7–11 LPA",
                "Environmental Chemistry, Reporting, Communication",
                "Advise companies on reducing their environmental footprint. (Waiting for admin approval.)",
                JobStatus.PENDING);

        Application a = new Application();
        a.setJob(first);
        a.setCandidate(cand);
        a.setCoverLetter("I enjoy building REST APIs with Spring Boot and would love to grow with your team.");
        a.setStatus(ApplicationStatus.UNDER_REVIEW);
        a.setEmployerNote("Thanks! We are reviewing your profile this week.");
        apps.save(a);
    }

    private User user(String name, String email, String password, Role role,
                      String company, String headline, String skills) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPassword(encoder.encode(password));
        u.setRole(role);
        u.setCompanyName(company);
        u.setHeadline(headline);
        u.setSkills(skills);
        return users.save(u);
    }

    private Job job(User employer, String title, String location, JobType type, String salary,
                    String skills, String description, JobStatus status) {
        Job j = new Job();
        j.setEmployer(employer);
        j.setTitle(title);
        j.setLocation(location);
        j.setJobType(type);
        j.setSalary(salary);
        j.setSkills(skills);
        j.setDescription(description);
        j.setStatus(status);
        return jobs.save(j);
    }
}
