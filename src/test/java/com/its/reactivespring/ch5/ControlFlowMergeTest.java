package com.its.reactivespring.ch5;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

/**
 * merge(Publisher<Publisher<T>>… publishers) : operator here because it works a bit like flatMap(Publisher t), in that
 * it flattens the Publisher<T> elements given to it.
 * */
@Slf4j
public class ControlFlowMergeTest {
    @Test
    public void merge() {
        log.info("Entering ControlFlowMergeTest : merge");
        Flux<Integer> fastest = Flux.just(5,6);
        Flux<Integer> secondFastest = Flux
                                        .just(1,2)
                                        .delayElements(Duration.ofMillis(2));
        Flux<Integer> thirdFastest = Flux
                                        .just(3,4)
                                        .delayElements(Duration.ofMillis(20));
        log.info("3 flux instantiated");
        Flux<Flux<Integer>> streamOfStreams = Flux
                                                .just(secondFastest, thirdFastest, fastest);
        log.info("streamofstreams instantiated");
        Flux<Integer> merge = Flux.merge(streamOfStreams);
        log.info("merge instantiated");
        StepVerifier
            .create(merge)
            .expectNext(5, 6, 1, 2, 3, 4)
            .verifyComplete();
        log.info("Leaving ControlFlowMergeTest : merge");
    }
}
