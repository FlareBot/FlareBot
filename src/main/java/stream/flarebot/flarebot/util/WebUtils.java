package stream.flarebot.flarebot.util;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONObject;
import stream.flarebot.flarebot.FlareBot;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

public class WebUtils {

    public static MediaType PLAIN_TEXT = MediaType.parse("plain/text");
    public static MediaType APPLICATION_JSON = MediaType.parse("application/json");

    private static final Callback defaultCallback = new Callback() {
        @Override
        @ParametersAreNonnullByDefault
        public void onFailure(Call call, IOException e) {
            FlareBot.LOGGER.error("Error for " + call.request().method() + " request to " + call.request().url(), e);
        }

        @Override
        @ParametersAreNonnullByDefault
        public void onResponse(Call call, Response response) throws IOException {
            response.close();
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

    public static boolean pingHost(String host, int timeout) {
        return pingHost(host, 80, timeout);
    }

    public static boolean pingHost(String host, int port, int timeout) {
        String hostname;
        try {
            hostname = new URL(host).getHost();
        } catch (MalformedURLException e) {
            return false;
        }
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(hostname, port), timeout);
            return true;
        } catch (IOException e) {
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
    }

}
