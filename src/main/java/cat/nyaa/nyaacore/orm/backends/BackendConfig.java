package cat.nyaa.nyaacore.orm.backends;

import cat.nyaa.nyaacore.configuration.ISerializable;

public class BackendConfig implements ISerializable {
    @Serializable
    public String provider;
    @Serializable
    public String sqlite_file;
    @Serializable
    public String mysql_url;
    @Serializable
    public String mysql_username;
    @Serializable
    public String mysql_password;
    @Serializable
    public String mysql_jdbc_driver;

    public BackendConfig() {
    }

    public BackendConfig(String provider, String sqlite_file, String mysql_url, String mysql_username, String mysql_password, String mysql_jdbc_driver) {
        this.provider = provider;
        this.sqlite_file = sqlite_file;
        this.mysql_url = mysql_url;
        this.mysql_username = mysql_username;
        this.mysql_password = mysql_password;
        this.mysql_jdbc_driver = mysql_jdbc_driver;
    }

    public static BackendConfig sqliteBackend(String dbFileName) {
        return new BackendConfig("sqlite", dbFileName, null, null, null, null);
    }

    public static BackendConfig mysqlBackend(String url) {
        return mysqlBackend(url, null, null);
    }

    public static BackendConfig mysqlBackend(String url, String username, String password) {
        return new BackendConfig("mysql", null, url, username, password, null);
    }
}
