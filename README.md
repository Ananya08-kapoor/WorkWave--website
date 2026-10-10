# WorkWave: Online Job Portal (Java + Spring Boot)

Employers post vacancies → Admin approves → Job seekers search & apply → Employer reviews → Status is updated.

## Run it
Requirements: **JDK 17+** and **Maven 3.8+** (or open the folder in IntelliJ / Eclipse / VS Code as a Maven project).

```bash
mvn spring-boot:run
# open http://localhost:8080
```

Demo accounts (created automatically on first start):

| Role       | Email                  | Password      |
|------------|------------------------|---------------|
| Admin      | admin@workwave.com      | admin123      |
| Employer   | hr@techcorp.com        | employer123   |
| Job seeker | candidate@workwave.com  | seeker123     |

H2 console (dev only): http://localhost:8080/h2-console, JDBC URL `jdbc:h2:file:./data/workwave`, user `sa`, empty password.

## Features
- **Three roles** with separate dashboards and Spring Security role-based access.
- **Job lifecycle:** `PENDING → APPROVED / REJECTED → CLOSED`. Unapproved jobs are hidden from the public.
- **Application lifecycle:** `APPLIED → UNDER_REVIEW → SHORTLISTED → HIRED` (or `REJECTED`), with an optional note to the candidate.
- **Search** by keyword (title, skill, company), location and job type.
- **Skill-match badge:** each job shows how many of its required skills the logged-in candidate has.
- **Light / dark theme** toggle (☀️/🌙). Remembers your choice and follows the OS setting on first visit.
- Application progress tracker, animated hero, stat cards, one-application-per-job rule, BCrypt passwords, CSRF protection.

## Project layout
```
src/main/java/com/workwave
├── model        User, Job, Application + enums (Role, JobType, JobStatus, ApplicationStatus)
├── repository   Spring Data JPA repositories
├── service      JobService, ApplicationService (business rules + ownership checks)
├── security     SecurityConfig, CustomUserDetailsService
├── web          Controllers (Home, Auth, Admin, Employer, Seeker)
└── config       DataInitializer (demo data)
src/main/resources
├── templates    Thymeleaf pages (+ fragments.html for navbar/footer/job card)
└── static       css/style.css (theme variables), js/app.js (theme toggle)
```

## Ideas to extend
- Real resume upload (store files, validate type/size) instead of a link
- Email notifications when status changes (Spring Mail)
- Switch H2 → MySQL/PostgreSQL (change `application.properties` + driver dependency)
- Pagination, saved jobs, company profiles, admin ability to block users
- Remove `/h2-console/**` from `SecurityConfig` before deploying anywhere public
