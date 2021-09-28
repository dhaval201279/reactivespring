package com.its.reactivespring.ch5;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.function.Function;

/**
 * If a reactive stream results in an error, then you can trap that error and decide what happens using
 * various operators whose name usually starts with on\*.
 *
 * Use the onErrorResume operator to produce a Publisher that should be emitted starting from the place
 * where the error was encountered.
 * */
@Slf4j
public class OnErrorResumeTest {
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
    public void onErrorResume() {
        log.info("Entering OnErrorResumeTest : onErrorResume");
        Flux<Integer> integerFlux = resultsInError
                                        .onErrorResume(IllegalArgumentException.class, e -> {
                                            log.info("Entering onErrorResume -> apply");
                                            return Flux
                                                    .just(3, 2, 1);
                                        });

        log.info("integerFlux instantiated");
        StepVerifier
            .create(integerFlux)
            .expectNext(1, 3, 2, 1)
            .verifyComplete();

        log.info("Leaving OnErrorResumeTest : onErrorResume");
    }
}
