package stream.flarebot.flarebot.database;

import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.CodecNotFoundException;
import com.datastax.driver.core.exceptions.QueryExecutionException;
import com.datastax.driver.core.exceptions.QueryValidationException;
import com.google.common.util.concurrent.ListenableFuture;
import io.github.binaryoverload.JSONConfig;
import stream.flarebot.flarebot.FlareBot;

import java.io.File;
import java.io.IOException;

public class CassandraController {

    private static Cluster cluster;
    // Cassandra sessions should be kept open, these handle the pooling per node internally.
    private static Session session;

    public void init() {
        Cluster.Builder builder = Cluster.builder().withClusterName("FlareBot Nodes")
                .withCredentials(FlareBot.getUser(), FlareBot.getPass())
                .withPoolingOptions(new PoolingOptions().setConnectionsPerHost(HostDistance.LOCAL, 2, 4).setConnectionsPerHost(HostDistance.REMOTE, 2, 4));
        if(FlareBot.getNodes() != null){
            for(String node: FlareBot.getNodes()){
                builder.addContactPoint(node);
            }
        }
        cluster = builder.build();
        session = cluster.connect();
    }

    public static void runTask(CassandraTask task) {
        try {
            task.execute(session);
        } catch (QueryExecutionException | QueryValidationException e) {
            FlareBot.LOGGER.error("Failed to execute Cassandra query", e);
        }
    }

    public static void runUnsafeTask(CassandraTask task) throws QueryExecutionException, QueryValidationException {
        task.execute(session);
    }

    public static ResultSet execute(String query) {
        return session.execute(query);
    }

    public static ResultSetFuture executeAsync(String query) {
        return session.executeAsync(query);
    }

    public static PreparedStatement prepare(String query) {
        return session.prepare(query);
    }

    public static ListenableFuture<PreparedStatement> prepareAsync(String query) {
        return session.prepareAsync(query);
    }


    public void close() {
        session.close();
        cluster.close();
    }
}
