package com.its.reactivespring.ch5;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;


/**
 * stream is immutable. But, what if you want to operate on an existing publisher? Use
 * the transform operator. It gives you a reference to the current Publisher in which you can customize it.
 * It’s convenient as a way to generically modify a Publisher<T>. It gives you a chance to change the
 * Publisher at assembly time, on initialization.
 * */
@Slf4j
public class TransformerTest {
    @Test
    public void transform(){
        log.info("Entering TransformerTest : transform");
        var finished = new AtomicBoolean();

        /**
         * Flux
         *                         .just("A","B","C")
         *                         .transform(stringFlux ->
         *                             stringFlux
         *                                 .doFinally(signalType -> finished.set(Boolean.TRUE))
         *                         );
         * */

        /**
         * The transform operator gives us a chance to act on a Flux<T>, customizing it. This can be quite useful
         * if you want to avoid extra intermediate variables.
         * */

        var letters = Flux
                        .just("A","B","C")
                        .transform(new Function<Flux<String>, Publisher<String>>() {
                                       @Override
                                       public Publisher<String> apply(Flux<String> stringFlux) {
                                           log.info("Entering and returning from apply");
                                           return stringFlux
                                                   .doFinally((SignalType signalType) -> {
                                                       log.info("Signal Type : {} ", signalType);
                                                       finished.set(Boolean.TRUE);
                                                   });
                                       }
                                   }
                        );

        StepVerifier
            .create(letters)
            .expectNextCount(3)
            .verifyComplete();

        Assert
            .assertTrue("Finished boolean must be true : {}", finished.get());

        log.info("Leaving TransformerTest : transform");
    }
}
