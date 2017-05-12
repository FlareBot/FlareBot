package stream.flarebot.flarebot.util;

import org.joda.time.DateTime;
import stream.flarebot.flarebot.objects.Report;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportManager {

    private static List<Report> reportsToSave = new ArrayList<>();

    public List<Report> getGuildReports(long guildID){
        List<Report> reports = new ArrayList<>();
        try {
            SQLController.runSqlTask(conn ->{
                ResultSet set = conn.createStatement().executeQuery("SELECT * FROM reports WHERE guild_id = " + guildID);
                while(set.next()){
                    int id = set.getInt("report_id");
                    String message = set.getString("message");
                    long reporterId = set.getLong("reporter_id");
                    long reportedId = set.getLong("reported_id");
                    DateTime time = new DateTime(set.getTimestamp("time").getTime());
                    boolean solved = set.getBoolean("solved");

                    Report report = new Report(guildID, id, message, reporterId, reportedId, time, solved);
                    reports.add(report);
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return reports;
    }

    public Report getReport(long guildID, int id){
        List<Report> theReport = new ArrayList<>(); //Probably not the best way to do it but I couldn't think of anything else
        try {
            SQLController.runSqlTask(conn ->{
                ResultSet set = conn.createStatement().executeQuery("SELECT * FROM reports WHERE guild_id = " + guildID + ", report_id = " + id );
                String message = set.getString("message");
                long reporterId = set.getLong("reporter_id");
                long reportedId = set.getLong("reported_id");
                DateTime time = new DateTime(set.getTimestamp("time").getTime());
                boolean solved = set.getBoolean("solved");

                theReport.add(new Report(guildID, id, message, reporterId, reportedId, time, solved));

            });
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return theReport.get(0);
    }
}
