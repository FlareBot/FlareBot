package com.bwfcwalshy.flarebot.mod;

import org.eclipse.jetty.util.ConcurrentHashSet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AutoModConfig {

    //TODO: Disable by default. Enabled for testing
    private boolean enabled = true;

    private String modLogChannel;
    private Map<Action, Integer> actions = new ConcurrentHashMap<>();
    private Map<Action, ConcurrentHashSet<String>> whitelist = new ConcurrentHashMap<>();
    private int maxMessagesPerSecond = 3;

    public boolean isEnabled(){
        return this.enabled;
    }

    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    public String getModLogChannel(){
        return this.modLogChannel;
    }

    public void setModLogChannel(String modLogChannel){
        this.modLogChannel = modLogChannel;
    }

    public Map<Action, Integer> getActions(){
        return this.actions;
    }

    public ConcurrentHashSet<String> getWhitelist(Action action){
        return this.whitelist.get(action);
    }

    public int getMaxMessagesPerSecond(){
        return this.maxMessagesPerSecond;
    }
}
