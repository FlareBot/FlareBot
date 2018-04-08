package stream.flarebot.flarebot.commands.music;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.permissions.Permission;

public class KillCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (guild.getGuild().getAudioManager().isAttemptingToConnect() || guild.getGuild().getAudioManager().isConnected()) {
            guild.getGuild().getAudioManager().closeAudioConnection();
            FlareBot.instance().getMusicManager().getPlayer(guild.getGuildId()).clean();
        }
        channel.sendMessage("Killed the voice connection, please change voice region and try to use the bot " +
                "again!").queue();
    }

    @Override
    public String getCommand() {
        return "kill";
    }

    @Override
    public String getDescription() {
        return "When Discord has issues then use this and hopefully all will work <3";
    }

    @Override
    public String getUsage() {
        return "`{%}kill` - Kill and reset the state of the player. Good for when Discord has issues.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }

    @Override
    public Permission getPermission() {
        return Permission.KILL_COMMAND;
    }
}
