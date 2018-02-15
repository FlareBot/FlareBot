package stream.flarebot.flarebot.api;

import stream.flarebot.flarebot.util.Constants;

import static stream.flarebot.flarebot.api.Method.GET;
import static stream.flarebot.flarebot.api.Method.PATCH;
import static stream.flarebot.flarebot.api.Method.POST;
import static stream.flarebot.flarebot.api.Method.PUT;

public enum ApiRoute {

    // Root route
    COMMANDS(POST, "/commands"),

    // Stats route
    UPDATE_DATA(POST, "/stats/data"),
    DISPATCH_COMMAND(POST, "/stats/command"),

    // Guild route
    UPDATE_PREFIX(PATCH, "/guild/prefix"),

    // Guild/options route
    GET_OPTIONS_AUTOMODD(GET, "/guild/options/automod"),
    GET_OPTIONS_LANGUAGE(GET, "/guild/options/language"),

    // Debug route
    TEST(POST, "/test"),
    ;

    private Method method;
    private String route;

    ApiRoute(Method method, String route) {
        this.method = method;
        this.route = route;
    }

    public Method getMethod() {
        return this.method;
    }

    public String getRoute() {
        return this.route;
    }

    public String getFullUrl() {
        return Constants.getAPI() + route;
    }
}
