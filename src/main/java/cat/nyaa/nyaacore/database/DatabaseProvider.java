package cat.nyaa.nyaacore.database;

import java.util.Map;

public interface DatabaseProvider {
    Database get(Map<String, Object> configuration);
}
