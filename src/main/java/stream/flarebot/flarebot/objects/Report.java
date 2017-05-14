package stream.flarebot.flarebot.objects;

import java.sql.Timestamp;

/**
 * Represents an individual report
 */
public class Report {
    private String guildId;

    private int id;

    private String message;

    private String reporterId;
    private String reportedId;

    private Timestamp time;

    private ReportStatus status;

    public Report(String guildId, int id, String message, String reporterId, String reportedId, Timestamp time, ReportStatus status){
        this.guildId = guildId;
        this.id = id;
        this.message = message;
        this.time = time;

        this.reporterId = reporterId;
        this.reportedId = reportedId;

        this.status = status;
    }

    public int getId(){ return id; }

    public String getGuildId(){ return guildId; }

    public String getMessage(){ return message; }

    public String getReporterId(){ return reporterId; }

    public String getReportedId(){ return reportedId; }

    public Timestamp getTime(){ return time; }

    public ReportStatus getStatus(){ return status; }

    public void setStatus(ReportStatus status){ this.status = status; }
}
