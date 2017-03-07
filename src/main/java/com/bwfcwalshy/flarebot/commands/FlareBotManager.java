package com.bwfcwalshy.flarebot.commands;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.objects.Poll;
import com.bwfcwalshy.flarebot.util.SQLController;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class FlareBotManager {

    private static FlareBotManager instance;

    private List<String> loadedSongs = new ArrayList<>();
    private Random rand = new Random();

    private Map<String, Poll> polls = new HashMap<>();

    public FlareBotManager() {
        instance = this;
    }

    public static FlareBotManager getInstance() {
        return instance;
    }

    public void loadRandomSongs() {
        try {
            SQLController.runSqlTask(conn -> {
                conn.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS random_songs (video_id VARCHAR(12) PRIMARY KEY);");
                ResultSet set = conn.createStatement().executeQuery("SELECT * FROM random_songs;");
                while (set.next()) {
                    loadedSongs.add(set.getString("video_id"));
                }
            });
        } catch (SQLException e) {
            FlareBot.LOGGER.error("Could not load songs!", e);
        }
    }

    public Set<String> getRandomSongs(int amount, TextChannel channel) {
        Set<String> songs = new HashSet<>();
        if (amount < 10 || amount > 100) {
            MessageUtils.sendErrorMessage(MessageUtils.getEmbed()
                    .setDescription("Invalid amount. Make sure it is 10 or more and 100 or less!"), channel);
            return null;
        }

        for (int i = 0; i < amount; i++) {
            songs.add(loadedSongs.get(rand.nextInt(loadedSongs.size())));
        }
        return songs;
    }

    public Map<String, Poll> getPolls(){
        return this.polls;
    }

    public Poll getPollFromGuild(Guild guild){
        return this.polls.get(guild.getId());
    }
}
