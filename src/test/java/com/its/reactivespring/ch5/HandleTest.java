package com.its.reactivespring.ch5;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;
import reactor.test.StepVerifier;

import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class HandleTest {

    @Test
    public void testHandle() {
        log.info("Entering HandleTest : testHandle");
        log.info("Executing 1st StepVerifier");
        StepVerifier
            .create(this.handle(5,4))
            .expectNext(0,1,2,3)
            .expectError(IllegalArgumentException.class)
            .verify();

        log.info("Executing 2nd StepVerifier");
        StepVerifier
            .create(this.handle(3,3))
            .expectNext(0,1,2)
            .verifyComplete();
        log.info("Leaving HandleTest : testHandle");
    }

    private Flux<Integer> handle(int max, int noToErrors) {
        log.info("Entering and leaving HandleTest : handle with max : {} and noToErrors as : {} ", max, noToErrors);
        return Flux
                .range(0, max)
                .handle(new BiConsumer<Integer, SynchronousSink<Integer>>() {
                    @Override
                    public void accept(Integer value, SynchronousSink<Integer> sink) {
                        log.info("Entering Flux.range.handle.accept with value : {} & synchronousSink : {}", value, sink);
                        var upTo = Stream
                                    .iterate(0, new Predicate<Integer>() {
                                        @Override
                                        public boolean test(Integer i) {
                                            log.info("Checking integer : {} in predicate", i);
                                            return i < noToErrors;
                                        }
                                    }, new UnaryOperator<Integer>() {
                                        @Override
                                        public Integer apply(Integer i) {
                                            log.info("Checking integer : {} in UnaryOperator", i);
                                            return i + 1;
                                        }
                                    })
                                    .collect(Collectors.toList());

                        log.info("Stream : {} iterated with size as : {} ", upTo, upTo.size());

                        if(upTo.contains(value)) {
                            log.info("list contains integer : {} ", value);
                            sink
                                .next(value);
                            log.info("Returning from 1st if");
                            return;
                        }

                        if(value == noToErrors) {
                            log.info("integer contains error with count as  : {} ", noToErrors);
                            sink
                                .error(new IllegalArgumentException("No. 4 for you!"));
                            log.info("Returning from 2nd if");
                            return;
                        }
                        log.info("Marking the sink as complete");
                        sink.complete();
                    }

                });
    }
}
