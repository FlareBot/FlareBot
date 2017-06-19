package stream.flarebot.flarebot.errors;

public class YoutubeAccessException extends Exception {

    public YoutubeAccessException() {
        super();
    }

    public YoutubeAccessException(String message) {
        super(message);
    }

    public YoutubeAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public YoutubeAccessException(Throwable cause) {
        super(cause);
    }

}
