package stream.flarebot.flarebot.api;

import okhttp3.Call;
import stream.flarebot.flarebot.util.Constants;
import stream.flarebot.flarebot.util.MessageUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;

public interface Callback extends okhttp3.Callback {

    @Override
    @ParametersAreNonnullByDefault
    default void onFailure(Call call, IOException e) {
        MessageUtils.sendException("Error on API call! URL: " + call.request().url() + "\nBody: "
                + (call.request().body() != null), e, Constants.getErrorLogChannel());
    }
}
