package stream.flarebot.flarebot.util;

import stream.flarebot.flarebot.FlareBot;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

/**
 * <br>
 * Created by Arsen on 25.9.16..
 */
public class Parser {

    public static User mention(String mention) {
        mention = mention.replace("<@", "").replace("!", "").replace(">", "");
        return FlareBot.getInstance().getUserByID(mention);
    }

    public static Member mention(String arg, Guild guild) {
        User u = mention(arg);
        return u != null ? guild.getMember(u) : null;
    }
}
