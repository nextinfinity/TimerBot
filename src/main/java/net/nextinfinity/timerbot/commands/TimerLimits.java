package net.nextinfinity.timerbot.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class TimerLimits {
    static final long MAX_MINUTES = 24 * 60;
    static final int MAX_REMINDERS = 60;
    static final int MAX_PER_USER = 3;
    static final int MAX_PER_GUILD = 10;

    private final Map<Long, Integer> guildCounts = new HashMap<>();
    private final Map<Long, Integer> userCounts = new HashMap<>();

    static List<Long> reminderTimes(long length, long interval, boolean warning) {
        if (length < 0 || length > MAX_MINUTES) {
            throw new IllegalArgumentException("Timer length must be between 0 and 1440 minutes.");
        }
        if (interval < 1 || interval > MAX_MINUTES) {
            throw new IllegalArgumentException("Notification interval must be between 1 and 1440 minutes.");
        }
        List<Long> times = new ArrayList<>();
        for (long time = interval; time < length; time += interval) {
            times.add(time);
            if (times.size() > MAX_REMINDERS) {
                throw new IllegalArgumentException("A timer may have at most 60 reminders. Increase the notification interval.");
            }
        }
        if (warning && length > 1 && !times.contains(length - 1)) {
            times.add(length - 1);
        }
        if (times.size() > MAX_REMINDERS) {
            throw new IllegalArgumentException("A timer may have at most 60 reminders, including the one-minute warning.");
        }
        times.sort(Long::compareTo);
        return List.copyOf(times);
    }

    synchronized Runnable reserve(long guild, long user) {
        if (guildCounts.getOrDefault(guild, 0) >= MAX_PER_GUILD
                || userCounts.getOrDefault(user, 0) >= MAX_PER_USER) {
            throw new IllegalArgumentException("Limit reached: 3 active timers per user and 10 per server.");
        }
        guildCounts.merge(guild, 1, Integer::sum);
        userCounts.merge(user, 1, Integer::sum);
        // Idempotent release, including when a scheduling failure races completion.
        return new Runnable() {
            private boolean released;
            public void run() {
                synchronized (TimerLimits.this) {
                    if (!released) {
                        released = true;
                        decrement(guildCounts, guild);
                        decrement(userCounts, user);
                    }
                }
            }
        };
    }

    private static void decrement(Map<Long, Integer> counts, long key) {
        counts.computeIfPresent(key, (ignored, count) -> count == 1 ? null : count - 1);
    }
}
