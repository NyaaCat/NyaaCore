package cat.nyaa.nyaacore.database.provider;

import cat.nyaa.nyaacore.database.relational.BaseDatabase;
import cat.nyaa.nyaacore.database.relational.SynchronizedQuery;
import cat.nyaa.nyaacore.database.relational.TableStructure;
import com.google.common.base.FinalizablePhantomReference;
import com.google.common.base.FinalizableReferenceQueue;
import com.google.common.collect.Maps;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.ref.Reference;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLiteDatabase extends BaseDatabase {

    private Plugin plugin;
    private String file;
    private Connection dbConn;
    public static Function<Plugin, Consumer<Runnable>> executorSupplier = (plugin) -> (runnable) -> Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    public static Function<Plugin, Logger> loggerSupplier = Plugin::getLogger;
    private final Semaphore mainConnLock = new Semaphore(1);
    private final AtomicBoolean mainConnInTransaction = new AtomicBoolean(false);
    private static FinalizableReferenceQueue frq = new FinalizableReferenceQueue();
    private static final ConcurrentMap<Reference<?>, Semaphore> references = Maps.newConcurrentMap();

    public SQLiteDatabase(Plugin basePlugin, String fileName) {
        super(loggerSupplier.apply(basePlugin), executorSupplier.apply(basePlugin));
        maxPoolSize = 0;
        minPoolSize = 0;
        validPoolChance = 0;
        file = fileName;
        plugin = basePlugin;
        dbConn = createConnection();
    }

    @Override
    public void close() {
        super.close();
        try {
            dbConn.close();
            dbConn = null;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    final public Connection getConnection() {
        return dbConn;
    }

    @Override
    public Connection createConnection() {
        if (dbConn != null) {
            throw new IllegalStateException();
        }
        Connection conn;
        File dbFile = new File(plugin.getDataFolder(), file);
        try {
            Class.forName("org.sqlite.JDBC");
            String connStr = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            conn = DriverManager.getConnection(connStr);
            conn.setAutoCommit(true);
        } catch (ClassNotFoundException | SQLException ex) {
            throw new RuntimeException(ex);
        }
        return conn;
    }

    /**
     * Remember to close the new connection cloned.
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        SQLiteDatabase db = (SQLiteDatabase) super.clone();
        db.dbConn = db.createConnection();
        db.connectionPool.clear();
        db.usedConnections.clear();
        db.fillPool();
        return db;
    }

    @Override
    public void createTable(Class<?> cls) {
        Validate.notNull(cls);
        if (createdTableClasses.contains(cls)) return;
        try {
            if (!mainConnLock.tryAcquire(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            createTable(cls, getConnection());
        } finally {
            mainConnLock.release();
        }
    }

    private void createTable(Class<?> cls, Connection conn) {
        Validate.notNull(cls);
        if (createdTableClasses.contains(cls)) return;
        TableStructure ts = TableStructure.fromClass(cls);
        String sql = ts.getCreateTableSQL("sqlite");
        try (Statement smt = conn.createStatement()) {
            smt.executeUpdate(sql);
            createdTableClasses.add(cls);
        } catch (SQLException ex) {
            throw new RuntimeException(sql, ex);
        }
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public void beginTransaction() {
        try {
            mainConnLock.acquireUninterruptibly();
            super.beginTransaction();
            mainConnInTransaction.set(true);
        } catch (Throwable e) {
            mainConnLock.release();
            mainConnInTransaction.set(false);
            throw e;
        }
    }

    @Override
    public void commitTransaction() {
        if (!mainConnInTransaction.get()) {
            throw new IllegalStateException();
        }
        try {
            super.commitTransaction();
        } finally {
            mainConnLock.release();
            mainConnInTransaction.set(false);
        }
    }

    @Override
    public void rollbackTransaction() {
        if (!mainConnInTransaction.get()) {
            throw new IllegalStateException();
        }
        try {
            super.rollbackTransaction();
        } finally {
            mainConnLock.release();
            mainConnInTransaction.set(false);
        }
    }

    /**
     * Return the SynchronizedQuery object for specified table class.
     *
     * @return SynchronizedQuery object
     */
    @Override
    public <T> SynchronizedQuery.NonTransactionalQuery<T> query(Class<T> tableClass) {
        if (mainConnInTransaction.get()) {
            createTable(tableClass, getConnection());
        } else {
            createTable(tableClass);
        }
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

    @Override
    public <T> SynchronizedQuery.TransactionalQuery<T> queryTransactional(Class<T> tableClass) {
        Connection conn;
        try {
            if (!mainConnLock.tryAcquire(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            createTable(tableClass, conn);
        } catch (Throwable ex) {
            mainConnLock.release();
            throw new RuntimeException(ex);
        }
        SQLiteQuery<T> sqliteQuery = new SQLiteQuery<>(tableClass, conn, logger);
        FinalizablePhantomReference<SynchronizedQuery.TransactionalQuery<T>> reference = new FinalizablePhantomReference<SynchronizedQuery.TransactionalQuery<T>>(sqliteQuery, frq) {
            public void finalizeReferent() {
                Semaphore lock = references.remove(this);
                if (lock != null) {
                    logger.severe("Unhandled TransactionalQuery found: " + this);
                    try {
                        if (conn.isValid(1) && !conn.getAutoCommit()) {
                            conn.rollback();
                        }
                        conn.close();
                    } catch (SQLException e) {
                        logger.log(Level.SEVERE, "Bad connection!", e);
                    }
                    dbConn = null;
                    dbConn = createConnection();
                    lock.release();
                }
            }
        };
        references.put(reference, mainConnLock);
        sqliteQuery.setReference(reference);
        return sqliteQuery;
    }

    public static class SQLiteQuery<T> extends SynchronizedQuery.TransactionalQuery<T> {
        private final Logger logger;
        private FinalizablePhantomReference<SynchronizedQuery.TransactionalQuery<T>> reference;

        public SQLiteQuery(Class<T> tableClass, Connection conn, Logger logger) {
            super(tableClass, conn);
            this.logger = logger;
        }

        @Override
        public T selectUniqueForUpdate() {
            return selectUniqueUnchecked();
        }

        @Override
        public void close() {
            super.close();
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            } finally {
                Semaphore lock = references.remove(reference);
                if (lock != null) {
                    lock.release();
                } else {
                    logger.severe("Double handled TransactionalQuery found");
                }
            }
        }

        public void setReference(FinalizablePhantomReference<TransactionalQuery<T>> reference) {
            this.reference = reference;
        }
    }
}
