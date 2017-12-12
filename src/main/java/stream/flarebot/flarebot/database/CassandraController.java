package stream.flarebot.flarebot.database;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.QueryExecutionException;
import com.datastax.driver.core.exceptions.QueryValidationException;
import com.google.common.util.concurrent.ListenableFuture;
import io.github.binaryoverload.JSONConfig;
import stream.flarebot.flarebot.FlareBot;

public class CassandraController {

    private static Cluster cluster;
    // Cassandra sessions should be kept open, these handle the pooling per node internally.
    private static Session session;

    private CassandraController() {
    }

    public CassandraController(JSONConfig config) {
        Cluster.Builder builder = Cluster.builder().withClusterName("FlareBot Nodes")
                .withCredentials(config.getString("cassandra.username").get(), config.getString("cassandra.password").get())
                .withPoolingOptions(new PoolingOptions().setConnectionsPerHost(HostDistance.LOCAL, 2, 4).setConnectionsPerHost(HostDistance.REMOTE, 2, 4));
        config.getArray("cassandra.nodes").ifPresent(array -> array.forEach(ip -> builder.addContactPoint(ip.getAsString())));
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
        try {
            return session.execute(query);
        } catch (QueryExecutionException | QueryValidationException e) {
            FlareBot.LOGGER.error("Failed to execute Cassandra query", e);
            return null;
        }
    }

    public static ResultSetFuture executeAsync(String query) {
        return session.executeAsync(query);
    }

    public static ResultSetFuture executeAsync(BoundStatement statement) {
        return session.executeAsync(statement);
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
