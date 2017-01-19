package com.bwfcwalshy.flarebot.web;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.util.SQLController;
import com.bwfcwalshy.flarebot.web.objects.MonthlyPlaylist;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.stream.Collectors;

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
            new Require("permission")),
    MONTHLYPLAYLIST((request, response) -> {
        MonthlyPlaylist playlist = FlareBot.GSON.fromJson(request.body(), MonthlyPlaylist.class);
        SQLController.runSqlTask(connection -> {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO playlist (name, guild, list) VALUES (" +
                    "   ?," +
                    "   ?," +
                    "   ?" +
                    ")");
            statement.setString(1, playlist.name);
            statement.setString(2, "226785954537406464");
            statement.setString(3, Arrays.stream(playlist.playlist).collect(Collectors.joining(",")));
            statement.executeUpdate();
        });
        return true;
    },
            new BodyRequire(e -> e.isJsonPrimitive() && ((JsonPrimitive) e).isString(), "name"),
            new BodyRequire(JsonElement::isJsonArray, "playlist"));

    private Route consumer;
    private Require[] requires;

    DataSetters(Route o, Require... requires) {
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
