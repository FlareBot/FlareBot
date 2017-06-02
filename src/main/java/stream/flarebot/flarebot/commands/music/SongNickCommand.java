package stream.flarebot.flarebot.commands.music;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.SQLController;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SongNickCommand implements Command {
    private static Set<Long> guilds = ConcurrentHashMap.newKeySet();

    static {
        try {
            SQLController.runSqlTask(conn -> {
                conn.createStatement().execute("CREATE TABLE IF NOT EXISTS guilds (id BIGINT UNSIGNED PRIMARY KEY)");
                ResultSet results = conn.createStatement().executeQuery("SELECT * FROM guilds");
                while(results.next())
                    guilds.add(results.getLong("id"));
            });
        } catch (SQLException e) {
            FlareBot.LOGGER.error("Could not load song nickname IDs", e);
        }
    }

    public static Set<Long> getGuilds() {
        return Collections.unmodifiableSet(guilds);
    }

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("true")) {
                SQLController.asyncRunSqlTask(conn ->
                        conn.createStatement().execute("INSERT INTO guilds (id) VALUES (" + channel.getGuild().getId() + ")"));
                guilds.add(channel.getGuild().getIdLong());
                channel.sendMessage(MessageUtils.getEmbed(sender).setDescription("Done :-)").build()).queue();
                return;
            } else if (args[0].equalsIgnoreCase("false")) {
                SQLController.asyncRunSqlTask(conn ->
                        conn.createStatement().execute("DELETE FROM guilds WHERE id = " + channel.getGuild().getId()));
                guilds.remove(channel.getGuild().getIdLong());
                channel.sendMessage(MessageUtils.getEmbed(sender).setDescription("Done :-)").build()).queue();
                return;
            }
        }
        MessageUtils.sendUsage(this, channel);
    }

    @Override
    public String getCommand() {
        return "songnick";
    }

    @Override
    public String getDescription() {
        return "Automatically changes my nickname to be the name of the currently playing song";
    }

    @Override
    public String getUsage() {
        return "`{%}songnick true|false` - Turns on/off nickname auto changing to current song names.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }

    @Override
    public String getPermission() {
        return "flarebot.songnick";
    }

    public static void removeGuild(long l) {
        guilds.remove(l);
    }
}
