package stream.flarebot.flarebot.util;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import stream.flarebot.flarebot.FlareBot;

import java.io.IOException;

public class WebUtils {

    private static final Callback defaultCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            FlareBot.LOGGER.error("Error for " + call.request().method() + " request to " + call.request().url(), e);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            FlareBot.LOGGER.debug("Reponse for " + call.request().method() + " request to " + call.request().url());
        }
    };

    public static Response post(String url, MediaType type, String body) throws IOException {
        Request.Builder request = new Request.Builder().url(url);
        RequestBody requestBody = RequestBody.create(type, body);
        request = request.post(requestBody);
        return post(request);
    }

    public static Response post(Request.Builder builder) throws IOException {
        return FlareBot.getOkHttpClient().newCall(builder.build()).execute();
    }

    public static Response get(String url) throws IOException {
        return get(new Request.Builder().url(url));
    }

    public static Response get(Request.Builder builder) throws IOException {
        return FlareBot.getOkHttpClient().newCall(builder.get().build()).execute();
    }

    public static void postAsync(Request.Builder builder) {
        FlareBot.getOkHttpClient().newCall(builder.build()).enqueue(defaultCallback);
    }

    public static int getShards(String token) throws IOException {
        Request.Builder request = new Request.Builder()
                .url("https://discordapp.com/api/gateway/bot")
                .header("Authorization", "Bot " + token);
        Response response = get(request);
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        String jsonString = response.body().string();
        return new JSONObject(jsonString).getInt("shards");
    }


    public enum BodyTypes {
        PLAIN_TEXT("plain/text"),
        APPLICATION_JSON("application/json");

        private String type;

        BodyTypes(String type) {
            this.type = type;
        }

        public MediaType getType(){
            return MediaType.parse(getRawType());
        }

        public String getRawType() {
            return this.type;
        }
    }

}
