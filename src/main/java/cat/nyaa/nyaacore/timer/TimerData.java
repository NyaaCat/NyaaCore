package cat.nyaa.nyaacore.timer;

import cat.nyaa.nyaacore.configuration.ISerializable;
import org.bukkit.configuration.ConfigurationSection;

import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;

public class TimerData implements ISerializable {
    // after how much time should the callback function be called.
    @Serializable(manualSerialization = true)
    public Duration duration;

    // Should the callback function be called multiple times during one period.
    @Serializable
    public boolean repeatable;

    // Should auto reset the timer, if set to false, resetPeriod and periodCountingBase are ignored.
    @Serializable
    public boolean autoReset;

    // How often should the timer be reset
    @Serializable(manualSerialization = true)
    public Period resetPeriod;

    // What's the first time should the timer be reset, can be in the past or future.
    @Serializable(manualSerialization = true)
    public ZonedDateTime periodCountingBase;

    // Should the timer automatically pause when the server turns down. If set to true, makeUpMissedOnServerUp is always considered as false.
    @Serializable
    public boolean pauseOnDetach;

    // Should the callback function be called when registered to make up the missed called during the detached period.
    @Serializable
    public boolean makeUpMissedOnReattach;

    @Override
    public void deserialize(ConfigurationSection config) {
        ISerializable.deserialize(config, this);
        this.duration = Duration.parse(config.getString("duration"));
        this.resetPeriod = Period.parse(config.getString("resetPeriod"));
        this.periodCountingBase = ZonedDateTime.parse(config.getString("periodCountingBase"));
    }

    @Override
    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);
        config.set("duration", duration.toString());
        config.set("resetPeriod", resetPeriod.toString());
        config.set("periodCountingBase", periodCountingBase.toString());
    }
}
