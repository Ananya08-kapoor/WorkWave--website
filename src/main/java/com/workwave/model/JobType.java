package com.workwave.model;

public enum JobType {
    FULL_TIME("Full-time"), PART_TIME("Part-time"), INTERNSHIP("Internship"),
    REMOTE("Remote"), CONTRACT("Contract");

    private final String label;
    JobType(String label) { this.label = label; }
    public String getLabel() { return label; }
}
