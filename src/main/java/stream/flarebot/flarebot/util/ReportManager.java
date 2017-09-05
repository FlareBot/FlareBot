package stream.flarebot.flarebot.util;

import stream.flarebot.flarebot.objects.Report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ReportManager {

    public ReportManager() {
    }

    private List<Report> reports = new ArrayList<>();

    public List<Report> getReports() {
        Collections.sort(reports);
        return reports;
    }

    public Report getReport(int id) {
        return reports.get(id - 1);
    }

    public void report(Report report) {
        reports.add(report);
    }

    private static ReportManager instance;

    public static ReportManager getInstance() {
        if (instance == null) {
            instance = new ReportManager();
        }
        return instance;
    }

    public int getLastId() {
        if (!getReports().isEmpty()) {
            return reports.get(reports.size() - 1).getId() + 1;
        } else {
            return 1;
        }
    }
}
