package stream.flarebot.flarebot.objects.guilds;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GuildOptions {

    private Map<String, Object> options = new HashMap<>();

    /**
     * Get the guild options
     *
     * @return options for this specific guild.
     */
    public Map<String, Object> getOptions() {
        return this.options;
    }

    /**
     * Check if a specific option exists.
     *
     * @param path Path of the option
     * @return If the specific option exists.
     */
    public boolean hasOption(String path) {
        return options.containsKey(path);
    }

    public void setOption(String path, Object option) {
        this.options.put(path, option);
    }

    public void removeOption(String path) {
        if(options.containsKey(path))
            options.remove(path);
    }

    public boolean getBoolean(String path) {
        Objects.requireNonNull(options.get(path), "That path does not exist or it returns null");
        if(options.get(path) instanceof Boolean)
            return (boolean) options.get(path);
        else
            throw new IllegalStateException("The path `" + path + "` does not return a boolean!");
    }
}
