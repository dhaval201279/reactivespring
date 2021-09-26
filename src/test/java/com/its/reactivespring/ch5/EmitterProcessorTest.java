package com.its.reactivespring.ch5;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

/**
 * A Publisher<T> produces data, and a Subscriber<T> consumes data. Sometimes, you will want something that acts as a bridge,
 * performing double duty and satisfying the contract for both Publisher<T> and Subscriber<T> - useful if you need to adapt from one
 * type to the other, for example. Processor<T> is fit for purpose here.
 *
 * EmitterProcessor acts like a java.util.Queue<T>, allowing 1 end to pump the data into it and the other
 * to consume that data
 * */
@Log4j2
public class EmitterProcessorTest {
    
    @Test
    public void emitterProcessor(){
        log.info("Entering EmitterProcessorTest : emitterProcessor");
        /**
         * creates a new EmitterProcessor that acts as a sort of
         * queue.
         * */
        EmitterProcessor <String> processor = EmitterProcessor
                                                .create();
        log.info("Invoking produce after instantiating emitterprocessor");
        produce(processor.sink());
        log.info("Invoking consume after producing String");
        consume(processor);
        log.info("Leaving EmitterProcessorTest : emitterProcessor");
    }

    /**
     * Publishes 3 strings 1,2 and 3 with EmitterProcessor
     * */
    private void produce(FluxSink<String> sink) {
        log.info("Entering EmitterProcessorTest : produce");
        sink.next("1");
        sink.next("2");
        sink.next("3");
        sink.complete();
        log.info("Leaving EmitterProcessorTest : produce");
    }

    private void consume(Flux<String> publisher) {
        log.info("Entering EmitterProcessorTest : consume");
        StepVerifier
            .create(publisher)
            .expectNext("1")
            .expectNext("2")
            .expectNext("3")
            .verifyComplete();
        log.info("Leaving EmitterProcessorTest : consume");
    }

    @Test
    public void sinkManySpec() {
        log.info("Entering EmitterProcessorTest : sinkManySpec");
        /**
         * creates a new EmitterProcessor that acts as a sort of
         * queue.
         * */
        Sinks.Many <String> sinks = Sinks
                                        .many()
                                        .multicast()
                                        .onBackpressureBuffer();
        log.info("Invoking produce after instantiating Sinks.Many");
        produceSinkMany(sinks);
        log.info("Invoking consume after producing String");
        consumeSinkMany(sinks);
        log.info("Leaving EmitterProcessorTest : sinkManySpec");
    }

    /**
     * Publishes 3 strings 1,2 and 3 with Sinks.Many
     * */
    private void produceSinkMany(Sinks.Many <String> sink) {
        log.info("Entering EmitterProcessorTest : produceSinkMany");
        sink.tryEmitNext("1");
        sink.tryEmitNext("2");
        sink.tryEmitNext("3");
        log.info("Leaving EmitterProcessorTest : produceSinkMany");
    }

    private void consumeSinkMany(Sinks.Many <String> sinks) {
        log.info("Entering EmitterProcessorTest : consumeSinkMany");
        StepVerifier
                .create(sinks.asFlux())
                .expectNext("1")
                .expectNext("2")
                .expectNext("3")
                .verifyComplete();
        log.info("Leaving EmitterProcessorTest : consumeSinkMany");
    }
}
