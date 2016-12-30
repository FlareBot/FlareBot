package com.bwfcwalshy.flarebot.util;

import com.bwfcwalshy.flarebot.FlareBot;
import sx.blah.discord.handle.obj.IUser;

/**
 * <br>
 * Created by Arsen on 25.9.16..
 */
public class Parser {

    public static IUser mention(String mention) {
        mention = mention.replace("<@", "").replace("!", "").replace(">", "");
        return FlareBot.getInstance().getClient().getUserByID(mention);
    }
}
