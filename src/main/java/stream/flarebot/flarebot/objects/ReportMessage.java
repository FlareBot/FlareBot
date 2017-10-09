package stream.flarebot.flarebot.objects;

import java.sql.Timestamp;

public class ReportMessage {

    private String message;
    private Timestamp time;

    public ReportMessage(String message, Timestamp time) {
        this.message = message;
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getTime() {
        return time;
    }
}
