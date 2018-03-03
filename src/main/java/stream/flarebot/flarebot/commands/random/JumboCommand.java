package stream.flarebot.flarebot.commands.random;

import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection

public class JumboCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length > 0) {
            if (!message.getEmotes().isEmpty()) {
                Emote e = message.getEmotes().get(0);
                InputStream stream = read(new URL(e.getImageUrl()), channel);
                if (stream != null)
                    channel.sendFile(stream, e.getName() + ".png").queue();
            } else
                MessageUtils.sendWarningMessage("Please send a valid emote!", channel);
        } else
            MessageUtils.sendUsage(this, channel, sender, args);
    }

    @Override
    public String getCommand() {
        return "jumbo";
    }

    @Override
    public String getDescription() {
        return "Jumbo size an emoji!";
    }

    @Override
    public String getUsage() {
        return "`{%}jumbo <emote>` - See an emote in it's true size because who doesn't love jumbo sized stuff?";
}

    @Override
    public CommandType getType() {
        return CommandType.RANDOM;
    }
    
    private InputStream read(URL url, TextChannel channel) {
        try {
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.addRequestProperty("User-Agent", "Mozilla/5.0 FlareBot");

            return conn.getInputStream();
        } catch (IOException e) {
            MessageUtils.sendErrorMessage("Failed to jumbo image!\nMessage: " + e.getMessage(), channel);
            FlareBot.LOGGER.error("Failed to send image for " + getCommand() + " command. Guild ID: "
                   + guild.getGuild(), e);
            return null;
        }
    }
}
