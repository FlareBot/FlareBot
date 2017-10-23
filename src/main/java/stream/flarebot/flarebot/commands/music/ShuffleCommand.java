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

public class ShuffleCommand implements Command {

    private PlayerManager musicManager;

    public ShuffleCommand(FlareBot bot) {
        this.musicManager = bot.getMusicManager();
    }

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        this.musicManager.getPlayer(channel.getGuild().getId()).shuffle();
    }

    @Override
    public String getCommand() {
        return "shuffle";
    }

    @Override
    public String getDescription() {
        return "Shuffle up the order of the songs";
    }

    @Override
    public String getUsage() {
        return "`{%}shuffle` - Shuffles order of the songs";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }

    @Override
    public boolean isDefaultPermission() {
        return true;
    }
}
