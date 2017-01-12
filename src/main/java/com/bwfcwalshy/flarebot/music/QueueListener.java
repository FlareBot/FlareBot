package com.bwfcwalshy.flarebot.music;

import com.arsenarsen.lavaplayerbridge.hooks.QueueHook;
import com.arsenarsen.lavaplayerbridge.player.Item;
import com.arsenarsen.lavaplayerbridge.player.Player;
import com.arsenarsen.lavaplayerbridge.player.Playlist;
import com.arsenarsen.lavaplayerbridge.player.Track;
import com.bwfcwalshy.flarebot.FlareBot;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;

import java.util.ArrayList;
import java.util.List;

public class QueueListener implements QueueHook {

    @Override
    public void execute(Player player, Item item) {
        List<Track> tracks = new ArrayList<>();
        if(item instanceof Playlist) {
            ((AudioPlaylist) item).getTracks().forEach(track -> tracks.add(new Track(track)));
        }else if(item instanceof Track){
            tracks.add((Track) item);
        }else{
            FlareBot.LOGGER.error("Unsupported item! " + item);
            return;
        }

        sendQueueData(tracks);
    }

    private void sendQueueData(List<Track> tracks){
        JsonArray array = new JsonArray();
        for(Track t : tracks){
            JsonObject o = new JsonObject();
            o.addProperty("title", t.getTrack().getInfo().title);
            o.addProperty("length", t.getTrack().getDuration());
            o.addProperty("id", t.getTrack().getIdentifier());
            o.addProperty("requester", t.getMeta().getOrDefault("requester", "Unknown").toString());
            array.add(o);
        }
        FlareBot.LOGGER.debug(array.toString());

        FlareBot.getInstance().postToApi("updatePlaylists", "playlist", array);
    }
}
