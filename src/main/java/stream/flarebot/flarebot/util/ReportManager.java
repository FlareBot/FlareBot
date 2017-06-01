package stream.flarebot.flarebot.util;

import stream.flarebot.flarebot.objects.Report;
import stream.flarebot.flarebot.objects.ReportStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public final class ReportManager {

    private ReportManager() {
    }

    private final List<Report> reportsToSave = new ArrayList<>();

    public List<Report> getGuildReports(String guildID) {
        List<Report> reports = new ArrayList<>();
        try {
            SQLController.runSqlTask(conn -> {
                ResultSet set = conn.createStatement().executeQuery("SELECT * FROM reports WHERE guild_id = " + guildID);
                while (set.next()) {
                    int id = set.getInt("report_id");
                    String message = set.getString("message");
                    String reporterId = set.getString("reporter_id");
                    String reportedId = set.getString("reported_id");
                    Timestamp time = set.getTimestamp("time");
                    ReportStatus status = ReportStatus.get(set.getInt("status"));

                    reports.add(new Report(guildID, id, message, reporterId, reportedId, time, status));
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return reports;
    }

    public Report getReport(String guildID, int id) {
        final Report[] report = new Report[1];
        try {
            SQLController.runSqlTask(conn -> {
                ResultSet set = conn.createStatement().executeQuery("SELECT * FROM reports WHERE guild_id = " + guildID + ", report_id = " + id);
                String message = set.getString("message");
                String reporterId = set.getString("reporter_id");
                String reportedId = set.getString("reported_id");
                Timestamp time = set.getTimestamp("time");
                ReportStatus status = ReportStatus.get(set.getInt("status"));

                report[0] = new Report(guildID, id, message, reporterId, reportedId, time, status);
            });
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return report[0];
    }

    public List<Report> getReportsToSave() {
        return reportsToSave;
    }

    private static ReportManager instance;

    public static ReportManager getInstance() {
        if (instance == null) {
            instance = new ReportManager();
        }
        return instance;
    }
}
