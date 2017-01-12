package com.bwfcwalshy.flarebot.music;

import com.arsenarsen.lavaplayerbridge.hooks.QueueHook;
import com.arsenarsen.lavaplayerbridge.player.Item;
import com.arsenarsen.lavaplayerbridge.player.Player;
import com.arsenarsen.lavaplayerbridge.player.Track;
import com.bwfcwalshy.flarebot.FlareBot;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public class QueueListener implements QueueHook {

    @Override
    public void execute(Player player, Item item) {
        // Recode that thing. It can only ever be Track or Playlist lol
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
        System.out.println(array.toString());

        FlareBot.getInstance().postToApi("updatePlaylists", "playlist", array);
    }
}
