package stream.flarebot.flarebot.util.objects;

import net.dv8tion.jda.core.entities.Emote;
import stream.flarebot.flarebot.util.ButtonRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ButtonGroup {
    Map<Emote, ButtonRunnable> buttons;
    public ButtonGroup() {
        buttons = new HashMap<>();
    }

    public boolean addButton(Emote emoje, ButtonRunnable runnable) {
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

    public ButtonRunnable getRunnable(Emote emote) {
        return buttons.get(emote);
    }
}
