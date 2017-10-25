package stream.flarebot.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.PlayerManager;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;

public class StopCommand implements Command {

    private PlayerManager musicManager;

    public StopCommand(FlareBot bot) {
        this.musicManager = bot.getMusicManager();
    }

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        musicManager.getPlayer(channel.getGuild().getId()).stop();
    }

    @Override
    public String getCommand() {
        return "stop";
    }

    @Override
    public String getDescription() {
        return "Stops your playlist.";
    }

    @Override
    public String getUsage() {
        return "`{%}stop` - Stops the current playlist.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }

    @Override
    public String getPermission() {
        return "flarebot.stop";
    }

    @Override
    public boolean isDefaultPermission() {
        return false;
    }
}
