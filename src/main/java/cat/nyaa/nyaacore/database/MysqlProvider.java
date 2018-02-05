package cat.nyaa.nyaacore.database;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.persistence.Table;
import java.util.Map;

@SuppressWarnings("unchecked")
public class MysqlProvider implements DatabaseProvider {

    @Override
    public MysqlDatabase get(Plugin plugin, Map<String, Object> configuration) {
        Class<?>[] classes = DatabaseUtils.scanClasses(plugin, configuration, Table.class);
        String jdbc = (String)configuration.get("jdbc");
        return new MysqlDatabase(plugin, jdbc == null? "com.mysql.jdbc.Driver" : jdbc, (String)configuration.get("url"), (String)configuration.get("username"), (String)configuration.get("password"), classes);
    }

}