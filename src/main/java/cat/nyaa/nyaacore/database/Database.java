package cat.nyaa.nyaacore.database;

public interface Database extends AutoCloseable {
    <T extends Database> T connect();
    void close();
}
