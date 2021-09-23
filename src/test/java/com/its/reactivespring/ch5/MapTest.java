package com.its.reactivespring.ch5;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.function.Function;

/**
 * Map operator - applies a function to each item emitted in the stream. This function modifies
 * each item by the source Publisher<T> and emits the modified item. The source stream gets replaced
 * with another stream whose values are the output of the function applied to each item in the source
 * stream.
 * */
@Slf4j
public class MapTest {

    @Test
    public void maps() {
        log.info("Entering MapTest : maps");
        var data = Flux
                    .just("a", "b", "c")
                    .map(s -> {
                        log.info("Entering MapTest : apply");
                        return s.toUpperCase();
                    });
        log.info("Flux of data instantiated");
        StepVerifier
            .create(data)
            .expectNext("A", "B", "C")
            .verifyComplete();

        log.info("Leaving MapTest : maps");
    }
}
