package stream.flarebot.flarebot.web;

import okhttp3.Interceptor;
import okhttp3.Response;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class DataInterceptor implements Interceptor {

    private static DataInterceptor instance;

    private static final AtomicInteger REQUESTS = new AtomicInteger(0);

    public DataInterceptor() {
        instance = this;
    }

    @Override
    @ParametersAreNonnullByDefault
    public Response intercept(Chain chain) throws IOException {
        REQUESTS.incrementAndGet();
        return chain.proceed(chain.request());
    }

    public static DataInterceptor getInstance() {
        return instance;
    }

    public static AtomicInteger getRequests() {
        return REQUESTS;
    }
}
