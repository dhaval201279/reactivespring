package com.its.reactivespring.mc.ethoca;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StopWatch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootApplication
@Slf4j
public class ReactiveApplicationWithIsolatedExceptionForEachParallelThread {

    /*public static void main(String[] args) {
        log.info("Entering main");
        //SpringApplication.run(ReactiveApplicationWithIsolatedExceptionForEachParallelThread.class, args);
        //withDoOnNext();
        withFlatMap();
        //withMap();
        log.info("Leaving main");
    }*/

   /*private static void withDoOnNextAndOnErrorContinue() {
      log.info("Entering withDoOnNext");
      List<String> users = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
      Flux
            .fromIterable(users)
            .parallel(4)
            .runOn(Schedulers.boundedElastic())
            .doOnNext(user -> {
               log.info("Processing user : {}", user);
               // Call processUser and handle exceptions
               try {
                  log.info("Processing user : {}", user);
                  prcessUser(user);
                  log.info("Processing user completed within doOnNext: {}", user);
               } catch (Exception e) {
                  throw new RuntimeException("Error processing user " + user, e);
               }
            })
            .onErrorContinue((error, user) -> {
               log.error("Error occurred while processing user: {} - error: {}", user, error.getMessage());
            })
            .sequential()
            .doOnComplete(() -> {
               log.info("Processing completed");
            })
            .blockLast();
      log.info("Leaving withDoOnNext");
   }*/

    private static void withDoOnNext() {
        StopWatch stopWatch = new StopWatch();

        log.info("Entering withDoOnNext");
        List <String> users = Arrays.asList("1","2","3","4","5","6","7","8","9","10");
        stopWatch.start("Rx - with doOnNext");
        Flux
                .fromIterable(users)
                .parallel(AppConstants.PARALLELISM)
                .runOn(Schedulers.boundedElastic())
                .doOnNext(user -> {
                    try {
                        log.info("Processing user : {}", user);
                        processUser(user);
                        log.info("Processing user completed within doOnNext: {}", user);
                    } catch (Exception e) {
                        throw new RuntimeException("Error processing user " + user, e);
                    }
                })
                .doOnError(error -> {
                    log.error("Error occurred while processing user - error : {} ", error);
                })
                .sequential()
                .doOnComplete(() -> {
                    log.info("Processing completed");
                })
                .blockLast();
        stopWatch.stop();
        log.info("Time taken bgy Rx - with doOnNext : {} ms ", stopWatch.getLastTaskTimeMillis());
        log.info("Leaving withDoOnNext");

    }

    private static void processUser(String user) throws Exception {
        log.info("Entering processUser with user : {} ", user);
        //try {
        Thread.sleep(AppConstants.SUCCESSFULL_PROCESSING_SLEEP_TIME);
        if (Integer.parseInt(user) % 5 == 0) {
            log.info("User " + user + " is erroneous (divisible by 5). Hence throwing exception after sleeping for 1 more sec");
            Thread.sleep(AppConstants.FAILURE_PROCESSING_SLEEP_TIME);
            throw new Exception("User " + user);
        }
      /*} catch (InterruptedException e) {
         log.error("Interrupted exception occurred e : {} ", e);
         e.printStackTrace();
      } catch (Exception e) {
         log.error("Exception occurred e : {} ", e);
         e.printStackTrace();
      }*/
        log.info("Leaving processUser with user : {} after sleeping for .02 sec", user);
    }

    public static void withFlatMap() {
        log.info("Entering withFlatMap");
        //List<String> users = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");

        // Generate list with 25K string objects
        List<String> users = generateUserList(AppConstants.USER_LIST_SIZE_100K);
        log.info("user list size : {}", users.size());

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("Rx - with flatMap");
        // Counters for success and failure
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        Flux
                .fromIterable(users)
                .parallel(AppConstants.PARALLELISM)
                .runOn(Schedulers.boundedElastic())
                .flatMap(user ->
                        Mono.fromCallable(() -> {
                                    log.info("Entering processUser from Callable : {} ", user);
                                    processUser(user);
                                    log.info("Leaving processUser from Callable  : {}", user);
                                    successCount.incrementAndGet();
                                    return user;

                                })
                                .doOnError(error -> {
                                    log.error("Error occurred while processing user {}: {}", user, error.getMessage());
                                    failureCount.incrementAndGet();
                                })
                                .onErrorResume(error -> {
                                    log.info("Entering onErrorResume");
                                    return Mono.empty();
                                }) // Skip the errored user
                )
                .sequential()
                .doOnComplete(() -> {

                    log.info("Success count: {}", successCount.get());
                    log.info("Failure count: {}", failureCount.get());
                    log.info("$$ Processing completed for user list size : {} ", users.size());
                })
                .blockLast();
        stopWatch.stop();
        log.info("Time taken by Rx - with flatMap : {} ms ", stopWatch.getLastTaskTimeMillis());
        log.info("Leaving withFlatMap");
    }

    public static List<String> generateUserList(int size) {
        return IntStream
                .rangeClosed(1, size)
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());
    }

}