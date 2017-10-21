package stream.flarebot.flarebot.objects.guilds;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GuildOptions {

    private Map<String, Object> options = new HashMap<>();

    public GuildOptions(){
        validateOptions();
    }

    /**
     * Get the guild options
     *
     * @return options for this specific guild.
     */
    public Map<String, Object> getOptions() {
        return this.options;
    }

    public String getFullPath(String path) {
        Objects.requireNonNull(path, "Cannot have a null path");
        if(options.get(path) != null)
            return path;
        else
            return options.keySet().stream().filter(s -> {
                return s.substring(s.indexOf(".") + 1)
                        .equalsIgnoreCase(path);
            }).findFirst().orElse(null);
    }

    /**
     * Check if a specific option exists, this will work if you use the full path or if you just use the option name.
     *
     * @param option Path of the option
     * @return If the specific option exists.
     */
    public boolean hasOption(String option) {
        return options.containsKey(getFullPath(option));
    }

    /**
     * Set an option for the guild, this requires a string path and any object.
     *
     * @param path Path of the option - This will be something like `category.option` for example `commands.disable-command-message`
     * @param value The value of the path.
     */
    public boolean setOption(String path, Object value) {
        if(get(path) instanceof String && value instanceof String)
            this.options.put(path, value.toString());
        else if(get(path) instanceof Boolean && value instanceof Boolean)
            this.options.put(path, value);
        else if(get(path) instanceof Integer && value instanceof Integer)
            this.options.put(path, value);
        else
            return false;
        return true;
    }

    public void removeOption(String path) {
        if(options.containsKey(path))
            options.remove(path);
    }

    public boolean getBoolean(String path) {
        path = getFullPath(path);
        if(options.get(path) instanceof Boolean)
            return (boolean) options.get(path);
        else
            throw new IllegalStateException("The path `" + path + "` does not return a boolean!");
    }

    public int getInt(String path) {
        path = getFullPath(path);
        if(options.get(path) instanceof Integer)
            return (int) options.get(path);
        else
            throw new IllegalStateException("The path `" + path + "` does not return an integer!");
    }

    public String getString(String path) {
        path = getFullPath(path);
        if(options.get(path) instanceof String)
            return (String) options.get(path);
        else
            throw new IllegalStateException("The path `" + path + "` does not return an string!");
    }

    public Object get(String path) {
        return options.get(getFullPath(path));
    }

    public void validateOptions() {
        if(!hasOption("commands.delete-command-message"))
            setOption("commands.delete-command-message", true);
    }

    /**
     * Check if a value is valid for the option.
     *
     * @param path Path of the option
     * @param value Value which will be validated.
     */
    public boolean isValid(String path, Object value) {
        path = getFullPath(path);

        if(options.get(path) instanceof String) {
            return true;
        } else if (options.get(path) instanceof Boolean) {
            return Boolean.valueOf(String.valueOf(value));
        }
        return false;
    }
}
