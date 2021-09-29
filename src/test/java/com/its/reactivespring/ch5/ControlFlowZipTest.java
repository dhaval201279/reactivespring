package com.its.reactivespring.ch5;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.util.function.Function;

/**
 * zip operator is beneficial in scatter-gather kinds of processing.
 *
 * E.g. Suppose you’ve issued a call to one database for a
 * sequence of orders (passing in their order IDs), ordered by their ID, and you’ve made another call to another database
 * for customer information belonging to a given order. So you’ve got two sequences, of identical length, ordered by the
 * same key (order ID). You can use zip to merge them into a Publisher<T> of Tuple* instances.
 * */
@Slf4j
public class ControlFlowZipTest {
    @Test
    public void zip(){
        log.info("Entering ControlFlowZipTest : zip");
        Flux<Integer> numbers = Flux.just(1, 2, 3);
        Flux<String> alphabets = Flux.just("a", "b", "c");
        log.info("2 flux instantiated");
        Flux<String> zip = Flux
                            .zip(numbers, alphabets)
                            .map(tuple -> {
                                log.info("Entering map -> apply with tuple : {} ", tuple);
                                return from(tuple.getT1(), tuple.getT2());
                            });
        log.info("zipped both the streams");
        StepVerifier
            .create(zip)
            .expectNext("1 : a", "2 : b", "3 : c")
            .verifyComplete();

        log.info("Leaving ControlFlowZipTest : zip");
    }

    private String from(Integer t1, String t2) {
        log.info("Entering and leaving ControlFlowZipTest : from");
        return t1 + " : " + t2;
    }
}
