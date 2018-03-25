package stream.flarebot.flarebot.ws;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketListener;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

public class WebSocketFactory extends com.neovisionaries.ws.client.WebSocketFactory {

    private WebSocketListener listener;

    public WebSocketFactory(WebSocketListener listener) {
        this.listener = listener;
    }

    @Override
    public WebSocket createSocket(URI uri) throws IOException {
        WebSocket socket = super.createSocket(uri);
        socket.addListener(this.listener);
        return socket;
    }

    @Override
    public WebSocket createSocket(URL url) throws IOException {
        WebSocket socket = super.createSocket(url);
        socket.addListener(this.listener);
        return socket;
    }

    @Override
    public WebSocket createSocket(String uri) throws IOException {
        WebSocket socket = super.createSocket(uri);
        socket.addListener(this.listener);
        return socket;
    }

    @Override
    public WebSocket createSocket(URI uri, int timeout) throws IOException {
        WebSocket socket = super.createSocket(uri, timeout);
        socket.addListener(this.listener);
        return socket;
    }

    @Override
    public WebSocket createSocket(URL url, int timeout) throws IOException {
        WebSocket socket = super.createSocket(url, timeout);
        socket.addListener(this.listener);
        return socket;
    }

    @Override
    public WebSocket createSocket(String uri, int timeout) throws IOException {
        WebSocket socket = super.createSocket(uri, timeout);
        socket.addListener(this.listener);
        return socket;
    }
}
