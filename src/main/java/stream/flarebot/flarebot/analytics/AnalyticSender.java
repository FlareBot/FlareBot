package stream.flarebot.flarebot.analytics;

import org.json.JSONObject;
import stream.flarebot.flarebot.util.Constants;

public interface AnalyticSender {

    /**
     * This is the data which will be sent, it needs to be a JSONObject and have a "data" field with whatever should be
     * sent.
     *
     * <b>Make sure there is a "data" field if sending to FlareBot's API</b>
     *
     * @return The JSONObject to be sent.
     */
    JSONObject processData();

    /**
     * This is how often the analytic data should be sent in milliseconds. It is usually a good idea for readability
     * purposes to sue {@link java.util.concurrent.TimeUnit} and toMillis on any constant.
     *
     * @return The frequency that the data should be sent in milliseconds.
     */
    long dataDeliveryFrequency();

    /**
     * The endpoint which should be to sent to, this needs to have a leading / (slash).
     *
     * @return Endpoint for the sender data to be sent to with a trailing slash.
     */
    String endpoint();

    /**
     * The API URL which we will send the analytic data to, this should not include the endpoint for the said analytic data.
     * By default this is the FlareBot API as defined in {@link Constants#FLAREBOT_API} with the endpoint "/analytics".
     *
     * @return API URL which the data will be sent to (Without endpoint).
     */
    default String apiUrl() {
        return Constants.FLAREBOT_API + "/analytics";
    }
}
