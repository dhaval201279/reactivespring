package com.its.reactivespring.ch5;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Retry supports this with the retryBackoff(long times, Duration duration) operator. If a Publisher<T> emits no data and
 * doesn’t produce an error? Use repeatWhenEmpty(), which will attempt to re-subscribe in the event of an empty Publisher<T>
 * If the Publisher<T> is empty, and you don’t want to re-subscribe, and just want to produce a default value, use
 * defaultIfEmpty(T default).
 * */
@Slf4j
public class ControlFlowRetryTest {
    @Test
    public void retry(){
        log.info("Entering ControlFlowRetryTest : retry");
        var errored = new AtomicBoolean();
        Flux<String> producer = Flux
                                    .create(sink -> {
                                        log.info("Entering create -> accept");
                                        if (! errored.get()){
                                            errored.set(Boolean.TRUE);
                                            sink.error(new RuntimeException("Nope !"));
                                            log.info("Returning ex. name : {} ", RuntimeException.class.getName());
                                        } else {
                                            log.info("its not an error");
                                            sink.next("hello");
                                        }
                                        sink.complete();
                                        log.info("Leaving create -> accept");
                                    });
        log.info("Producer flux instantiated");
        Flux<String> retryOnError = producer.retry();
        log.info("retryonerror flux instantiated");
        StepVerifier
            .create(retryOnError)
            .expectNext("hello")
            .verifyComplete();
        log.info("Leaving ControlFlowRetryTest : retry");
    }
}
