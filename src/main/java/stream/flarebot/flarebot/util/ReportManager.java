package stream.flarebot.flarebot.util;

import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.objects.Report;
import stream.flarebot.flarebot.objects.ReportStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public final class ReportManager {

    private ReportManager() {
    }

    private Map<String, List<Report>> reports = new HashMap<>();

    public List<Report> getGuildReports(String guildID) {
        List<Report> reports = this.reports.getOrDefault(guildID, new ArrayList<>());
        try {
            SQLController.runSqlTask(conn -> {
                ResultSet set = conn.createStatement().executeQuery("SELECT * FROM reports WHERE guild_id = " + guildID);
                while (set.next()) {
                    if (reports.stream().filter(r -> {
                        try {
                            return r.getId() == set.getInt("id");
                        } catch (SQLException e) {
                            // TODO: FIX
                            return false;
                        }
                    }).collect(Collectors.toList()).size() != 0) {
                        continue;
                    }
                    int id = set.getInt("id");
                    String message = set.getString("message");
                    String reporterId = set.getString("reporter_id");
                    String reportedId = set.getString("reported_id");
                    Timestamp time = set.getTimestamp("time");
                    ReportStatus status = ReportStatus.get(set.getInt("status"));

                    reports.add(new Report(guildID, id, message, reporterId, reportedId, time, status));
                }
            });
        } catch (SQLException e) {
            // TODO: Fix
            FlareBot.LOGGER.error(ExceptionUtils.getStackTrace(e));
            return new ArrayList<>();
        }
        Collections.sort(reports);
        this.reports.put(guildID, reports);
        return reports;
    }

    public Report getReport(String guildID, int id) {
        List<Report> reports = this.reports.getOrDefault(guildID, new ArrayList<>());

        final Report[] report = new Report[1];
        try {
            SQLController.runSqlTask(conn -> {
                ResultSet set = conn.createStatement().executeQuery("SELECT * FROM reports WHERE guild_id = " + guildID + " AND id = " + id);
                if (set.next()) {
                    if (reports.stream().filter(r -> {
                        try {
                            return r.getId() == set.getInt("id");
                        } catch (SQLException e) {
                            return false;
                        }
                    }).collect(Collectors.toList()).size() != 0) {
                        return;
                    }
                    String message = set.getString("message");
                    String reporterId = set.getString("reporter_id");
                    String reportedId = set.getString("reported_id");
                    Timestamp time = set.getTimestamp("time");
                    ReportStatus status = ReportStatus.get(set.getInt("status"));

                    report[0] = new Report(guildID, id, message, reporterId, reportedId, time, status);
                }
            });
        } catch (SQLException e) {
            FlareBot.LOGGER.error(e.getMessage(), e);
            return this.getReportLocal(id, guildID);
        }
        if (report[0] != null) {
            reports.add(report[0]);
        } else if (this.getReportLocal(id, guildID) != null) {
            reports.add(this.getReportLocal(id, guildID));
        }
        this.reports.put(guildID, reports);
        return report[0] == null ? this.getReportLocal(id, guildID) : report[0];
    }

    public Report getReportLocal(int id, String guildId) {
        if (this.reports.containsKey(guildId)) {
            List<Report> reports = this.reports.get(guildId).stream().filter(r -> r.getId() == id).collect(Collectors.toList());
            if (reports.size() >= 1) {
                return reports.get(0);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public void report(String guildID, Report report) {
        List<Report> reports = this.reports.getOrDefault(guildID, new ArrayList<>());
        reports.add(report);
        this.reports.put(guildID, reports);
    }

    public List<Report> getAllReports() {
        List<Report> reports = new ArrayList<>();
        for (Map.Entry<String, List<Report>> entry : this.reports.entrySet()) {
            reports.addAll(entry.getValue());
        }
        return reports;
    }

    private static ReportManager instance;

    public static ReportManager getInstance() {
        if (instance == null) {
            instance = new ReportManager();
        }
        return instance;
    }

    public int getLastId() {
        if (!getAllReports().isEmpty()) {
            List<Report> reports = getAllReports();
            Collections.sort(reports);
            return reports.get(reports.size() - 1).getId() + 1;
        } else {
            return 1;
        }
    }
}
