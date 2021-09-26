package com.its.reactivespring.ch5;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.function.Function;

/**
 * Both flatMap and concatMap eventually process every inner stream so long as they all finally complete. switchMap is different;
 * it cancels any outstanding inner publishers as soon as a new value arrives.
 * 
 * Use Case to understand it better -
 * 
 * Imagine a network service offering predictions based on input characters - the quintessential lookahead service.
 * 1. You type "re" in a textbox, triggering a network request, and predictions for possible completions
 * return. 
 * 2. You type "rea" and trigger another network request.
 * 3. You type "reac" and trigger yet another request.
 * 4. You might type faster than the network service can provide predictions, which means you might type
 * "react" before the predictions for "reac" are available. 
 * 
 * Use switchMap to cancel the previous as-yet incomplete network calls, preserving only the latest outstanding network 
 * call for "react" and, eventually, "reactive."
 *
 * In this example, we use delayElements(long) to artificially
 * delay the publication of elements in the streams. So, the outer stream (the words typed) emits new
 * values every 100 ms. The inner stram (the network call) emits values every 500 ms. The outer stream
 * only ever sees the final results for the last word, "reactive."
 * 
 * */
@Slf4j
public class SwitchMapTest {
    @Test
    public void switchMapWithLookaheads() {
        log.info("Entering SwitchMapTest : switchMapWithLookaheads");
        Flux<String> source = Flux
                                .just("re", "rea", "reac", "react", "reactive")
                                .delayElements(Duration.ofMillis(100))
                                .switchMap(this::apply);

        log.info("Invoking test");
        StepVerifier
            .create(source)
            .expectNext("reactive -> reactive")
            .verifyComplete();

        log.info("Leaving SwitchMapTest : switchMapWithLookaheads");
    }
    
    private Flux<String> lookup(String word) {
        log.info("Entering and leaving SwitchMapTest : lookup");
        return Flux
                .just(word + " -> reactive")
                .delayElements(Duration.ofMillis(500));
    }

    private Publisher<? extends String> apply(String s) {
        log.info("Entering apply");
        return lookup(s);
    }
}
