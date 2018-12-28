package cat.nyaa.nyaacore.database.relational;

import org.apache.commons.lang.Validate;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("rawtypes")
public abstract class BaseDatabase implements RelationalDB {
    protected Set<Class> createdTableClasses = new HashSet<>();

    protected int maxPoolSize = 20;
    protected final Queue<Connection> connectionPool = new ArrayBlockingQueue<>(maxPoolSize);
    protected final List<Connection> usedConnections = new CopyOnWriteArrayList<>();
    protected int minPoolSize = 5;
    protected double validPoolChance = 0.05;
    protected final Logger logger;
    protected final Consumer<Runnable> executor;

    protected BaseDatabase(Logger logger, Consumer<Runnable> executor) {
        this.logger = logger;
        this.executor = executor;
    }

    @Override
    public Connection newConnection() {
        Connection connection = connectionPool.poll();
        try {
            for (; connection != null && !connection.isValid(1); connection = connectionPool.poll()) {
                connection.close();
            }
        } catch (SQLException e) {
            getLogger().log(Level.INFO, "Bad connection found", e);
        }
        if (connection == null) {
            connection = createConnection();
        }
        usedConnections.add(connection);
        if (ThreadLocalRandom.current().nextDouble() < validPoolChance) {
            executeAsync(this::validPool);
        }
        return connection;
    }

    protected void fillPool() {
        int filled = 0;
        while (connectionPool.size() < minPoolSize) {
            connectionPool.offer(createConnection());
            ++filled;
        }
        logger.log(Level.FINE, "fillPool: " + filled + " filled. " + connectionPool.size());
    }

    protected void validPool() {
        int size = connectionPool.size();
        int failed = 0;
        while (size-- > 0) {
            Connection connection = connectionPool.poll();
            if (connection == null) break;
            try {
                if (connection.isValid(1)) {
                    connectionPool.offer(connection);
                } else {
                    ++failed;
                    connection.close();
                }
            } catch (SQLException e) {
                logger.log(Level.INFO, "Bad connection found", e);
            }
        }
        logger.log(Level.FINE, "validPool: " + failed + " disposed. " + connectionPool.size());
        fillPool();
    }

    @Override
    public void recycleConnection(Connection conn) {
        executeAsync(() -> {
            if (usedConnections.remove(conn)) {
                try {
                    if (conn.getAutoCommit() && conn.isValid(1)) {
                        logger.log(Level.FINE, "Connection recycled");
                        connectionPool.offer(conn);
                    } else {
                        logger.log(Level.FINE, "Connection disposed");
                        conn.close();
                    }
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            if (ThreadLocalRandom.current().nextDouble() < validPoolChance) {
                validPool();
            }
        });
    }

    public abstract Connection createConnection();

    @Override
    public void createTable(Class<?> cls) {
        Validate.notNull(cls);
        if (createdTableClasses.contains(cls)) return;
        TableStructure ts = TableStructure.fromClass(cls);
        String sql = ts.getCreateTableSQL();
        try (Statement smt = getConnection().createStatement()) {
            smt.executeUpdate(sql);
            createdTableClasses.add(cls);
        } catch (SQLException ex) {
            throw new RuntimeException(sql, ex);
        }
    }

    @Override
    public void close() {
        for (Connection usedConnection : usedConnections) {
            try {
                usedConnection.close();
            } catch (SQLException e) {
                getLogger().log(Level.INFO, "Bad connection found", e);
            }
        }
        for (Connection connection : connectionPool) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.log(Level.INFO, "Bad connection found", e);
            }
        }
    }

    @Override
    public void beginTransaction() {
        try {
            getConnection().setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void rollbackTransaction() {
        try {
            getConnection().rollback();
            getConnection().setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void commitTransaction() {
        try {
            getConnection().commit();
            getConnection().setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract Plugin getPlugin();

    protected void executeAsync(Runnable runnable) {
        executor.accept(runnable);
    }

    protected Logger getLogger() {
        return logger;
    }

    /**
     * Return the SynchronizedQuery object for specified table class.
     *
     * @return SynchronizedQuery object
     */
    @Override
    public <T> SynchronizedQuery.NonTransactionalQuery<T> query(Class<T> tableClass) {
        createTable(tableClass);
        return new SynchronizedQuery.NonTransactionalQuery<T>(tableClass, this.getConnection()) {
            @Override
            public T selectUniqueForUpdate() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void close() {

            }
        };
    }
}
