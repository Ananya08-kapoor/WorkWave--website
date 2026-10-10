package com.workwave.repository;

import com.workwave.model.Job;
import com.workwave.model.JobStatus;
import com.workwave.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByStatusOrderByPostedAtDesc(JobStatus status);
    List<Job> findByEmployerOrderByPostedAtDesc(User employer);
    List<Job> findAllByOrderByPostedAtDesc();
    long countByStatus(JobStatus status);
}
