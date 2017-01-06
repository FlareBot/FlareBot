package com.bwfcwalshy.flarebot.web;

import com.bwfcwalshy.flarebot.FlareBot;
import spark.Request;
import spark.Response;

import java.util.function.BiFunction;

public enum DataSetters {
    ADDPERMISSION((request, response) -> FlareBot.getInstance()
            .getPermissions(FlareBot.getInstance().getClient().getChannelByID(request.queryParams("guildid")))
            .addPermission(request.queryParams("group"), request.queryParams("permission")),
            new Require("guildid", gid -> FlareBot.getInstance().getClient().getGuildByID(gid) != null),
            new Require("group"),
            new Require("permission")),
    REMOVEPERMISSION((request, response) -> FlareBot.getInstance()
            .getPermissions(FlareBot.getInstance().getClient().getChannelByID(request.queryParams("guildid")))
            .removePermission(request.queryParams("group"), request.queryParams("permission")),
            new Require("guildid", gid -> FlareBot.getInstance().getClient().getGuildByID(gid) != null),
            new Require("group"),
            new Require("permission"));

    private BiFunction<Request, Response, Object> consumer;
    private Require[] requires;

    DataSetters(BiFunction<Request, Response, Object> o, Require... requires) {
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
