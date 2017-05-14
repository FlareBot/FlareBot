package stream.flarebot.flarebot.util;

import stream.flarebot.flarebot.objects.Report;
import stream.flarebot.flarebot.objects.ReportStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ReportManager {

    public static List<Report> reportsToSave = new ArrayList<>();

    public static List<Report> getGuildReports(String guildID){
        List<Report> reports = new ArrayList<>();
        try {
            SQLController.runSqlTask(conn ->{
                ResultSet set = conn.createStatement().executeQuery("SELECT * FROM reports WHERE guild_id = " + guildID);
                while(set.next()){
                    int id = set.getInt("report_id");
                    String message = set.getString("message");
                    String reporterId = set.getString("reporter_id");
                    String reportedId = set.getString("reported_id");
                    Timestamp time = set.getTimestamp("time");
                    ReportStatus status = ReportStatus.get(set.getInt("status"));

                    Report report = new Report(guildID, id, message, reporterId, reportedId, time, status);
                    reports.add(report);
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return reports;
    }

    public static Report getReport(String guildID, int id){
        final Report[] report = new Report[1];
        try {
            SQLController.runSqlTask(conn ->{
                ResultSet set = conn.createStatement().executeQuery("SELECT * FROM reports WHERE guild_id = " + guildID + ", id = " + id );
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

}
