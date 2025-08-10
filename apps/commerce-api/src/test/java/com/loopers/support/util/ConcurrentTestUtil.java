package com.loopers.support.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.List;

public class ConcurrentTestUtil {

    public static void executeAsyncWithTasks(List<Runnable> tasks) throws InterruptedException {
        int threadCount = tasks.size();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        try {
            for (Runnable task : tasks) {
                executorService.execute(() -> {
                    try {
                        task.run();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // 타임아웃을 30초로 증가
            latch.await(30, TimeUnit.SECONDS);
        } finally {
            executorService.shutdown();
            // 완전한 종료를 위해 대기
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        }
    }
}
