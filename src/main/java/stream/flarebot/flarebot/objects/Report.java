package stream.flarebot.flarebot.objects;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents an individual report
 */
public class Report implements Comparable<Report> {

    private int id;
    private String message;
    private String reporterId;
    private String reportedId;
    private Timestamp time;
    private ReportStatus status;
    private List<ReportMessage> messages = new CopyOnWriteArrayList<>();

    public Report(int id, String message, String reporterId, String reportedId, Timestamp time, ReportStatus status) {

        this.id = id;
        this.message = message;
        this.time = time;

        this.reporterId = reporterId;
        this.reportedId = reportedId;

        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getReporterId() {
        return reporterId;
    }

    public String getReportedId() {
        return reportedId;
    }

    public Timestamp getTime() {
        return time;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public Report setStatus(ReportStatus status) {
        this.status = status;
        return this;
    }

    public List<ReportMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ReportMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int compareTo(Report o) {
        return this.getId() - o.getId();
    }
}
