package com.its.reactivespring.mc.ethbatch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootApplication
@Slf4j
public class Jdk17ApplicationWithIsolatedExceptionForEachParallelThread {

    public static void main(String[] args) {
        log.info("Entering main");
        withFlatMapUsingJDK();
        log.info("Leaving main");
    }

    public static void withFlatMapUsingJDK() {
        StopWatch stopWatch = new StopWatch();
        log.info("Entering withFlatMapUsingJDK");

        //List<String> objectList = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

        // Generate list with 25K string objects
        List<String> objectList = generateObjectList(AppConstants.USER_LIST_SIZE_500K);
        log.info("object list size : {}", objectList.size());

        stopWatch.start("JDK - with CompletableFuture");
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Define thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(AppConstants.PARALLELISM);

        // Submit tasks for parallel processing
        List<CompletableFuture<Void>> futures =
                objectList
                    .stream()
                    .map(anObject -> CompletableFuture.runAsync(() -> {
                        try {
                            log.info("Processing anObject : {} via CompletableFuture.runAsync", anObject);
                            processSomeBizLogic(anObject);
                            log.info("anObject : {} processing completed via CompletableFuture.runAsync", anObject);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            log.error("Error occurred while processing anObject {}: {}", anObject, e.getMessage());
                            failureCount.incrementAndGet();
                        }
                    }, executorService))
                    .toList(); // Collect CompletableFuture<Void> for each user

        // Wait for all tasks to complete
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allOf.join();
        } catch (Exception e) {
            log.error("Error waiting for all tasks to complete: {}", e.getMessage());
        }

        // Shut down the executor
        executorService.shutdown();

        // Log results
        log.info("Success count: {}", successCount.get());
        log.info("Failure count: {}", failureCount.get());
        log.info("## Processing completed for user list size : {} ", objectList.size());
        stopWatch.stop();
        log.info("Time taken by jdk - with CompletableFuture : {} ms ", stopWatch.getLastTaskTimeMillis());
        log.info("Leaving withMapUsingJDK");
    }

    private static void processSomeBizLogic(String aStringObj) throws Exception {
        log.info("Entering processSomeBizLogic with aStringObj: {}", aStringObj);
        Thread.sleep(AppConstants.SUCCESSFULL_PROCESSING_SLEEP_TIME); // Simulate processing delay
        if (Integer.parseInt(aStringObj) % 5 == 0) {
            log.info("User " + aStringObj + " is erroneous (divisible by 9). Hence throwing exception after sleeping for 1 more sec");
            Thread.sleep(AppConstants.FAILURE_PROCESSING_SLEEP_TIME);
            throw new Exception("User " + aStringObj);
        }
        log.info("Leaving processSomeBizLogic with aStringObj : {} after sleeping for .02 sec", aStringObj);
    }

    public static List<String> generateObjectList(int size) {
        return IntStream
                .rangeClosed(1, size)
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());
    }
}
