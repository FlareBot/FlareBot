package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.guilds.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

public class JoinCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (member.getVoiceState().inVoiceChannel()) {
            if (channel.getGuild().getAudioManager().isAttemptingToConnect()) {
                MessageUtils.sendErrorMessage("Currently connecting to a voice channel! Try again soon!", channel);
                return;
            }
            if (channel.getGuild().getSelfMember().getVoiceState().inVoiceChannel() && !channel.getGuild()
                    .getSelfMember()
                    .getVoiceState()
                    .getAudioChannel()
                    .getId()
                    .equals(member
                            .getVoiceState()
                            .getAudioChannel()
                            .getId()) && !getPermissions(channel).hasPermission(member, "flarebot.join.other")) {
                MessageUtils.sendErrorMessage("You need the permission `flarebot.join.other` for me to join your voice channel while I'm in one!", channel);
                return;
            }
            GeneralUtils.joinChannel(channel, member);
        }
    }

    @Override
    public String getCommand() {
        return "join";
    }

    @Override
    public String getDescription() {
        return "Tell me to join your voice channel.";
    }

    @Override
    public String getUsage() {
        return "`{%}join` - Joins FlareBot to join your active voice channel";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"summon"};
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }
}
