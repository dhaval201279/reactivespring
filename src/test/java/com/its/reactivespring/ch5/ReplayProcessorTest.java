package com.its.reactivespring.ch5;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.ReplayProcessor;
import reactor.test.StepVerifier;

/**
 * Another quite useful Processor<I,O> is the ReplayProcessor that replays either unbounded items or a limited number of
 * items to any late Subscriber<T>. In the example below, we configure a ReplayProcessor that will replay the last two
 * items observed for as many Subscriber<T> instances as want to subscribe.
 * */
@Slf4j
public class ReplayProcessorTest {
    @Test
    public void replayProcessor() {
        log.info("Entering ReplayProcessorTest : replayProcessor");
        int historySize = 2;

        /**
         * The ReplayProcessor.create factory method creates a processor that will retain the last 2 elements
         * (its history) and that will only do so for a limited (bounded) number of subscribers.
         * */
        ReplayProcessor replayProcessor = ReplayProcessor.create(historySize, Boolean.FALSE);
        log.info("Invoking produce after instantiating ReplayProcessor");
        produce(replayProcessor.sink());
        log.info("Invoking consume after producing String");
        consume(replayProcessor);

        log.info("Leaving ReplayProcessorTest : replayProcessor");
    }

    /**
     * Publishes 3 strings 1,2 and 3 with EmitterProcessor
     * */
    private void produce(FluxSink<String> sink) {
        log.info("Entering ReplayProcessorTest : produce");
        sink.next("1");
        sink.next("2");
        sink.next("3");
        sink.complete();
        log.info("Leaving ReplayProcessorTest : produce");
    }

    private void consume(Flux<String> publisher) {
        log.info("Entering ReplayProcessorTest : consume");
        //for (int i=0; i<5; i++) {
            StepVerifier
                .create(publisher)
                .expectNext("2")
                .expectNext("3")
                .verifyComplete();
        //}
        log.info("Leaving ReplayProcessorTest : consume");
    }
}
