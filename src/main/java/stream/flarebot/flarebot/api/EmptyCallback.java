package stream.flarebot.flarebot.api;

import okhttp3.Call;
import okhttp3.Response;

import java.io.IOException;

public class EmptyCallback implements Callback {

    @Override
    public void onResponse(Call call, Response response) throws IOException {
    }
}
