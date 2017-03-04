package com.bwfcwalshy.flarebot.mod;

import net.dv8tion.jda.core.entities.Guild;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigurationProvider {
    private static final Map<String, Map<Automod, SeverityLevel>> LEVELS = new ConcurrentHashMap<>();

    public static SeverityLevel getSeverityFor(Guild guild, Automod automod) {
        return LEVELS.computeIfAbsent(guild.getId(), m -> new ConcurrentHashMap<>())
                .getOrDefault(automod, automod.getSeverityLevel());
    }

    public static void setSeverityFor(Guild guild, Automod automod, SeverityLevel severity) {
        LEVELS.computeIfAbsent(guild.getId(), m -> new ConcurrentHashMap<>()).put(automod, severity);
        update(guild, automod);
    }

    private static void update(Guild guild, Automod automod) {

    }
}
