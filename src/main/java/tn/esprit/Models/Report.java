package tn.esprit.Models;

import java.sql.Timestamp;

public class Report {
    private int reportId;
    private int reporterId;
    private int reportedUserId;
    private ReportReason reason;
    private String evidence;
    private ReportStatus status;
    private Timestamp createdAt;

    // Constructeur
    public Report(int reporterId, int reportedUserId, ReportReason reason, String evidence) {
        this.reporterId = reporterId;
        this.reportedUserId = reportedUserId;
        this.reason = reason;
        this.evidence = evidence;
        this.status = ReportStatus.PENDING;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public Report(int reporterId, int reportedUserId, ReportReason reason, String evidence, ReportStatus status, Timestamp createdAt) {
        this.reporterId = reporterId;
        this.reportedUserId = reportedUserId;
        this.reason = reason;
        this.evidence = evidence;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters et Setters
    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public int getReporterId() { return reporterId; }
    public void setReporterId(int reporterId) { this.reporterId = reporterId; }

    public int getReportedUserId() { return reportedUserId; }
    public void setReportedUserId(int reportedUserId) { this.reportedUserId = reportedUserId; }

    public ReportReason getReason() { return reason; }
    public void setReason(ReportReason reason) { this.reason = reason; }

    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }

    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "Report{" +
                "reportId=" + reportId +
                ", reporterId=" + reporterId +
                ", reportedUserId=" + reportedUserId +
                ", reason=" + reason +
                ", evidence='" + evidence + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
