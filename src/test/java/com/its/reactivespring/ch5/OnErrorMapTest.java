package com.its.reactivespring.ch5;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Use onErrorMap if you want to normalize errors or, for some reason, map one error to another. You can
 * use it with other operators to filter particular errors, then canonicalize them, then route to a shared
 * error handler.
 * */
@Slf4j
public class OnErrorMapTest {
    @Test
    public void OnErrorMap() throws Exception {
        log.info("Entering OnErrorMapTest : OnErrorMap");

        class GenericException extends RuntimeException {

        }
        var counter = new AtomicInteger();
        Flux<Integer> resultsInError  = Flux.error(new IllegalArgumentException("Oops !!"));
        Flux<Integer> errorHandlingStream = resultsInError
                                                .onErrorMap(IllegalArgumentException.class, e -> {
                                                    log.info("Entering and leaving onErrorMap -> apply with e : {} ", e);
                                                    return new GenericException();
                                                })
                                                .doOnError(GenericException.class, genericException -> {
                                                    log.info("Entering doOnError -> accept with exception : {} ", genericException);
                                                    counter.incrementAndGet();
                                                    log.info("Leaving doOnError -> accept after incrementing counter ");
                                                });

        log.info("errorHandlingStream instantiated");
        StepVerifier
            .create(errorHandlingStream)
            .expectError()
            .verify();

        log.info("Leaving OnErrorMapTest : OnErrorMap");
    }
}
