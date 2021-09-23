package com.its.reactivespring.ch5;

import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.test.StepVerifier;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * example launches a thread when the stream initializes. The new thread stashes a reference to the FluxSink<Integer>,
 * using it to emit random value at random times up until five values have been emitted. Then, the stream completes.
 *
 * This example shows how to adapt asynchronous things in the world to a Reactive Stream type using some convenient factory methods.
 *
 * */
@Log4j2
public class AsyncApiIntegrationTest {

    private final ExecutorService executorService = Executors
                                                        .newFixedThreadPool(1);

    @Test
    public void async() {
        log.info("Entering AsyncApiIntegrationTest : async");
        // <1>
        /**
         * The Flux.create factory passes a reference to a FluxSink<T> in a Consumer<FluxSink<T>>. We will use
         * the FluxSink<T> to emit new elements as they become available. It is important that we stash this
         * reference for later
         * */
        Flux<Integer> integers = Flux
                                    .create(emitter -> this.launch(emitter, 5));

        log.info("Flux of integer created asynchronously ");
        // <2>
        /**
         * It’s important to tear down any resources once the Flux has finished its work.
         * */
        StepVerifier
                .create(integers
                            .doFinally(signalType -> this.executorService.shutdown())
                )
                .expectNextCount(5)//
                .verifyComplete();

        log.info("Leaving AsyncApiIntegrationTest : async");
    }

    // <3>
    /**
     * The launch method spins up a background thread using the ExecutorService. Setup whatever
     * connections with an external API only after execution inside the callback has begun.
     * */
    private void launch(FluxSink<Integer> integerFluxSink, int count) {
        log.info("Entering AsyncApiIntegrationTest : launch with fluxsink : {} and count : {} ",
                integerFluxSink, count);
        executorService
            .submit(() -> {
                var integer = new AtomicInteger();
                log.info("Atomic integer instantiated : {}", integer);
                Assert.assertNotNull(integerFluxSink);
                while (integer.get() < count) {
                    log.info("integer is < count ", count);
                    double random = Math.random();
                    /**
                     * Each time there’s a new element, use the FluxSink<T> to emit a new element
                     * */
                    integerFluxSink.next(integer.incrementAndGet());// <4>
                    long radomDurationToSleep = (long) random * 1_000;
                    log.info("Sleeping this thread for random duration : {}", radomDurationToSleep);
                    this.sleep(radomDurationToSleep);
                }
                log.info("integerFluxSink about to get completed", integer);
                /**
                 * Finally, once we’ve finished emitting elements, we tell the Subscriber<T> instances
                 * */
                integerFluxSink.complete(); // <5>
            });
    }

    private void sleep(long s) {
        try {
            Thread.sleep(s);
        }
        catch (Exception e) {
            log.error(e);
        }
    }

}
