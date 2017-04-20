package stream.flarebot.flarebot.commands.general;

import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;

public class JoinCommand implements Command {

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (member.getVoiceState().inVoiceChannel()) {
            if (channel.getGuild().getAudioManager().isAttemptingToConnect()) {
                channel.sendMessage("Currently connecting to a voice channel! Try again soon!").queue();
                return;
            }
            if (channel.getGuild().getSelfMember().getVoiceState().inVoiceChannel() &&
                    !channel.getGuild().getSelfMember().getVoiceState().equals(member.getVoiceState()) &&
                    !FlareBot.getInstance().getPermissions(channel).hasPermission(member, "flarebot.join.other")) {
                channel.sendMessage(new EmbedBuilder().setColor(Color.red).setDescription("You need the permission `flarebot.join.other` for me to join your voice channel while I'm in one!")
                        .build()).queue();
                return;
            }
            try {
                channel.getGuild().getAudioManager().openAudioConnection(member.getVoiceState().getChannel());
            } catch (Exception e) {
                channel.sendMessage("Error: `" + e.getMessage() + "`").queue();
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
    public CommandType getType() {
        return CommandType.GENERAL;
    }
}
