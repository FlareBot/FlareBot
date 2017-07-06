package stream.flarebot.flarebot.api;

import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import stream.flarebot.flarebot.FlareBot;

import java.util.concurrent.Future;

public class ApiRequest {

    private ApiRoute route;
    private JSONObject content;

    public ApiRequest(ApiRoute route){
        this.route = route;
    }

    public ApiRequest setBody(JSONObject object){
        if(route.allowsBody())
            this.content = object;
        return this;
    }

    public JsonNode send(){
        FlareBot.LOGGER.debug("Sent request to route '" + route.getRoute() + "'");
        try {
            if(route.getMethod() == HttpMethod.POST)
                return Unirest.post(route.getFullUrl()).body(content).asJson().getBody();
            else if(route.getMethod() == HttpMethod.PUT)
                return Unirest.put(route.getFullUrl()).body(content).asJson().getBody();
            else if(route.getMethod() == HttpMethod.PATCH)
                return Unirest.put(route.getFullUrl()).body(content).asJson().getBody();
            else if(route.getMethod() == HttpMethod.GET)
                return Unirest.get(route.getFullUrl()).asJson().getBody();
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Future<HttpResponse<JsonNode>> sendAsync(){
        FlareBot.LOGGER.debug("Sent request to route '" + route.getRoute() + "'");
        if(route.getMethod() == HttpMethod.POST)
            return Unirest.post(route.getFullUrl()).body(content).asJsonAsync();
        else if(route.getMethod() == HttpMethod.PUT)
            return Unirest.put(route.getFullUrl()).body(content).asJsonAsync();
        else if(route.getMethod() == HttpMethod.PATCH)
            return Unirest.put(route.getFullUrl()).body(content).asJsonAsync();
        else if(route.getMethod() == HttpMethod.GET)
            return Unirest.get(route.getFullUrl()).asJsonAsync();
        return null;
    }
}
