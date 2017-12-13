package stream.flarebot.flarebot.util.objects;

import net.dv8tion.jda.core.entities.Emote;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ButtonGroup {
    Map<Emote, Runnable> buttons;
    public ButtonGroup() {
        buttons = new HashMap<>();
    }

    public boolean addButton(Emote emoje, Runnable runnable) {
        if (!buttons.containsKey(emoje)) {
            buttons.put(emoje, runnable);
            return true;
        } else {
            return false;
        }
    }

    public Set<Emote> getButtonEmotes() {
        return buttons.keySet();
    }

    public Runnable getRunnable(Emote emote) {
        return buttons.get(emote);
    }
}
