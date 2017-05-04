package com.bwfcwalshy.flarebot.mod.reports;

import com.bwfcwalshy.flarebot.scheduler.FlarebotTask;

public class ReportsTracker {
    public ReportsTracker(){
        new FlarebotTask("ReportsTracker"){
            @Override
            public void run(){

            }
        }.repeat(60_000, 60_000);
    }
}
