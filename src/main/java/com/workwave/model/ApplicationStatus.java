package com.workwave.model;

public enum ApplicationStatus {
    APPLIED("Applied"), UNDER_REVIEW("Under review"), SHORTLISTED("Shortlisted"),
    HIRED("Hired"), REJECTED("Rejected");

    private final String label;
    ApplicationStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
