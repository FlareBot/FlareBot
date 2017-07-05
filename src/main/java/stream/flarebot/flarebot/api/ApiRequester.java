package stream.flarebot.flarebot.api;

public class ApiRequester {

    public static ApiRequest request(ApiRoute route) {
        return new ApiRequest(route);
    }
}
