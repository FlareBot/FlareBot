package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;

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
            if (channel.getGuild().getSelfMember()
                    .hasPermission(member.getVoiceState().getChannel(), Permission.VOICE_CONNECT) &&
                    channel.getGuild().getSelfMember()
                            .hasPermission(member.getVoiceState().getChannel(), Permission.VOICE_SPEAK)) {
                if (member.getVoiceState().getChannel().getUserLimit() > 0 && member.getVoiceState().getChannel()
                        .getMembers().size()
                        >= member.getVoiceState().getChannel().getUserLimit() && !member.getGuild().getSelfMember()
                        .hasPermission(member
                                .getVoiceState()
                                .getChannel(), Permission.MANAGE_CHANNEL)) {
                    MessageUtils.sendErrorMessage("We can't join :(\n\nThe channel user limit has been reached and we don't have the 'Manage Channel' permission to " +
                            "bypass it!", channel);
                    return;
                }
                channel.getGuild().getAudioManager().openAudioConnection(member.getVoiceState().getChannel());
            } else {
                MessageUtils.sendErrorMessage("I do not have permission to " + (!channel.getGuild().getSelfMember()
                        .hasPermission(member.getVoiceState()
                                .getChannel(), Permission.VOICE_CONNECT) ?
                        "connect" : "speak") + " in your voice channel!", channel);
            }
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
