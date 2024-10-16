package com.vts.fxdata.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TimeUtils {
    private static long SECONDS_PER_DAY = 86400;
    private static long SECONDS_PER_HOUR = 3600;
    private static long SECONDS_PER_MINUTE = 60;

    public static String formatDuration(LocalDateTime start, LocalDateTime end, boolean includeSeconds) {
        Duration duration = Duration.between(start, end);
        long totalSeconds = duration.getSeconds();

        // Extract the hours, minutes, and seconds
        long days = totalSeconds / SECONDS_PER_DAY;
        long hours = totalSeconds / SECONDS_PER_HOUR;
        long minutes = (totalSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE;
        String ret = "";
        if (days>0) {
            ret += String.format("%02dd ", days);
        }
        if (hours>0) {
            ret += String.format("%02dh ", hours);
        }
        if (minutes>0 || !includeSeconds) {
            ret += String.format("%02dm", minutes);
        }
        if (includeSeconds) {
            var seconds = totalSeconds % SECONDS_PER_MINUTE;
            ret += String.format(" %02ds", seconds);
        }
        return ret;
    }

    public static LocalDateTime removeSeconds(LocalDateTime time) {
        return time.truncatedTo(ChronoUnit.SECONDS);
    }
}
