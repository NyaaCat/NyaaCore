package cat.nyaa.nyaacore.timer;

import cat.nyaa.nyaacore.configuration.ISerializable;
import org.bukkit.configuration.ConfigurationSection;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZonedDateTime;

public class TimerPersistData extends TimerData {

    @Serializable(manualSerialization = true)
    Instant creationTime;
    @Serializable(manualSerialization = true)
    Instant lastTimerCallback;
    @Serializable(manualSerialization = true)
    Instant lastResetCallback;
    @Serializable(manualSerialization = true)
    Instant lastCheckpoint;
    @Serializable(manualSerialization = true)
    Duration timeElapsed;
    @Serializable
    boolean isPaused;

    public TimerPersistData() {

    }

    public TimerPersistData(TimerData data) {
        this.duration = data.duration;
        this.repeatable = data.repeatable;
        this.autoReset = data.autoReset;
        this.resetPeriod = data.resetPeriod;
        this.periodCountingBase = data.periodCountingBase;
        this.pauseOnDetach = data.pauseOnDetach;
        this.makeUpMissedOnReattach = data.makeUpMissedOnReattach;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        ISerializable.deserialize(config, this);
        this.duration = Duration.parse(config.getString("duration"));
        this.resetPeriod = Period.parse(config.getString("resetPeriod"));
        this.periodCountingBase = ZonedDateTime.parse(config.getString("periodCountingBase"));

        this.creationTime = Instant.parse(config.getString("creationTime"));
        this.lastTimerCallback = Instant.parse(config.getString("lastTimerCallback"));
        this.lastResetCallback = Instant.parse(config.getString("lastResetCallback"));
        this.lastCheckpoint = Instant.parse(config.getString("lastCheckpoint"));
        this.timeElapsed = Duration.parse(config.getString("timeElapsed"));
    }

    @Override
    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);
        config.set("duration", duration.toString());
        config.set("resetPeriod", resetPeriod.toString());
        config.set("periodCountingBase", periodCountingBase.toString());

        config.set("creationTime", creationTime.toString());
        config.set("lastTimerCallback", lastTimerCallback.toString());
        config.set("lastResetCallback", lastResetCallback.toString());
        config.set("lastCheckpoint", lastCheckpoint.toString());
        config.set("timeElapsed", timeElapsed.toString());
    }
}
