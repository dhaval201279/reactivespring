package com.its.reactivespring.ch5;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.function.Function;

/**
 * Use the onErrorReturn operator to produce a single value to be emitted starting from the place where
 * the error was encountered.
 * */
@Slf4j
public class OnErrorReturnTest {
    private final Flux<Integer> resultsInError = Flux
                                                    .just(1, 2, 3)
                                                    .flatMap(integer -> {
                                                        log.info("Entering flatMap -> apply with integer : {} ", integer);
                                                        if (integer == 2) {
                                                            log.info("integer is 2, hence throwing exception");
                                                            return Flux
                                                                    .error(new IllegalArgumentException("Oops !!"));
                                                        } else {
                                                            log.info("integer is other than 2");
                                                            return Flux
                                                                    .just(integer);
                                                        }
                                                    });

    @Test
    public void OnErrorReturn() {
        log.info("Entering OnErrorReturnTest : OnErrorReturn");
        Flux<Integer> integerFlux = resultsInError
                                        .onErrorReturn(0);

        log.info("integerFlux instantiated");
        StepVerifier
            .create(integerFlux)
            .expectNext(1, 0)
            .verifyComplete();

        log.info("Leaving OnErrorReturnTest : OnErrorReturn");
    }
}
