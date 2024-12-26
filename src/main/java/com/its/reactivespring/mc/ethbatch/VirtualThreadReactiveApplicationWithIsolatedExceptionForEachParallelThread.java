package com.its.reactivespring.mc.ethbatch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StopWatch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootApplication
@Slf4j
public class VirtualThreadReactiveApplicationWithIsolatedExceptionForEachParallelThread {

    public static void main(String[] args) {
        log.info("Entering main");
        withFlatMap();
        log.info("Leaving main");
    }
    private static void processSomeBizLogic(String aStringObj) throws Exception {
        log.info("Entering processSomeBizLogic with aStringObj : {} ", aStringObj);
        Thread.sleep(AppConstants.SUCCESSFULL_PROCESSING_SLEEP_TIME);
        if (Integer.parseInt(aStringObj) % 5 == 0) {
            log.info("User " + aStringObj + " is erroneous (divisible by 9). Hence throwing exception after sleeping for 1 more sec");
            Thread.sleep(AppConstants.FAILURE_PROCESSING_SLEEP_TIME);
            throw new Exception("User " + aStringObj);
        }
        log.info("Leaving processSomeBizLogic with aStringObj : {} after sleeping for .02 sec", aStringObj);
    }

    public static void withFlatMap() {
        StopWatch stopWatch = new StopWatch();
        log.info("Entering withFlatMap");
        //List<String> objectList = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

        // Generate list with 25K string objects
        List<String> objectList = generateObjectList(AppConstants.USER_LIST_SIZE_500K);
        log.info("object list size : {}", objectList.size());


        stopWatch.start("Rx:VT - with flatMap");
        // Counters for success and failure
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        // Custom executor with virtual threads
        var virtualThreadExecutor = Executors.newThreadPerTaskExecutor(
            Thread
                .ofVirtual()
                .name("rx-vt-", 0)
                .factory()
        );
        /*ExecutorService es = Executors.newVirtualThreadPerTaskExecutor();
        Scheduler virtualThreadScheduler = Schedulers.fromExecutor(es);*/

        try (virtualThreadExecutor) {
            Flux
                .fromIterable(objectList)
                .flatMap(user ->
                    Mono
                        .fromCallable(() -> {
                            log.info("Entering processUser in virtual thread: {}", user);
                            processSomeBizLogic(user);
                            log.info("Leaving processUser in virtual thread: {}", user);
                            successCount.incrementAndGet();
                            return user;
                        })
                        .doOnError(error -> {
                            log.error("Error occurred while processing user {}: {}", user, error.getMessage());
                            failureCount.incrementAndGet();
                        })
                        .onErrorResume(error -> {
                            log.info("Skipping user due to error: {}", user);
                            return Mono.empty(); // Skip errored users
                        })
                        .subscribeOn(Schedulers.fromExecutor(virtualThreadExecutor)) // Use virtual threads
                )
                .doOnComplete(() -> {
                    log.info("Processing completed");
                    log.info("Success count: {}", successCount.get());
                    log.info("Failure count: {}", failureCount.get());
                })
                .blockLast();
        }
        stopWatch.stop();
        log.info("Time taken by Rx:VT - with flatMap : {} ms ", stopWatch.getLastTaskTimeMillis());
        log.info("Leaving withFlatMap");
    }

    public static List<String> generateObjectList(int size) {
        return IntStream
                .rangeClosed(1, size)
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());
    }

}
