package com.workwave.model;

public enum JobStatus {
    PENDING("Awaiting approval"), APPROVED("Live"), REJECTED("Rejected"), CLOSED("Closed");

    private final String label;
    JobStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
