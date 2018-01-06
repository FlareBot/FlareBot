package stream.flarebot.flarebot.commands.music;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import java.util.regex.Pattern;

public class MusicAnnounceCommand implements Command {

    private static final Pattern ARGS_PATTERN = Pattern.compile("(here)|(off)", Pattern.CASE_INSENSITIVE);

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 1 && ARGS_PATTERN.matcher(args[0]).matches()) {
            if (args[0].equalsIgnoreCase("here")) {
                guild.setMusicAnnounceChannelId(channel.getId());
                channel.sendMessage(MessageUtils.getEmbed(sender)
                        .setDescription("Set music announcements to appear in " + channel
                                .getAsMention()).build()).queue();
            } else {
                guild.setMusicAnnounceChannelId(null);
                channel.sendMessage(MessageUtils.getEmbed(sender)
                        .setDescription(String
                                .format("Disabled announcements for `%s`", channel.getGuild()
                                        .getName()))
                        .build()).queue();
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
        return "`{%}announce here|off` - Sets the music announce channel or turns it off.";
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
