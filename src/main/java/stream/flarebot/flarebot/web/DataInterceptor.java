package stream.flarebot.flarebot.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Interceptor;
import okhttp3.Response;
import javax.annotation.ParametersAreNonnullByDefault;


public class DataInterceptor implements Interceptor {
    
    private static final List<DataInterceptor> interceptors = new ArrayList<>();

    private final AtomicInteger requests = new AtomicInteger(0);
    
    private RequestSender sender;

    public DataInterceptor(RequestSender sender) {
        instance = this;
        this.sender = sender;
        
        interceptors.add(this);
    }

    @Override
    @ParametersAreNonnullByDefault
    public Response intercept(Chain chain) throws IOException {
        requests.incrementAndGet();
        return chain.proceed(chain.request());
    }

    public AtomicInteger getRequests() {
        return requests;
    }
    
    public RequestSender getSender() {
        return this.sender;
    }
    
    public static List<DataInterceptors> getInterceptors() {
        return interceptors;
    }
    
    public static enum RequestSender {
        JDA,
        FLAREBOT
    }
}
