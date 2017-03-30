package com.bwfcwalshy.flarebot.mod;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AutoModGuild {

    private AutoModConfig config;

    private Map<String, Integer> userPoints = new ConcurrentHashMap<>();

    public AutoModGuild(){
        config = new AutoModConfig();
        for(Action action : Action.values)
            config.getActions().put(action, action.getDefaultPoints());
    }

    public AutoModConfig getConfig() {
        return config;
    }

    public Map<String, Integer> getUserPoints(){
        return this.userPoints;
    }

    public int getPointsForUser(String userId){
        return this.userPoints.getOrDefault(userId, 0);
    }

    public void addPoints(String userId, int points) {
        this.userPoints.put(userId, userPoints.containsKey(userId) ? userPoints.get(userId) + points : points);
    }
}
