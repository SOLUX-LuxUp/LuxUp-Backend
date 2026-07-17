package com.taptap.backend.insight.util;

import java.time.LocalTime;
import java.util.List;

public final class TimeSlotResolver {

    public static final List<String> SLOT_ORDER = List.of("새벽", "아침", "점심", "저녁", "밤");

    private TimeSlotResolver() {
    }

    /**
     * 새벽 00:00~05:59 / 아침 06:00~09:59 / 점심 10:00~13:59 / 저녁 14:00~18:59 / 밤 19:00~23:59
     */
    public static String resolve(LocalTime time) {
        if (time.isBefore(LocalTime.of(6, 0))) return "새벽";
        if (time.isBefore(LocalTime.of(10, 0))) return "아침";
        if (time.isBefore(LocalTime.of(14, 0))) return "점심";
        if (time.isBefore(LocalTime.of(19, 0))) return "저녁";
        return "밤";
    }
}