package com.its.reactivespring.ch5;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * timeout : if a value isn’t recorded from a Publisher<T> within a particular time, a java.util.concurrent.TimeoutException
 * is returned. This is an excellent last-line-of-defense for making potentially slow, shaky, service-to-service calls.
 * The timeout operator is great if you want to timebox potentially error-prone or stalled requests, aborting if the
 * request takes longer than your SLA budget affords you.
 * */
@Slf4j
public class ControlFlowTimeoutTest {
    @Test
    public void timeout(){
        log.info("Entering ControlFlowTimeoutTest : timeout");
        Flux<Integer> numbers = Flux
                                .just(1, 2, 3)
                                .delayElements(Duration.ofSeconds(1))
                                .timeout(Duration.ofMillis(500))
                                .onErrorResume(this :: given);

        log.info(" flux instantiated");

        StepVerifier
            .create(numbers)
            .expectNext(0)
            .verifyComplete();

        log.info("Leaving ControlFlowTimeoutTest : timeout");
    }

    private Flux<Integer> given(Throwable throwable) {
        log.info("Entering ControlFlowTimeoutTest : given with exception : {} ", throwable);
        Assert
            .assertTrue("this exception should be a : " + TimeoutException.class.getName(),
                    throwable instanceof TimeoutException);
        log.info("Leaving ControlFlowTimeoutTest : given");
        return Flux.just(0);
    }


}
