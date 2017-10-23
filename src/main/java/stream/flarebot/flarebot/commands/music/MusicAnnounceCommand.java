package stream.flarebot.flarebot.commands.music;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.database.CassandraController;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class MusicAnnounceCommand implements Command {

    private static final Pattern ARGS_PATTERN = Pattern.compile("(here)|(off)", Pattern.CASE_INSENSITIVE);
    private static Map<String, String> announcements = new ConcurrentHashMap<>();

    //TODO: Do
    static {
        CassandraController.runTask(session -> {
            session.execute("CREATE TABLE IF NOT EXISTS flarebot.announces (" +
                    "guild_id varchar PRIMARY KEY," +
                    "channel_id varchar)");
            ResultSet set = session.execute("SELECT * FROM flarebot.announces");
            Row row;
            while ((row = set.one()) != null) {
                announcements.put(row.getString("guild_id"), row.getString("channel_id"));
            }
        });
    }

    public static Map<String, String> getAnnouncements() {
        return announcements;
    }

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 1 && ARGS_PATTERN.matcher(args[0]).matches()) {
            if (args[0].equalsIgnoreCase("here")) {
                announcements.put(channel.getGuild().getId(), channel.getId());
                channel.sendMessage(MessageUtils.getEmbed(sender)
                        .setDescription("Set music announcements to appear in " + channel
                                .getAsMention()).build()).queue();
                CassandraController.runTask(session -> session.executeAsync(session.prepare("UPDATE flarebot.announces SET " +
                        "channel_id = ? WHERE guild_id = ?").bind()
                        .setString(0, channel.getId()).setString(1, channel.getGuild().getId())));
            } else {
                announcements.remove(channel.getGuild().getId());
                channel.sendMessage(MessageUtils.getEmbed(sender)
                        .setDescription(String
                                .format("Disabled announcements for `%s`", channel.getGuild()
                                        .getName()))
                        .build()).queue();
                CassandraController.runTask(session -> session.executeAsync(session.prepare("DELETE FROM flarebot.announces " +
                        "WHERE guild_id = ?").bind().setString(0, channel.getGuild().getId())));
            }
        } else {
            MessageUtils.sendUsage(this, channel, sender, args);
        }
    }

    @Override
    public String getCommand() {
        return "announce";
    }

    @Override
    public String getDescription() {
        return "Announces a track start in a text channel.";
    }

    @Override
    public String getUsage() {
        return "`{%}announce here|off` - Sets the music announce channel or turns it off";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }

    @Override
    public String getPermission() {
        return "flarebot.songannounce";
    }

    @Override
    public boolean isDefaultPermission() {
        return false;
    }
}
