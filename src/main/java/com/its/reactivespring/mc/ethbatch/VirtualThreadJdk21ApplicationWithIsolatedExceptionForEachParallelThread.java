package com.its.reactivespring.mc.ethbatch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StopWatch;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootApplication
@Slf4j
public class VirtualThreadJdk21ApplicationWithIsolatedExceptionForEachParallelThread {
    public static void main(String[] args) {
        log.info("Entering main");
        withFlatMapUsingVirtualThreads();
        log.info("Leaving main");
    }

    public static void withFlatMapUsingVirtualThreads() {
        StopWatch stopWatch = new StopWatch();
        log.info("Entering withFlatMapUsingVirtualThreads");

        //List<String> users = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
        // Generate list with 25K string objects
        List<String> users = generateUserList(AppConstants.USER_LIST_SIZE_500K);
        log.info("User list size: {}", users.size());

        stopWatch.start("JDK 21 - with Virtual Threads");
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        var virtualThreadExecutor = Executors.newThreadPerTaskExecutor(
                Thread
                        .ofVirtual()
                        .name("jdk21-vt-", 0)
                        .factory()
        );

        try (virtualThreadExecutor) {
            // Submit tasks for parallel processing
            List<CompletableFuture<Void>> futures =
                    users.stream()
                            .map(user -> CompletableFuture.runAsync(() -> {
                                try {
                                    log.info("Processing user: {}", user);
                                    processSomeBizLogic(user);
                                    successCount.incrementAndGet();
                                } catch (Exception e) {
                                    log.error("Error occurred while processing user {}: {}", user, e.getMessage());
                                    failureCount.incrementAndGet();
                                }
                            }, virtualThreadExecutor))
                            .toList(); // Collect CompletableFuture<Void> for each user

            // Wait for all tasks to complete
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            try {
                allOf.join();
            } catch (Exception e) {
                log.error("Error waiting for all tasks to complete: {}", e.getMessage());
            }
        }

        // Log results
        log.info("Success count: {}", successCount.get());
        log.info("Failure count: {}", failureCount.get());
        log.info("## Processing completed for user list size: {} ", users.size());
        stopWatch.stop();
        log.info("Time taken by JDK 21 - with Virtual Threads: {} ms ", stopWatch.getLastTaskTimeMillis());
        log.info("Leaving withFlatMapUsingVirtualThreads");
    }

    private static void processSomeBizLogic(String user) throws Exception {
        log.info("Entering processUser with user: {}", user);
        Thread.sleep(AppConstants.SUCCESSFULL_PROCESSING_SLEEP_TIME); // Simulate processing delay
        if (Integer.parseInt(user) % 5 == 0) {
            log.info("User " + user + " is erroneous (divisible by 5). Hence throwing exception after sleeping for 1 more sec");
            Thread.sleep(AppConstants.FAILURE_PROCESSING_SLEEP_TIME);
            throw new Exception("User " + user);
        }
        log.info("Leaving processUser with user: {}", user);
    }

    public static List<String> generateUserList(int size) {
        return IntStream.rangeClosed(1, size)
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());
    }

}
