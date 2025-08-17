package com.loopers.support.monitoring;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class DbCallChecker {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final String name;

    public DbCallChecker(String name) {
        this.name = name;
    }

    /**
     * key: 시간 (밀리초 단위)
     * value: 해당 시간에 발생한 DB 조회 횟수
     */
    private final Map<String, AtomicLong> dbSelectCountPerTime = new ConcurrentHashMap<>();

    public void incrementDbSelectCount() {
        dbSelectCountPerTime
                .computeIfAbsent(LocalTime.now().format(FORMATTER), k -> new AtomicLong(0))
                .incrementAndGet();
    }

    public void logDbCall() {
        TreeMap<String, AtomicLong> sortedMap = new TreeMap<>(Collections.reverseOrder());
        sortedMap.putAll(dbSelectCountPerTime);

        int keySize = sortedMap.keySet().size();

        List<Long> counts = sortedMap.values().stream()
                .map(AtomicLong::get)
                .toList();

        long totalCalls = counts.stream().mapToLong(Long::longValue).sum();
        double avg = counts.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long max = counts.stream().mapToLong(Long::longValue).max().orElse(0);
        long min = counts.stream().mapToLong(Long::longValue).min().orElse(0);

        String stats = String.format(
                """
                        \n
                        =====================
                        [DB Call Stats - %s]
                        %-18s %6d   // DB 호출 시간대 개수
                        %-18s %6d   // 총 DB 호출 횟수
                        %-18s %6.2f   // 평균 호출 횟수
                        %-18s %6d   // 최대 호출 횟수
                        %-18s %6d   // 최소 호출 횟수
                        =====================
                        """,
                name,
                "Time Points:", keySize,
                "Total DB Calls:", totalCalls,
                "Avg Calls:", avg,
                "Max Calls:", max,
                "Min Calls:", min
        );

        log.info(stats);
        sortedMap.forEach((time, count) -> log.info("[{}] DB Calls: {}", time, count));
    }

    public void reset() {
        dbSelectCountPerTime.clear();
    }
}
