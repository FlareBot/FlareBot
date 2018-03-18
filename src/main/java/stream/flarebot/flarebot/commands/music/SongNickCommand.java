package stream.flarebot.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.player.Track;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.permissions.Permission;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.general.GuildUtils;

public class SongNickCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (guild.isSongnickEnabled()) {
            guild.setSongnick(false);
            if (GuildUtils.canChangeNick(guild.getGuildId()))
                channel.getGuild().getController().setNickname(channel.getGuild().getSelfMember(), null).queue();
            MessageUtils.sendSuccessMessage("Disabled changing nickname with song!", channel, sender);
        } else {
            if (!GuildUtils.canChangeNick(guild.getGuildId())) {
                MessageUtils.sendErrorMessage("FlareBot can't change it's nickname so SongNick hasn't been enabled",
                        channel);
                return;
            }
            guild.setSongnick(true);
            if (FlareBot.instance().getMusicManager().getPlayer(guild.getGuildId()).getPlayingTrack() != null) {
                Track track = FlareBot.instance().getMusicManager().getPlayer(guild.getGuildId()).getPlayingTrack();
                String str = null;
                if (track != null) {
                    str = track.getTrack().getInfo().title;
                    if (str.length() > 32)
                        str = str.substring(0, 32);
                    str = str.substring(0, str.lastIndexOf(' ') + 1);
                } // Even I couldn't make this a one-liner
                guild.getGuild().getController()
                        .setNickname(guild.getGuild().getSelfMember(), str)
                        .queue();
            }
            MessageUtils.sendSuccessMessage("Enabled changing nickname with song!", channel, sender);
        }
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
        return "`{%}songnick` - Toggles nickname auto changing to current song names.";
    }

    @Override
    public Permission getPermission() {
        return Permission.SONGNICK_COMMAND;
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }
}
