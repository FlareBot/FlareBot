package stream.flarebot.flarebot.api;

import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import stream.flarebot.flarebot.FlareBot;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiRequester {

    private static OkHttpClient client = new OkHttpClient.Builder().connectionPool(new ConnectionPool(4, 10, TimeUnit.SECONDS)).build();

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static Response request(ApiRoute route) {
        return request(route, null);
    }

    public static Response request(ApiRoute route, JSONObject object) {
        return request(route, object, route.getMethod());
    }

    public static void requestAsync(ApiRoute route, JSONObject object) {
        requestAsync(route, object, route.getMethod(), null);
    }

    public static void requestAsync(ApiRoute route, JSONObject object, Callback callback) {
        requestAsync(route, object, route.getMethod(), callback);
    }

    public static void requestAsync(ApiRoute route, Callback callback) {
        requestAsync(route, null, route.getMethod(), callback);
    }

    /* Root methods */
    public static Response request(ApiRoute route, JSONObject object, Method method) {
        try {
            FlareBot.LOGGER.trace("Sending request with route '" + route.getRoute() + "'. Body: " + (object != null ?
                    object.toString() : false));
            return client.newCall(getRequest(route, object, method)).execute();
        } catch (IOException e) {
            FlareBot.LOGGER.error("Failed to request route " + route.getRoute(), e);
            return null;
        }
    }

    public static void requestAsync(ApiRoute route, JSONObject object, Method method, Callback callback) {
        FlareBot.LOGGER.trace("Sending async request with route '" + route.getRoute() + "'. Body: " + (object != null ?
                object.toString() : false));
        if (callback == null) callback = new DefaultCallback();
        client.newCall(getRequest(route, object, method)).enqueue(callback);
    }

    private static Request getRequest(ApiRoute route, JSONObject object, Method method) {
        Request.Builder request = new Request.Builder().url(route.getFullUrl());
        request.addHeader("Authorization", FlareBot.getInstance().getApiKey());
        request.addHeader("User-Agent", "Mozilla/5.0 FlareBot");
        RequestBody body = RequestBody.create(JSON, (object == null ? new JSONObject().toString() : object.toString()));
        if (method == Method.GET) {
            request = request.get();
        } else if (method == Method.PATCH) {
            request = request.patch(body);
        } else if (method == Method.POST) {
            request = request.post(body);
        } else if (method == Method.PUT) {
            request = request.put(body);
        } else if (method == Method.DELETE) {
            request = request.delete(body);
        } else {
            throw new IllegalArgumentException("The route " + route.name() + " is using an unsupported method! Method: "
                    + method.name());
        }
        return request.build();
    }
}
