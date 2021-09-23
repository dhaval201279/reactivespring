package com.its.reactivespring.ch5;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

/**
 * what happens if I have a Publisher of items, and for each item, I call into another
 * service that returns a Publisher<T>? Then, if you only had map, you’d have Publisher<Publisher<T>>,
 * which is harder to work with. We have an outer stream made up of inner streams.
 *
 * There are several operators, flatMap, concatMap, and switchMap, that flatten inner streams, merging
 * them into the outer stream.
 *
 * Two operators, flatMap and concatMap, work pretty much the same. They both merge items emitted by
 * inner streams into the outer stream. The difference between flatMap and concatMap is that the order in
 * which the items arrive is different. flatMap interleaves items from the inner streams; the order may be
 * different.
 * */
@Slf4j
public class FlatMapConcatMapTest {

    @Test
    public void flatMap() {
        log.info("Entering FlatMapConcatMapTest : flatMap");
        var data = Flux
                    .just(new Pair(1, 300), new Pair(2, 200), new Pair(3, 100))
                    .flatMap(id -> {
                        log.info("Entering function FlatMapConcatMapTest : apply");
                        return this.delayReplyFor(id.id, id.delay);
                    });
        log.info("Flux of data instantiated");
        StepVerifier
            .create(data)
            .expectNext(3, 2, 1)
            .verifyComplete();

        log.info("Leaving FlatMapConcatMapTest : flatMap");
    }

    @AllArgsConstructor
    static class Pair {
        private int id;
        private long delay;
    }

    private Flux<Integer> delayReplyFor(Integer i, long delay){
        log.info("Entering & leaving FlatMapConcatMapTest : delayReplyFor");
        return Flux
                .just(i)
                .delayElements(Duration.ofMillis(delay));
    }

    /**
     * The concatMap operator, on the other hand, preserves the order of items. The main disadvantage of
     * concatMap is that it has to wait for each Publisher<T> to complete its work. You lose asynchronicity on
     * the emitted items. It does its work one-by-one, so you can guarantee the ordering of the results.
     *
     * great example of event processing. In
     * such a scenario, each message corresponds to a mutation of some state, The following events, in the
     * following order, mutate the state in a customer record: "read," "update," "read," "delete," and "read."
     * These commands should be processed in the same order; you don’t want those updates processed in
     * parallel. Use concatMap to ensure that ordering.
     * */
    @Test
    public void concatMap() {
        log.info("Entering FlatMapConcatMapTest : concatMap");
        var data = Flux
                .just(new Pair(1, 300), new Pair(2, 200), new Pair(3, 100))
                .concatMap(id -> {
                    log.info("Entering function FlatMapConcatMapTest : apply");
                    return this.delayReplyFor(id.id, id.delay);
                });
        log.info("Flux of data instantiated");
        StepVerifier
                .create(data)
                .expectNext(1, 2, 3)
                .verifyComplete();

        log.info("Leaving FlatMapConcatMapTest : concatMap");
    }
}
