package stream.flarebot.flarebot.commands.general;

import com.arsenarsen.lavaplayerbridge.player.Player;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import java.util.Iterator;

public class FixCommand implements Command {


    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        for (Member member1 : guild.getGuild().getMembers()) {
            Iterator<String> iterator = guild.getAutoAssignRoles().iterator();
            while (iterator.hasNext()) {
                Role role = guild.getGuild().getRoleById(iterator.next());
                if (role == null) {
                    iterator.remove();
                } else {
                    if (!member1.getRoles().contains(role)) {
                        guild.getGuild().getController().addRolesToMember(member1, role).queue();
                    }
                }
            }
        }
        if (guild.isSongnickEnabled()) {
            Player player = FlareBot.getInstance().getMusicManager().getPlayer(guild.getGuildId());
            if (player.getPlaylist().isEmpty()) {
                guild.getGuild().getController().setNickname(guild.getGuild().getSelfMember(), null).queue();
            } else if (player.getPlayingTrack() != null) {
                String str = null;
                if (player.getPlayingTrack() != null) {
                    str = player.getPlayingTrack().getTrack().getInfo().title;
                    if (str.length() > 32)
                        str = str.substring(0, 32);
                    str = str.substring(0, str.lastIndexOf(' ') + 1);
                } // Even I couldn't make this a one-liner
                guild.getGuild().getController()
                        .setNickname(guild.getGuild().getSelfMember(), str)
                        .queue();
            }
        } else {
            guild.getGuild().getController().setNickname(guild.getGuild().getSelfMember(), null).queue();
        }
    }

    @Override
    public String getCommand() {
        return "fix";
    }

    @Override
    public String getDescription() {
        return "A command to fix common errors caused by downtime or crash";
    }

    @Override
    public String getUsage() {
        return "`{%}fix` - Fixes common issues";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }
}
