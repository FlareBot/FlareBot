package com.bwfcwalshy.flarebot.music.extractors;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public interface Extractor {
    Class<? extends AudioSourceManager> getSourceManagerClass();

    void process(String input, Player player, IMessage message, IUser user) throws Exception;

    boolean valid(String input);
}
