package com.tq.wechatnotifier.time;

import java.time.LocalTime;

public class TimePeriodHelper {

    public static boolean isWithinActivePeriod(int startH, int startM, int endH, int endM) {
        LocalTime now = LocalTime.now();
        LocalTime start = LocalTime.of(startH, startM);
        LocalTime end = LocalTime.of(endH, endM);

        if (start.isBefore(end) || start.equals(end)) {
            // Same day period: e.g., 9:00 - 17:00
            return !now.isBefore(start) && !now.isAfter(end);
        } else {
            // Overnight period: e.g., 22:00 - 07:00
            return !now.isBefore(start) || !now.isAfter(end);
        }
    }
}
