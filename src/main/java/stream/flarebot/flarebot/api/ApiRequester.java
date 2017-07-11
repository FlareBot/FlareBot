package stream.flarebot.flarebot.api;

import com.google.gson.JsonObject;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import stream.flarebot.flarebot.FlareBot;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiRequester {

    private static OkHttpClient client = new OkHttpClient.Builder().connectionPool(new ConnectionPool(4, 10, TimeUnit.SECONDS)).build();

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static Response request(ApiRoute route, JsonObject object) {
        return request(route, object, route.getMethod());
    }

    public static Response request(ApiRoute route) {
        return request(route, null);
    }

    public static Response request(ApiRoute route, JsonObject object, Method method) {
        Request.Builder request = new Request.Builder().url(route.getFullUrl());
        RequestBody body = RequestBody.create(JSON, (object == null ? new JsonObject().toString() : object.toString()));
        if(method == Method.GET) {
            request = request.get();
        } else if(method == Method.PATCH) {
            request = request.patch(body);
        } else if(method == Method.POST) {
            request = request.post(body);
        } else if(method == Method.PUT) {
            request = request.put(body);
        } else if(method == Method.DELETE) {
            request = request.delete(body);
        } else {
            throw new IllegalArgumentException("The route " + route.name() + " is using an unsupported method! Method: " + method.name());
        }
        try {
            FlareBot.LOGGER.debug("Sending request with route '" + route.getRoute() + "'. Body: " + (object != null ? object.toString() : false));
            return client.newCall(request.build()).execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
