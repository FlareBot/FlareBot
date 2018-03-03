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
    
    private final RequestSender sender;

    public DataInterceptor(RequestSender sender) {
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
    
    public static List<DataInterceptor> getInterceptors() {
        return interceptors;
    }
    
    public static enum RequestSender {
        
        JDA("JDA"),
        FLAREBOT("FlareBot");
        
        private String name;
        RequestSender(String name) {
            this.name = name;
        }
        
        public String getName() {
            return this.name;
        }
    }
}
