package stream.flarebot.flarebot.commands.music;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.music.VideoThread;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

public class SearchCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        MessageUtils.sendWarningMessage("This command is deprecated and will be fully removed and re-tooled next update! " +
                "**Please use `{%}play` instead!**", channel, sender);
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
