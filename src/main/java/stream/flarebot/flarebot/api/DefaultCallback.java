package stream.flarebot.flarebot.api;

import okhttp3.Call;
import okhttp3.Response;
import stream.flarebot.flarebot.FlareBot;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;

public class DefaultCallback implements Callback {

    @Override
    @ParametersAreNonnullByDefault
    public void onResponse(Call call, Response response) throws IOException {
        FlareBot.LOGGER.trace("[" + response.code() + "] - " + call.request().url().toString()
                .replaceFirst(FlareBot.FLAREBOT_API, ""));
        response.close();
    }
}
