package stream.flarebot.flarebot.music.extractors;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

public interface Extractor {

    Class<? extends AudioSourceManager> getSourceManagerClass();

    void process(String input, Player player, Message message, User user) throws Exception;

    boolean valid(String input);

    default AudioSourceManager newSourceManagerInstance() throws Exception {
        return getSourceManagerClass().newInstance();
    }
}
