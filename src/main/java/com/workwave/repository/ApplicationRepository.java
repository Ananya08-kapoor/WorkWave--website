package com.workwave.repository;

import com.workwave.model.Application;
import com.workwave.model.Job;
import com.workwave.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByCandidateOrderByAppliedAtDesc(User candidate);
    List<Application> findByJobOrderByAppliedAtDesc(Job job);
    boolean existsByJobAndCandidate(Job job, User candidate);
    long countByJob(Job job);
    long countByJobEmployer(User employer);
}
