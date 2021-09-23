package com.its.reactivespring.ch5;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicInteger;
/**
 * In typical, non-asynchronous programming, network requests issued on line one finish before those on
 * the next line.
 *
 * If you want to string together the
 * resolution of data in a stream, use the thenMany operator variants.
 *
 * There’s another variant, then, that accepts a Mono<T> instead of a Flux<T> but whose usage is otherwise
 * the same.
 * */
@Slf4j
public class ThenManyTest {
    @Test
    public void thenMany() {
        log.info("Entering ThenManyTest : thenMany");
        var letters = new AtomicInteger();
        var numbers = new AtomicInteger();

        Flux<String> letterPublisher = Flux
                                        .just("a", "b", "c")
                                        .doOnNext(value -> {
                                           log.info("Value obtained : {} ", value);
                                           letters.incrementAndGet();
                                        });

        log.info("letterPublisher instantiated");

        Flux<Integer> numberPublisher = Flux
                                        .just(1, 2, 3)
                                        .doOnNext(number -> {
                                            log.info("number obtained : {} ", number);
                                            numbers.incrementAndGet();
                                        });
        log.info("numberPublisher instantiated");

        Flux<Integer> thisBeforeThat = letterPublisher
                                        .thenMany(numberPublisher);

        log.info("thenMany invocation done on letterPublisher");

        StepVerifier
            .create(thisBeforeThat)
            .expectNext(1, 2, 3)
            .verifyComplete();

        log.info("Leaving ThenManyTest : thenMany");
    }
}
