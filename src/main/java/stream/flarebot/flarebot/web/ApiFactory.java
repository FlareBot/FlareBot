package stream.flarebot.flarebot.web;

import com.google.gson.JsonObject;
import spark.Spark;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.util.general.GeneralUtils;

@SuppressWarnings("Duplicates")
public class ApiFactory {

    public static void bind() {
        Spark.get("/data/:provider", (request, response) -> {
            response.header("Content-Type", "application/json");
            try {
                DataProviders provider;
                try {
                    provider = DataProviders.valueOf(request.params("provider").toUpperCase());
                } catch (Exception e) {
                    response.status(404);
                    JsonObject error = new JsonObject();
                    error.addProperty("error", "Unknown provider: '" + request.params("provider") + "'");
                    return error.toString();
                }
                return provider.process(request, response);
            } catch (Exception e) {
                response.status(500);
                JsonObject error = new JsonObject();
                error.addProperty("error", GeneralUtils.getStackTrace(e));
                FlareBot.LOGGER.error("Error on API!", e);
                return error.toString();
            }
        });
        Spark.post("/setters/:setter", (request, response) -> {
            response.header("Content-Type", "application/json");
            try {
                DataSetters setters;
                try {
                    setters = DataSetters.valueOf(request.params("setter").toUpperCase());
                } catch (Exception e) {
                    response.status(404);
                    JsonObject error = new JsonObject();
                    error.addProperty("error", "Unknown setter: '" + request.params("setter") + "'");
                    return error.toString();
                }
                return setters.process(request, response);
            } catch (Exception e) {
                response.status(500);
                JsonObject error = new JsonObject();
                error.addProperty("error", GeneralUtils.getStackTrace(e));
                FlareBot.LOGGER.error("Error on API!", e);
                return error.toString();
            }
        });
    }
}
