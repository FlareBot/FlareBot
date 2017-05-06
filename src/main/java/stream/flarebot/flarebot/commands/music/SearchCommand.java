package stream.flarebot.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.PlayerManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.MessageUtils;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.music.VideoThread;

import java.awt.*;

public class SearchCommand implements Command {

    private PlayerManager musicManager;

    public SearchCommand(FlareBot bot) {
        this.musicManager = bot.getMusicManager();
    }

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        channel.sendMessage(new EmbedBuilder().setColor(Color.YELLOW).setDescription("This is deprecated! Please use _play instead!").build());
        if (args.length > 0) {
            if (args[0].startsWith("http") || args[0].startsWith("www.")) {
                VideoThread.getThread(args[0], channel, sender).start();
            } else {
                String term = FlareBot.getMessage(args, 0);
                VideoThread.getSearchThread(term, channel, sender).start();
            }
        } else {
            MessageUtils.sendUsage(this, channel);
        }
    }

    @Override
    public String getCommand() {
        return "search";
    }

    @Override
    public String getDescription() {
        return "Search for a song on YouTube. Usage: `search URL` or `search WORDS`";
    }

    @Override
    public String getUsage() {
        return "{%}search <URL/words>";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }

}
