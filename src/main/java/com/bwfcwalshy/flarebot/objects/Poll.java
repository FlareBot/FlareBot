package com.bwfcwalshy.flarebot.objects;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Poll {

    private String question;
    private List<PollOption> pollOptions;
    private boolean open;
    private LocalDateTime endTime;
    private Color pollColor;

    public Poll(String question) {
        this.question = question;
        pollOptions = new ArrayList<>();
        endTime = LocalDateTime.now().plusMinutes(1);
    }

    public String getQuestion(){
        return this.question;
    }

    public List<PollOption> getPollOptions(){
        return this.pollOptions;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public boolean isOpen(){
        return this.open;
    }

    public void setOpen(boolean open){
        this.open = open;
    }

    public void setColor(Color color){
        this.pollColor = color;
    }

    public Color getColor() {
        return pollColor;
    }
}
