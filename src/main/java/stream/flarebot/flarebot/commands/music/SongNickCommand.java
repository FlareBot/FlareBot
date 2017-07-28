package stream.flarebot.flarebot.commands.music;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;

public class SongNickCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (guild.isSongnickEnabled()) {
            guild.setSongnickEnabled(false);
            channel.getGuild().getController().setNickname(channel.getGuild().getSelfMember(), null).queue();
            channel.sendMessage(MessageUtils.getEmbed(sender).setDescription("Disabled changing nickname with song!").setColor(Color.RED).build()).queue();
            return;
        } else {
            guild.setSongnickEnabled(true);
            channel.sendMessage(MessageUtils.getEmbed(sender).setDescription("Enabled changing nickname with song!").setColor(Color.GREEN).build()).queue();
            return;
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
    public CommandType getType() {
        return CommandType.MUSIC;
    }

    @Override
    public String getPermission() {
        return "flarebot.songnick";
    }

}
