package cat.nyaa.nyaacoretester;

import cat.nyaa.nyaacore.orm.DatabaseUtils;
import cat.nyaa.nyaacoretester.orm.SQLiteDatabaseTest;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

public class NyaaCoreTester extends JavaPlugin {
    public static NyaaCoreTester instance;

    @Override
    public void onEnable() {
        instance = this;
        saveConfig();
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                boolean enabled = Boolean.parseBoolean(System.getProperty("nyaacore.tester.enabled", "false"));
                if (!enabled) {
                    getLogger().warning("NyaaCoreTester installed but \"-Dnyaacore.tester.enabled=true\" not set. Shutdown the server.");
                    getServer().shutdown();
                } else {
                    getLogger().info("Ready for testing...");

                    try {
                        // https://www.baeldung.com/junit-tests-run-programmatically-from-java
                        JUnitCore junit = new JUnitCore();
                        junit.addListener(new TextListener(System.out));
                        Result result = junit.run(NyaaCoreTestSuite.class);
                        System.out.println(String.format("Finished. Result: Failures: %d. Ignored: %d. Tests run: %d. Time: %dms.",
                                result.getFailureCount(),
                                result.getIgnoreCount(),
                                result.getRunCount(),
                                result.getRunTime()
                        ));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    getLogger().info("Testing complete.");
                    getServer().shutdown();
                }
            }
        });
    }

    @RunWith(Suite.class)
    @Suite.SuiteClasses({
            DemoTests.class,
            SQLiteDatabaseTest.class
    })
    public static class NyaaCoreTestSuite {

    }
}
