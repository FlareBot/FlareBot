package com.bwfcwalshy.flarebot.mod;

import java.util.List;

public class GuildReports {
    private List<Report> reports;

    private int unresolved;
    private int resolved;

    private int total;

    public List<Report> getReports() {
        return this.reports;
    }

    public int getUnresolved() {
        return this.unresolved;
    }

    public int getResolved() {
        return this.resolved;
    }

    public int getTotal() {
        return this.total;
    }
}
