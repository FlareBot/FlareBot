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
            String nickname = null;
            if (player.getPlayingTrack() != null) {
                nickname = player.getPlayingTrack().getTrack().getInfo().title;
                if (nickname.length() > 32) {
                    nickname = nickname.substring(0, 32);
                }
                nickname = nickname.substring(0, nickname.lastIndexOf(' ') + 1);
            }
            guild.getGuild().getController()
                    .setNickname(guild.getGuild().getSelfMember(), nickname)
                    .queue();
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
