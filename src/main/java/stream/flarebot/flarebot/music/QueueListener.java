package stream.flarebot.flarebot.music;

import com.arsenarsen.lavaplayerbridge.hooks.QueueHook;
import com.arsenarsen.lavaplayerbridge.player.Item;
import com.arsenarsen.lavaplayerbridge.player.Player;
import com.arsenarsen.lavaplayerbridge.player.Playlist;
import com.arsenarsen.lavaplayerbridge.player.Track;
import stream.flarebot.flarebot.FlareBot;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class QueueListener implements QueueHook {

    @Override
    public void execute(Player player, Item item) {
        List<Track> tracks = new ArrayList<>();
        if (item instanceof Playlist) {
            ((Playlist) item).getPlaylist().forEach(tracks::add);
        } else {
            tracks.add((Track) item);
        }

        sendQueueData(tracks);
    }

    private void sendQueueData(List<Track> tracks) {
        JsonArray array = new JsonArray();
        for (Track t : tracks) {
            JsonObject object = new JsonObject();
            object.addProperty("title", t.getTrack().getInfo().title);
            object.addProperty("id", t.getTrack().getIdentifier());
            array.add(object);
        }

        FlareBot.getInstance().postToApi("updatePlaylistData", "playlist", array);
    }
}
