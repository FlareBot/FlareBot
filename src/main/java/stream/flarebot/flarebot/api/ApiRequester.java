package stream.flarebot.flarebot.api;

import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.flarebot.flarebot.FlareBot;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.zip.Deflater;

public class ApiRequester {

    private static final Logger logger = LoggerFactory.getLogger(ApiRequester.class);

    private static OkHttpClient client = new OkHttpClient.Builder().connectionPool(new ConnectionPool(4, 10, TimeUnit.SECONDS)).build();

    public static final MediaType JSON = MediaType.parse("application/json");

    public static Response request(ApiRoute route) {
        return request(route, null, JSON, false);
    }

    public static Response request(ApiRoute route, JSONObject object) {
        return request(route, object.toString(), JSON, false);
    }

    public static void requestAsync(ApiRoute route, JSONObject object) {
        requestAsync(route, object.toString(), null, JSON, false);
    }

    public static void requestAsync(ApiRoute route, Callback callback) {
        requestAsync(route, null, callback, JSON, false);
    }

    /* Root methods */
    public static Response request(ApiRoute route, String body, MediaType type, boolean compressed) {
        if (FlareBot.getInstance().isApiDisabled()) return null;
        try {
            FlareBot.LOGGER.trace("Sending request with route '" + route.getRoute() + "'. Body: " + (body != null ?
                    body : false));
            return client.newCall(getRequest(route, body, type, compressed)).execute();
        } catch (IOException e) {
            FlareBot.LOGGER.error("Failed to request route " + route.getRoute(), e);
            return null;
        }
    }

    public static void requestAsync(ApiRoute route, String body, Callback callback, MediaType type, boolean compressed) {
        if (FlareBot.getInstance().isApiDisabled()) return;
        FlareBot.LOGGER.trace("Sending async request with route '" + route.getRoute() + "'. Body: " + (body != null ?
                body: false));
        if (callback == null) callback = new DefaultCallback();
        client.newCall(getRequest(route, body, type, compressed)).enqueue(callback);
    }

    private static Request getRequest(ApiRoute route, String bodyContent, MediaType type, boolean compressed) {
        Request.Builder request = new Request.Builder().url(route.getFullUrl());
        request.addHeader("Authorization", FlareBot.getInstance().getApiKey());
        request.addHeader("User-Agent", "Mozilla/5.0 FlareBot");

        RequestBody body = RequestBody.create(JSON, bodyContent);
        if(compressed && !bodyContent.isEmpty()) {
            request.addHeader("Content-Encoding", "gzip");
            logger.debug("Starting compression: " + System.currentTimeMillis());
            byte[] output = new byte[100];
            Deflater deflater = new Deflater();
            deflater.setInput(bodyContent.getBytes());
            deflater.finish();
            deflater.deflate(output);
            deflater.end();
            logger.debug("Finished compression: " + System.currentTimeMillis());

            body = RequestBody.create(type, output);
        }

        logger.debug("Sending " + route.getRoute() + ", Type: " + type.toString() + ", Compressed: " + compressed);

        switch (route.getMethod()) {
            case GET:
                request = request.get();
                break;
            case PATCH:
                request = request.patch(body);
                break;
            case POST:
                request = request.post(body);
                break;
            case PUT:
                request = request.put(body);
                break;
            case DELETE:
                request = request.delete(body);
                break;
            default:
                throw new IllegalArgumentException("The route " + route.name() + " is using an unsupported method! Method: "
                        + route.getMethod().name());
        }
        return request.build();
    }
}
