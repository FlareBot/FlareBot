package stream.flarebot.flarebot.ws;

import com.neovisionaries.ws.client.ThreadType;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;
import net.dv8tion.jda.core.WebSocketCode;
import org.json.JSONObject;
import stream.flarebot.flarebot.metrics.Metrics;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebSocketListener implements com.neovisionaries.ws.client.WebSocketListener {

    public static HashMap<Integer, String> websocketCodes = new HashMap<>();

    static {
        for (Field field : WebSocketCode.class.getFields()) {
            try {
                websocketCodes.put((Integer) field.get(null), field.getName());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        websocketCodes.put(5, "VOICE_SERVER_PING");
    }

    @Override
    public void onStateChanged(WebSocket webSocket, WebSocketState webSocketState) throws Exception {
    }

    @Override
    public void onConnected(WebSocket webSocket, Map<String, List<String>> map) throws Exception {
    }

    @Override
    public void onConnectError(WebSocket webSocket, WebSocketException e) throws Exception {
    }

    @Override
    public void onDisconnected(WebSocket webSocket, WebSocketFrame webSocketFrame, WebSocketFrame webSocketFrame1, boolean b) throws Exception {
    }

    @Override
    public void onFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {
    }

    @Override
    public void onContinuationFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {
    }

    @Override
    public void onTextFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {
    }

    @Override
    public void onBinaryFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {
    }

    @Override
    public void onCloseFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {
    }

    @Override
    public void onPingFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {
    }

    @Override
    public void onPongFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {
    }

    @Override
    public void onTextMessage(WebSocket webSocket, String s) throws Exception {
    }

    @Override
    public void onBinaryMessage(WebSocket webSocket, byte[] bytes) throws Exception {
    }

    @Override
    public void onSendingFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {
    }

    @Override
    public void onFrameSent(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {
        int opCode = new JSONObject(webSocketFrame.getPayloadText()).getInt("op");
        String type = Thread.currentThread().getName().contains("AudioWS") ? "voice" : "web";
        String name = type.equals("voice") ?
                WebSocketCodes.AudioCodes.AUDIO_SOCKET_CODES.get(opCode).name() :
                WebSocketCodes.WEB_SOCKET_CODES.get(opCode).name();
        Metrics.websocketEvents.labels(
                String.valueOf(opCode),
                name,
                type
        ).inc();
    }

    @Override
    public void onFrameUnsent(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {
    }

    @Override
    public void onThreadCreated(WebSocket webSocket, ThreadType threadType, Thread thread) throws Exception {
    }

    @Override
    public void onThreadStarted(WebSocket webSocket, ThreadType threadType, Thread thread) throws Exception {
    }

    @Override
    public void onThreadStopping(WebSocket webSocket, ThreadType threadType, Thread thread) throws Exception {
    }

    @Override
    public void onError(WebSocket webSocket, WebSocketException e) throws Exception {
    }

    @Override
    public void onFrameError(WebSocket webSocket, WebSocketException e, WebSocketFrame webSocketFrame) throws Exception {
    }

    @Override
    public void onMessageError(WebSocket webSocket, WebSocketException e, List<WebSocketFrame> list) throws Exception {
    }

    @Override
    public void onMessageDecompressionError(WebSocket webSocket, WebSocketException e, byte[] bytes) throws Exception {
    }

    @Override
    public void onTextMessageError(WebSocket webSocket, WebSocketException e, byte[] bytes) throws Exception {
    }

    @Override
    public void onSendError(WebSocket webSocket, WebSocketException e, WebSocketFrame webSocketFrame) throws Exception {
    }

    @Override
    public void onUnexpectedError(WebSocket webSocket, WebSocketException e) throws Exception {
    }

    @Override
    public void handleCallbackError(WebSocket webSocket, Throwable throwable) throws Exception {
    }

    @Override
    public void onSendingHandshake(WebSocket webSocket, String s, List<String[]> list) throws Exception {
    }
}
