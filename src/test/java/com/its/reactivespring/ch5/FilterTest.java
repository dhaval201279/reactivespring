package com.its.reactivespring.ch5;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.function.Predicate;

/**
 * As you work your way through a stream, you might want to selectively filter out some values, which
 * you can do with filter. The filter operator applies a predicate to a stream of values, discarding those
 * that don’t match the predicate.
 * */

@Log4j2
public class FilterTest {
    @Test
    public void filter() {
        log.info("Entering FilterTest : filter");
        var count = 5;
        Flux<Integer> range = range()
                                .take(count);
        log.info("Obtained 5 nos. from the entire list");
        Flux<Integer> take = range
                                .filter(integer -> {
                                    log.info("Entering predicate");
                                    return integer % 2 == 0;
                                });
        log.info("Executing StepVerifier");
        StepVerifier
                .create(take)
                .expectNext(0,2,4)
                .verifyComplete();

        log.info("Leaving FilterTest : filter");
    }

    private Flux<Integer> range() {
        log.info("Entering and leaving FilterTest : range");
        return Flux.range(0, 1000);
    }
}
