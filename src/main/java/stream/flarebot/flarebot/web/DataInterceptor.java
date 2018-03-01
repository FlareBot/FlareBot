package stream.flarebot.flarebot.web;

import okhttp3.Interceptor;
import okhttp3.Response;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class DataInterceptor implements Interceptor {

    private static DataInterceptor instance;

    private final AtomicInteger requests = new AtomicInteger(0);
    
    private RequestSender sender;

    public DataInterceptor(RequestSender sender) {
        instance = this;
        this.sender = sender;
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

    public AtomicInteger getRequests() {
        return requests;
    }
    
    public RequestSender getSender() {
        return this.sender;
    }
    
    static enum RequestSender {
        JDA,
        FLAREBOT
    }
}
