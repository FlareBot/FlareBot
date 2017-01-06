package com.bwfcwalshy.flarebot.web;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.web.objects.Songs;
import spark.Request;
import spark.Response;

import java.util.function.BiFunction;

public enum DataProviders {
    SONGS((req, res) -> Songs.get()),
    GETPERMISSIONS((request, response) -> FlareBot.getInstance()
            .getPermissions(FlareBot.getInstance()
                    .getClient().getChannelByID(request.queryParams("guildid"))),
            new Require("guildid", gid -> FlareBot.getInstance().getClient().getGuildByID(gid) != null));

    private BiFunction<Request, Response, Object> consumer;
    private Require[] requires;

    DataProviders(BiFunction<Request, Response, Object> o, Require... requires) {
        consumer = o;
        this.requires = requires;
    }

    public String process(Request request, Response response) {
        for (Require require : requires) {
            if (!require.verify(request)) {
                response.status(400);
                return String.format("Require for <code>%s</code> failed!", require.getName());
            }

        }
        return FlareBot.GSON.toJson(consumer.apply(request, response));
    }
}
