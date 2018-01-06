package stream.flarebot.flarebot.web;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import spark.Request;
import spark.Response;
import spark.Route;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.Getters;
import stream.flarebot.flarebot.database.CassandraController;
import stream.flarebot.flarebot.web.objects.MonthlyPlaylist;

import java.util.Arrays;

public enum DataSetters {
    ADDPERMISSION((request, response) -> FlareBotManager.instance().getGuild(Getters.getChannelById(request.queryParams("guildid")).getGuild().getId())
            .getPermissions().getGroup(request.queryParams("group")).addPermission(request.queryParams("permission")),
            new Require("guildid", gid -> Getters.getGuildById(gid) != null),
            new Require("group"),
            new Require("permission")),
    REMOVEPERMISSION((request, response) -> FlareBotManager.instance().getGuild(Getters.getChannelById(request.queryParams("guildid")).getGuild().getId())
            .getPermissions().getGroup(request.queryParams("group"))
            .removePermission(request.queryParams("permission")),
            new Require("guildid", gid -> Getters.getGuildById(gid) != null),
            new Require("group"),
            new Require("permission")),
    MONTHLYPLAYLIST((request, response) -> {
        MonthlyPlaylist playlist = FlareBot.GSON.fromJson(request.body(), MonthlyPlaylist.class);
        CassandraController.runTask(session -> {
            session.execute("DELETE FROM playlist WHERE guild = '691337'");
            session.execute(session.prepare("INSERT INTO playlist (playlist_name, guild, list, scope) VALUES (?, ?, ?, 'global')").bind()
                    .setString(0, playlist.name)
                    .setString(1, "691337")
                    .setList(2, Arrays.asList(playlist.playlist)));
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
