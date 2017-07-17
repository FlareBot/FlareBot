package stream.flarebot.flarebot.commands.music;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.database.SQLController;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * <br>
 * Created by Arsen on 29.10.16..
 */
public class PlaylistsCommand implements Command {
    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("mark")) {
                if (FlareBot.getInstance().getPermissions(channel).isCreator(sender)) {
                    if (args.length == 1 || args.length == 2) {
                        MessageUtils.getUsage(this, channel, sender).queue();
                    } else if (args.length == 2) {
                        MessageUtils.sendErrorMessage("Usage: " + FlareBot.getPrefix(channel.getGuild().getId()) +
                                "", channel);
                    } else if (args.length >= 3) {
                        String playlist = "";
                        for (int i = 2; i < args.length; i++) playlist += args[i] + ' ';
                        playlist = playlist.trim();
                        try {
                            String finalPlaylist = playlist;
                            SQLController.runSqlTask(conn -> {
                                PreparedStatement statement = conn.prepareStatement("SELECT * FROM playlist WHERE " +
                                        "guild = ? AND playlist_name = ?");
                                statement.setString(1, channel.getGuild().getId());
                                statement.setString(2, finalPlaylist);
                                statement.execute();
                                if (statement.getResultSet().next()) {
                                    if (args[1].equalsIgnoreCase("global") || args[1].equalsIgnoreCase("local")) {
                                        PreparedStatement statement1 = conn.prepareStatement("UPDATE playlist SET " +
                                                "scope = ? WHERE guild = ? AND playlist_name = ?");
                                        statement1.setString(1, args[1].toLowerCase());
                                        statement1.setString(2, channel.getGuild().getId());
                                        statement1.setString(3, finalPlaylist);
                                        statement1.execute();
                                        channel.sendMessage(MessageUtils.getEmbed()
                                                .setDescription("Changed the scope of '" + finalPlaylist + "' to "
                                                        + args[1].toLowerCase()).build())
                                                .queue();
                                    } else {
                                        MessageUtils.sendErrorMessage("Invalid scope! Scopes are local and global!",
                                                channel);
                                    }
                                } else {
                                    MessageUtils.sendErrorMessage("That playlist could not be found!", channel);
                                }
                            });
                        } catch (SQLException e) {
                            FlareBot.LOGGER.error("Error changing scope!", e);
                        }
                    }
                }
            }
        } else {

            channel.sendTyping().complete();
            try {
                SQLController.runSqlTask(connection -> {
                    PreparedStatement get = connection
                            .prepareStatement("SELECT playlist_name, scope FROM playlist WHERE guild = ? OR scope = 'global' ORDER BY scope ASC");
                    get.setString(1, channel.getGuild().getId());
                    get.execute();
                    ResultSet set = get.getResultSet();
                    StringBuilder sb = new StringBuilder();
                    StringBuilder globalSb = new StringBuilder();
                    List<String> songs = new ArrayList<>();
                    int i = 1;
                    boolean loopingGlobal = true;
                    while (set.next() && songs.size() < 25) {
                        String toAppend;
                        if (set.getString("scope").equalsIgnoreCase("global")) {
                            toAppend = String.format("%s. %s\n", i++, set.getString("playlist_name"));
                            globalSb.append(toAppend);
                        } else {
                            if (loopingGlobal) {
                                loopingGlobal = false;
                                i = 1;
                            }
                            toAppend = String.format("%s. %s\n", i++, set.getString("playlist_name"));
                            if (sb.length() + toAppend.length() > 1024) {
                                songs.add(sb.toString());
                                sb = new StringBuilder();
                            }
                            sb.append(toAppend);
                        }
                    }
                    songs.add(sb.toString());
                    EmbedBuilder builder = MessageUtils.getEmbed(sender);
                    i = 1;
                    builder.addField("Global Playlists", (globalSb.toString()
                            .isEmpty() ? "No global playlists!" : globalSb
                            .toString()), false);
                    for (String s : songs) {
                        builder.addField("Page " + i++, s.isEmpty() ? "**No playlists!**" : s, false);
                    }
                    channel.sendMessage(builder.build()).queue();
                });
            } catch (SQLException e) {
                FlareBot.reportError(channel, "Unable to load playlists!", e);
            }
        }
    }

    @Override
    public String getCommand() {
        return "playlists";
    }

    @Override
    public String getDescription() {
        return "Lists all playlists saved in the current guild";
    }

    @Override
    public String getUsage() {
        return "{%}playlists mark <global/local> <playlist>";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }
}
