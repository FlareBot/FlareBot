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

    /**
     * Adds a button to the button group.
     *
     * @param unicode The unicode string for this emojie.
     * @param runnable A ButtonRunnable that is called when the button is clicked.
     * @return If adding the button was successful.
     */
    public boolean addButton(String unicode, ButtonRunnable runnable) {
        if (!buttons.containsKey(unicode)) {
            buttons.put(unicode, runnable);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets all the emojies in this button group.
     *
     * @return a Set<String> containg all the emojies in this button group.
     */
    public Set<String> getButtonEmotes() {
        return buttons.keySet();
    }

    /**
     * Gets the runnable associated with this button.
     *
     * @param unicode The unicode of the button.
     * @return The ButtonRunnable for this button.
     */
    public ButtonRunnable getRunnable(String unicode) {
        return buttons.get(unicode);
    }
}
