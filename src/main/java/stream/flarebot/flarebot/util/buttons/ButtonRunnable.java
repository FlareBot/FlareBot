package stream.flarebot.flarebot.util.buttons;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

public interface ButtonRunnable {

    void run(User user, Message message);
}
