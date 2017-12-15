package stream.flarebot.flarebot.util.objects;

import stream.flarebot.flarebot.util.ButtonRunnable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ButtonGroup {
    private Map<String, ButtonRunnable> buttons;
    public ButtonGroup() {
        buttons = new LinkedHashMap<>();
    }

    public boolean addButton(String unicode, ButtonRunnable runnable) {
        if (!buttons.containsKey(unicode)) {
            buttons.put(unicode, runnable);
            return true;
        } else {
            return false;
        }
    }

    public Set<String> getButtonEmotes() {
        return buttons.keySet();
    }

    public ButtonRunnable getRunnable(String unicode) {
        return buttons.get(unicode);
    }
}
