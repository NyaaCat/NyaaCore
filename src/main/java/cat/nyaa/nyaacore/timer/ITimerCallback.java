package cat.nyaa.nyaacore.timer;

import java.time.Instant;

public interface ITimerCallback {
    /**
     * @param timerName      the timer name
     * @param handler
     * @param designatedTime the time point when it should be invoked. Note this may not be the same as the real time.
     *                       Especially when the server goes offline for a long time.
     *                       It's also possible for this to be called continuously but with very different designated time.
     */
    void timerInvoked(String timerName, TimerHandler handler, Instant designatedTime, boolean isResetCallback);
}
