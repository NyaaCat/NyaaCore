package cat.nyaa.nyaacore.database;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AutoQuery<T> implements Query<T> {

    private final Query<T> query;

    public AutoQuery(Query<T> query) {
        this.query = query;
    }

    @Override
    public Query<T> clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Query<T> whereEq(String columnName, Object obj) {
        return query.whereEq(columnName, obj);
    }

    @Override
    public Query<T> where(String columnName, String comparator, Object obj) {
        return query.where(columnName, comparator, obj);
    }

    @Override
    public void delete() {
        try {
            query.delete();
        } finally {
            query.close();
        }
    }

    @Override
    public CompletableFuture<Void> deleteAsync() {
        return query.deleteAsync().thenRun(query::close).exceptionally(e -> {
            query.close();
            throw new RuntimeException(e);
        });
    }

    @Override
    public void insert(T object) {
        try {
            query.insert(object);
        } finally {
            query.close();
        }
    }

    @Override
    public CompletableFuture<Void> insertAsync(T object) {
        return query.insertAsync(object).thenRun(query::close).exceptionally(e -> {
            query.close();
            throw new RuntimeException(e);
        });
    }

    @Override
    public List<T> select() {
        try {
            return query.select();
        } finally {
            query.close();
        }
    }

    @Override
    public CompletableFuture<List<T>> selectAsync() {
        return query.selectAsync().thenApply(ts -> {
            query.close();
            return ts;
        }).exceptionally(e -> {
            query.close();
            throw new RuntimeException(e);
        });
    }

    @Override
    public T selectUnique() {
        try {
            return query.selectUnique();
        } finally {
            query.close();
        }
    }

    @Override
    public CompletableFuture<T> selectUniqueAsync() {
        return query.selectUniqueAsync().thenApply(t -> {
            query.close();
            return t;
        }).exceptionally(e -> {
            query.close();
            throw new RuntimeException(e);
        });
    }

    @Override
    public T selectUniqueUnchecked() {
        try {
            return query.selectUniqueUnchecked();
        } finally {
            query.close();
        }
    }

    @Override
    public CompletableFuture<T> selectUniqueUncheckedAsync() {
        return query.selectUniqueUncheckedAsync().thenApply(t -> {
            query.close();
            return t;
        }).exceptionally(e -> {
            query.close();
            throw new RuntimeException(e);
        });
    }

    @Override
    public int count() {
        try {
            return query.count();
        } finally {
            query.close();
        }
    }

    @Override
    public CompletableFuture<Integer> countAsync() {
        return query.countAsync().thenApply(t -> {
            query.close();
            return t;
        }).exceptionally(e -> {
            query.close();
            throw new RuntimeException(e);
        });
    }

    @Override
    public void update(T obj, String... columns) {
        try {
            query.update(obj, columns);
        } finally {
            query.close();
        }
    }

    @Override
    public CompletableFuture<Void> updateAsync(T obj, String... columns) {
        return query.updateAsync(obj, columns)
                    .thenRun(query::close)
                    .exceptionally(e -> {
                        query.close();
                        throw new RuntimeException(e);
                    });
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }
}
