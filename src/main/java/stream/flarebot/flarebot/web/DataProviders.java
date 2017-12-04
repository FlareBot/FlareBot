package stream.flarebot.flarebot.web;

import com.google.gson.JsonObject;
import spark.Request;
import spark.Response;
import spark.Route;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.web.objects.Songs;

public enum DataProviders {
    SONGS((req, res) -> Songs.get()),
    GETPERMISSIONS((request, response) -> FlareBotManager.getInstance().getGuild(FlareBot.getInstance().getChannelByID(request.queryParams("guildid")).getGuild().getId())
            .getPermissions(),
            new Require("guildid", gid -> FlareBot.getInstance().getGuildById(gid) != null));

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
