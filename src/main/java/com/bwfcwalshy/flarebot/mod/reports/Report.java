package com.bwfcwalshy.flarebot.mod.reports;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

public class Report {
    private User reported;
    private User reporter;
    private int reportId;
    private Guild guild;

    public User getReported() {
        return this.reported;
    }

    public User getReporter() {
        return this.reporter;
    }

    public int getReportId() {
        return this.reportId;
    }

    public Guild getGuild() {
        return this.guild;
    }
}
