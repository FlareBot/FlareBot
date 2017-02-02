package com.bwfcwalshy.flarebot.web;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.web.objects.Songs;
import com.google.gson.JsonObject;
import spark.Request;
import spark.Response;
import spark.Route;

public enum DataProviders {
    SONGS((req, res) -> Songs.get()),
    GETPERMISSIONS((request, response) -> FlareBot.getInstance()
            .getPermissions(FlareBot.getInstance()
                    .getChannelByID(request.queryParams("guildid"))),
            new Require("guildid", gid -> FlareBot.getInstance().getGuildByID(gid) != null));

    private Route consumer;
    private Require[] requires;

    DataProviders(Route o, Require... requires) {
        consumer = o;
        this.requires = requires;
    }

    @SuppressWarnings("Duplicates")
    public String process(Request request, Response response) throws Exception {
        for (Require require : requires) {
            if (!require.verify(request)) {
                response.status(400);
                JsonObject error = new JsonObject();
                error.addProperty("error", String.format("Require for '%s' failed!", require.getName()));
                return error.toString();
            }
        }
        return FlareBot.GSON.toJson(consumer.handle(request, response));
    }
}
