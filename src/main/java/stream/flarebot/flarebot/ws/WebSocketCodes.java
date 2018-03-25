package stream.flarebot.flarebot.ws;

import stream.flarebot.flarebot.util.general.GeneralUtils;

import java.util.Map;

public enum  WebSocketCodes {

    DISPATCH(0),
    HEARTBEAT(1),
    IDENTIFY(2),
    STATUS_UPDATE(3),
    VOICE_STATE_UPDATE(4),
    VOICE_SERVER_PING(5),
    RESUME(6),
    RECONNECT(7),
    REQUEST_GUILD_MEMBERS(8),
    INVALID_SESSION(9),
    HELLO(10),
    HEARTBEAT_ACK(11);

    private int opCode;
    public static final Map<Integer, WebSocketCodes> WEB_SOCKET_CODES = GeneralUtils.getReverseMapping(WebSocketCodes.class, WebSocketCodes::getOpCode);

    WebSocketCodes(int opCode) {
        this.opCode = opCode;
    }

    public int getOpCode() {
        return opCode;
    }

    public enum AudioCodes {

        IDENTIFY(0),
        SELECT_PROTOCOL(1),
        READY(2),
        HEARTBEAT(3),
        SESSION_DESCRIPTION(4),
        SPEAKING(5),
        HEARTBEAT_ACK(6),
        RESUME(7),
        HELLO(8),
        RESUMED(9),
        CLIENT_DISCONNECT(13);

        private int opCode;
        public static final Map<Integer, AudioCodes> AUDIO_SOCKET_CODES = GeneralUtils.getReverseMapping(AudioCodes.class, AudioCodes::getOpCode);

        AudioCodes(int opCode) {
            this.opCode = opCode;
        }

        public int getOpCode() {
            return opCode;
        }
    }

}
