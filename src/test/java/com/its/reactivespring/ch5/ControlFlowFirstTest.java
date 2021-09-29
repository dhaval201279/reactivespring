package com.its.reactivespring.ch5;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * first : it returns the first Publisher<T> from among a number of Publisher<T> instances that emits data. Even better,
 * first applies backpressure to the other Publisher<T> instances. At first, I admit, it’s hard to see why you might use
 * this, but it is critical in supporting one of favorite patterns: service hedging.
 * */
@Slf4j
public class ControlFlowFirstTest {
    @Test
    public void first(){
        log.info("Entering ControlFlowFirstTest : first");
        Flux<Integer> slow = Flux
                                .just(1, 2, 3)
                                .delayElements(Duration.ofMillis(10));
        Flux<Integer> fast = Flux
                                .just(4, 5, 6, 7)
                .delayElements(Duration.ofMillis(2));
        log.info(" 2 flux instantiated");

        Flux<Integer> first = Flux
                                .first(slow, fast);

        log.info(" first instantiated");

        StepVerifier
            .create(first)
            .expectNext(4, 5, 6, 7)
            .verifyComplete();

        log.info("Leaving ControlFlowFirstTest : first");
    }
}
