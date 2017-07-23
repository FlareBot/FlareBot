package stream.flarebot.flarebot.util;

import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.database.SQLController;
import stream.flarebot.flarebot.objects.Report;
import stream.flarebot.flarebot.objects.ReportStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public final class ReportManager {

    private ReportManager() {
    }

    public List<Report> getGuildReports(String guildID) {
        List<Report> reports = FlareBotManager.getInstance().getGuild(guildID).getReports();
        try {
            SQLController.runSqlTask(conn -> {
                ResultSet set = conn.createStatement().executeQuery("SELECT * FROM reports WHERE guild_id = " + guildID + " ORDER BY time ASC");
                while (set.next()) {
                    if (reports.stream().filter(r -> {
                        try {
                            return r.getReportedId().equals(set.getString("reported_id"));
                        } catch (SQLException e) {
                            // TODO: FIX
                            return false;
                        }
                    }).collect(Collectors.toList()).size() != 0) {
                        continue;
                    }
                    String message = set.getString("message");
                    String reporterId = set.getString("reporter_id");
                    String reportedId = set.getString("reported_id");
                    Timestamp time = set.getTimestamp("time");
                    ReportStatus status = ReportStatus.get(set.getInt("status"));

                    reports.add(new Report(guildID, this.getLastId(guildID), message, reporterId, reportedId, time, status));
                }
            });
        } catch (SQLException e) {
            FlareBot.LOGGER.error(e.getMessage(), e);
            return new ArrayList<>();
        }
        Collections.sort(reports);
        FlareBotManager.getInstance().getGuild(guildID).setReports(reports);
        return reports;
    }

    public Report getReport(String guildID, int id) {
        List<Report> reports = getGuildReports(guildID);
        try {
            return reports.get(id - 1);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public void report(String guildID, Report report) {
        List<Report> reports = FlareBotManager.getInstance().getGuild(guildID).getReports();
        reports.add(report);
    }

    private static ReportManager instance;

    public static ReportManager getInstance() {
        if (instance == null) {
            instance = new ReportManager();
        }
        return instance;
    }

    public int getLastId(String guildID) {
        List<Report> reports = FlareBotManager.getInstance().getGuild(guildID).getReports();
        if (!reports.isEmpty()) {
            Collections.sort(reports);
            return reports.get(reports.size() - 1).getId() + 1;
        } else {
            return 1;
        }
    }
}
