package com.its.reactivespring.ch5;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.function.Predicate;

/**
 * A Publisher<T> might emit an infinite number of records, and you may not be interested in everything,
 * so you can use take(long) to limit the number of elements.
 *
 * If you want to apply some predicate and stop consuming messages when that predicate matches, use
 * takeUntil(Predicate). There are other take variants. One that might be particularly useful in a
 * networked microservice context is take(Duration).
 * */

@Slf4j
public class TakeTest {

    @Test
    public void take() {
        log.info("Entering TakeTest : take");
        var count = 5;
        Flux<Integer> take = range()
                                .take(count);
        log.info("Executing StepVerifier");
        /**
         * expectNext and expectNextCount r mutually exclusive. am unable to use both together over
         * here
         * */
        StepVerifier
            .create(take)
            .expectNext(0,1,2,3,4)
            //.expectNextCount(count)
            .verifyComplete();

        log.info("Leaving TakeTest : take");
    }

    private Flux<Integer> range() {
        log.info("Entering and leaving TakeTest : range");
        return Flux.range(0, 1000);
    }

    @Test
    public void takeUntil() {
        log.info("Entering TakeTest : takeUntil");
        var count = 50;

        Flux<Integer> take = range()
                                .takeUntil(i -> {
                                    log.info("Entering test");
                                    return i == count - 1;
                                });
        log.info("Executing StepVerifier");
        StepVerifier
                .create(take)
                .expectNextCount(count)
                .verifyComplete();
        log.info("Leaving TakeTest : takeUntil");
    }
}
