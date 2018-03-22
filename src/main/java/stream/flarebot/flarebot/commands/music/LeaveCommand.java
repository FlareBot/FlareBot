package stream.flarebot.flarebot.commands.music;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.permissions.Permission;
import stream.flarebot.flarebot.util.MessageUtils;

public class LeaveCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (channel.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
            if ((!member.getVoiceState().inVoiceChannel() || !channel.getGuild().getSelfMember().getVoiceState()
                    .getAudioChannel().getId()
                    .equals(member.getVoiceState().getAudioChannel()
                            .getId())) && !getPermissions(channel)
                    .hasPermission(member, Permission.LEAVE_OTHER)) {
                MessageUtils.sendErrorMessage("You need the permission `" + Permission.LEAVE_OTHER + "` for me to leave a different voice channel!",
                        channel, sender);
                return;
            }
            channel.getGuild().getAudioManager().closeAudioConnection();
            if (FlareBotManager.instance().getLastActive().containsKey(guild.getGuildIdLong()))
                FlareBotManager.instance().getLastActive().remove(guild.getGuildIdLong());
            MessageUtils.sendInfoMessage("Bye bye! I've left the channel for now", channel, sender);
        }
    }

    @Override
    public String getCommand() {
        return "leave";
    }

    @Override
    public String getDescription() {
        return "Tell me to leave the voice channel.";
    }

    @Override
    public String getUsage() {
        return "`{%}leave` - Makes FlareBot leave its current channel.";
    }

    @Override
    public Permission getPermission() {
        return Permission.LEAVE_COMMAND;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"gtfo", "banish", "getout", "quit"};
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }
}
