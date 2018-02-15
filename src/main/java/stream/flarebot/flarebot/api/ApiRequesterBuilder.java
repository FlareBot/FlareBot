package stream.flarebot.flarebot.api;

import okhttp3.MediaType;
import okhttp3.Response;
import org.json.JSONObject;

import java.util.Objects;

public class ApiRequesterBuilder {

    private MediaType type = ApiRequester.JSON;
    private boolean compressed = false;
    private ApiRoute route;
    private String body;
    private boolean async;
    private Callback callback;

    public ApiRequesterBuilder(ApiRoute route) {
        Objects.requireNonNull(route, "URL cannot be null!");
        this.route = route;
    }

    public ApiRequesterBuilder setType(MediaType type) {
        Objects.requireNonNull(type, "MediaType cannot be null!");
        this.type = type;
        return this;
    }

    public ApiRequesterBuilder setCompressed(boolean compressed) {
        this.compressed = compressed;
        return this;
    }

    public ApiRequesterBuilder setBody(JSONObject body) {
        this.setBody(body.toString());
        return this;
    }

    public ApiRequesterBuilder setBody(String body) {
        this.body = body;
        return this;
    }

    public ApiRequesterBuilder setAync(boolean async, Callback callback) {
        this.async = async;
        this.callback = callback != null ? callback : new DefaultCallback();
        return this;
    }

    /**
     * Send the final request. The Response return will be null if it was async due to us not having it yet!
     *
     * @return The Response returned if sync, null otherwise.
     */
    public Response request() {
        if (async) {
            ApiRequester.requestAsync(route, body, callback, type, compressed);
            return null;
        } else
            return ApiRequester.request(route, body, type, compressed);
    }
}
