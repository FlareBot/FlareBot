package stream.flarebot.flarebot.api;

import okhttp3.Call;
import okhttp3.Response;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.util.Constants;

import javax.annotation.ParametersAreNonnullByDefault;

public class DefaultCallback implements Callback {

    @Override
    @ParametersAreNonnullByDefault
    public void onResponse(Call call, Response response) {
        FlareBot.LOGGER.trace("[" + response.code() + "] - " + call.request().url().toString()
                .replaceFirst(Constants.getAPI(), ""));
        response.close();
    }
}
