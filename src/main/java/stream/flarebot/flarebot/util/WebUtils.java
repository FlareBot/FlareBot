package stream.flarebot.flarebot.util;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import stream.flarebot.flarebot.FlareBot;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

/**
 * A utility class to provide easy ways to make web requests.
 */
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

    /**
     * Sends a post request to the specified
     *
     * @param url  The URL to send the post request to.
     * @param type The {@code MediaType} to treat the body as.
     * @param body The body to send with the request.
     * @return The response from the request.
     * @throws IOException if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout. Because networks can fail during an exchange, it is possible that the
     *                     remote server accepted the request before the failure.
     */
    public static Response post(String url, MediaType type, String body) throws IOException {
        Request.Builder request = new Request.Builder().url(url);
        RequestBody requestBody = RequestBody.create(type, body);
        request = request.post(requestBody);
        return request(request);
    }

    /**
     * Sends a request using the provided builder.
     *
     * @param builder The builder to send the request with.
     * @return The response from the request.
     * @throws IOException if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout. Because networks can fail during an exchange, it is possible that the
     *                     remote server accepted the request before the failure.
     * @see Call#execute()
     */
    public static Response request(Request.Builder builder) throws IOException {
        return FlareBot.getOkHttpClient().newCall(builder.build()).execute();
    }

    /**
     * Sends a GET request to the specified URL. This uses an empty builder with nothing other than the
     * URL set.
     *
     * @param url The URL to send the request to.
     * @return The response from the request.
     * @throws IOException if the request could not be executed due to cancellation, a connectivity
     *                     problem or timeout. Because networks can fail during an exchange, it is possible that the
     *                     remote server accepted the request before the failure.
     * @see WebUtils#request(Request.Builder)
     */
    public static Response get(String url) throws IOException {
        return request(new Request.Builder().get().url(url));
    }

    /**
     * Makes an async request using the specified builder. This uses the {@link WebUtils#defaultCallback}
     * as the callback.
     *
     * @param builder The builder for the request.
     */
    public static void asyncRequest(Request.Builder builder) {
        FlareBot.getOkHttpClient().newCall(builder.build()).enqueue(defaultCallback);
    }

    /**
     * Pings the specified host on port 80 by attempting to open a web socket to that port.
     *
     * @param host    The host to ping.
     * @param timeout The timeout after which the connection is classes as unsuccessful.
     * @return Whether the ping was successful or not.
     * @see WebUtils#pingHost(String, int, int)
     */
    public static boolean pingHost(String host, int timeout) {
        return pingHost(host, 80, timeout);
    }

    /**
     * Pings a specified host on the provided port by attempting to open a web socket. This will
     * return false if the URL is not correct <b>and</b> if the the socket couldn't connect.
     *
     * @param host    The host to ping.
     * @param port    The port on which to ping the host.
     * @param timeout The timeout after which the connection is classified as unsuccessful.
     * @return Whether the ping was successful or not.
     */
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
