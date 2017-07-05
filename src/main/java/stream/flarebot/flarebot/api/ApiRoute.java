package stream.flarebot.flarebot.api;

import com.mashape.unirest.http.HttpMethod;
import stream.flarebot.flarebot.FlareBot;

public enum ApiRoute {

    COMMANDS(HttpMethod.POST, "commands"),

    GET_OPTIONS_AUTOMODD(HttpMethod.GET, "guild-options/automod"),
    GET_OPTIONS_LANGUAGE(HttpMethod.GET, "guild-options/language"),

    LOAD_TIME(HttpMethod.PUT, "debug/load-time");

    private HttpMethod method;
    private String route;
    ApiRoute(HttpMethod method, String route){
        this.method = method;
        this.route = route;
    }

    public HttpMethod getMethod(){
        return this.method;
    }

    public String getRoute(){
        return this.route;
    }

    public boolean allowsBody() {
        return (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH || method == HttpMethod.DELETE);
    }

    public String getFullUrl() {
        return FlareBot.FLAREBOT_API + route;
    }
}
