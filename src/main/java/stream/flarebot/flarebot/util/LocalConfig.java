package stream.flarebot.flarebot.util;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import org.json.JSONObject;
import org.json.JSONTokener;
import stream.flarebot.flarebot.FlareBot;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;
import java.util.stream.Collectors;

public class LocalConfig {

    private final Gson gson = FlareBot.GSON;
    private final JsonParser parser = new JsonParser();

    private JSONObject object;

    public LocalConfig(URL url) {

        if (url == null)
            throw new IllegalArgumentException("URL cannot be null!");

        try (Reader r = new InputStreamReader(url.openStream())) {
            JSONTokener tokener = new JSONTokener(r);
            this.object = new JSONObject(tokener);
        } catch (IOException e) {
            FlareBot.LOGGER.error("There was an error reading the config file!", e);
        }
    }

    public String getString(JSONObject json, String path) {
        String[] subpaths = path.split("\\.");
        for (int i = 0; i < subpaths.length; i++) {
            String subpath = subpaths[i];
            if (json.get(subpath) == null) {
                return null;
            } else if (json.get(subpath) instanceof JSONObject) {
                return getString((JSONObject) this.object.get(subpath), Arrays.stream(subpaths).skip(i + 1).collect(Collectors.joining(".")));
            } else {
                return (String) json.get(subpath);
            }
        }
        return null;
    }

    public String getString(String path) {
        return getString(this.object, path);
    }

    public boolean exists(String s) {
        try {
            return getString(this.object, s) != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public JSONObject getJsonObject() {
        return object;
    }
}
