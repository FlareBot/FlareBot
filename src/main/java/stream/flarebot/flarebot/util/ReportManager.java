package stream.flarebot.flarebot.util;

import stream.flarebot.flarebot.objects.Report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ReportManager {

    private List<Report> reports = new ArrayList<>();

    public List<Report> getReports() {
        Collections.sort(reports);
        return reports;
    }

    public Report getReport(int id) {
        if (reports.size() >= id)
            return reports.get(id - 1);
        return null;
    }

    public void report(Report report) {
        reports.add(report);
    }

    public int getLastId() {
        if (!getReports().isEmpty()) {
            return reports.size();
        } else {
            return 0;
        }
    }
}
