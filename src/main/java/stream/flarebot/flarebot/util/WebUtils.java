package stream.flarebot.flarebot.util;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONObject;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.api.GzipRequestInterceptor;
import stream.flarebot.flarebot.web.DataInterceptor;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class WebUtils {
    
    private static final DataInterceptor dataInterceptor = new DataInterceptor(DataInterceptor.RequestSender.FLAREBOT);
    private static final OkHttpClient client =
            new OkHttpClient.Builder().connectionPool(new ConnectionPool(4, 10, TimeUnit.SECONDS))
                    .addInterceptor(dataInterceptor).addInterceptor(new GzipRequestInterceptor()).build();

    public static MediaType APPLICATION_JSON = MediaType.parse("application/json");

    private static final Callback defaultCallback = new Callback() {
        @Override
        @ParametersAreNonnullByDefault
        public void onFailure(Call call, IOException e) {
            FlareBot.LOGGER.error("Error for " + call.request().method() + " request to " + call.request().url(), e);
        }

        @Override
        @ParametersAreNonnullByDefault
        public void onResponse(Call call, Response response) {
            response.close();
            FlareBot.LOGGER.debug("Response for " + call.request().method() + " request to " + call.request().url());
        }
    };

    public static Response post(String url, MediaType type, String body) throws IOException {
        return post(url, type, body, false);
    }

    public static Response post(String url, MediaType type, String body, boolean sendAPIAuth) throws IOException {
        return post(url, type, body, sendAPIAuth, false);
    }
    public static Response post(String url, MediaType type, String body, boolean sendAPIAuth,
                                boolean compress) throws IOException {
        Request.Builder request = new Request.Builder().url(url);
        if (sendAPIAuth)
            request.addHeader("Authorization", FlareBot.getInstance().getApiKey());
        if (compress)
            request.addHeader("Content-Encoding", "gzip");
        RequestBody requestBody = RequestBody.create(type, body);
        request = request.post(requestBody);
        return post(request);
    }

    public static Response post(Request.Builder builder) throws IOException {
        Response res = client.newCall(builder.build()).execute();
        ResponseBody body = res.body();
        if(res.code() >= 200 && res.code() < 300)
            return res;
        else
            throw new IllegalStateException("Failed to POST to '" + builder.build().url() + "'! Code: " + res.code()
                    + ", Message: " + res.message() + ", Body: " + (body != null ? body.string().replace("\n", "")
                    .replace("\t", " ").replaceAll(" +", " ") : "null"));
    }

    public static Response get(String url) throws IOException {
        return get(new Request.Builder().url(url));
    }

    public static Response get(Request.Builder builder) throws IOException {
        return client.newCall(builder.get().build()).execute();
    }

    public static void postAsync(Request.Builder builder) {
        client.newCall(builder.build()).enqueue(defaultCallback);
    }

    public static boolean pingHost(String host, int timeout) {
        return pingHost(host, 80, timeout);
    }

    private static boolean pingHost(String host, int port, int timeout) {
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
