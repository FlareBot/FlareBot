package com.bwfcwalshy.flarebot.commands;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.objects.Poll;
import com.bwfcwalshy.flarebot.util.SQLController;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

import javax.xml.soap.Text;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class FlareBotManager {

    private static FlareBotManager instance;

    private List<String> loadedSongs = new ArrayList<>();
    private Random rand = new Random();

    private Map<String, Poll> polls = new HashMap<>();

    private FlareBot flareBot;

    public FlareBotManager(FlareBot bot) {
        instance = this;
        this.flareBot = bot;
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

    public void executeCreations(){
        try {
            SQLController.runSqlTask(conn -> {
                conn.createStatement().execute("CREATE TABLE IF NOT EXISTS playlist (\n" +
                        "  playlist_name  VARCHAR(60),\n" +
                        "  guild VARCHAR(20),\n" +
                        "  owner VARCHAR(20),\n" +
                        "  list  TEXT,\n" +
                        "  scope  VARCHAR(7) DEFAULT 'local',\n" +
                        "  PRIMARY KEY(name, guild)\n" +
                        ")");
            });
        } catch (SQLException e) {
            FlareBot.LOGGER.error("Database error!", e);
        }
    }

    public void savePlaylist(TextChannel channel, String owner, String name, String list) {
        try {
            SQLController.runSqlTask(connection -> {
                PreparedStatement exists = connection.prepareStatement("SELECT * FROM playlist WHERE playlist_name = ? AND guild = ?");
                exists.setString(1, name);
                exists.setString(2, channel.getGuild().getId());
                exists.execute();
                if (exists.getResultSet().isBeforeFirst()) {
                    channel.sendMessage("That name is already taken!").queue();
                    return;
                }
                PreparedStatement statement = connection.prepareStatement("INSERT INTO playlist (playlist_name, guild, owner, list) VALUES (" +
                        "   ?," +
                        "   ?," +
                        "   ?," +
                        "   ?" +
                        ")");
                statement.setString(1, name);
                statement.setString(2, channel.getGuild().getId());
                statement.setString(3, owner);
                statement.setString(4, list);
                statement.executeUpdate();
                channel.sendMessage(MessageUtils.getEmbed(FlareBot.getInstance().getUserByID(owner)).setDescription("Success!").build()).queue();
            });
        } catch (SQLException e) {
            flareBot.reportError(channel, "The playlist could not be saved! " + e.getMessage());
            FlareBot.LOGGER.error("Database error!", e);
        }
    }
}
