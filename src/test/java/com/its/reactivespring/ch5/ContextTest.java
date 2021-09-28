package com.its.reactivespring.ch5;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Signal;
import reactor.core.publisher.SignalType;
import reactor.util.context.Context;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * doOnEach operator is a handy way to gain access to the current Context, whose contents you can
 * then inspect.
 * */
@Slf4j
public class ContextTest {
    @Test
    public void reactorContext() {
        log.info("Entering ContextTest : reactorContext");
        var observedContextValues = new ConcurrentHashMap<String, AtomicInteger>();
        var max = 3;
        var key = "key1";
        var cdl = new CountDownLatch(max);
        Context reactorContext = Context.of(key,"value1");

        log.info("Key variables along with reactorContext instantiated");
        Flux<Integer> integerFlux = Flux
                                        .range(0,max)
                                        .delayElements(Duration.ofMillis(1))
                                        .doOnEach(signal -> {
                                            log.info("Entering doOnEach -> accept with signal : {} ", signal);
                                            Context currentContext = signal.getContext();
                                            if(signal.getType().equals(SignalType.ON_NEXT)) {
                                                String key1 = currentContext.get(key);
                                                log.info("Signal is onNext with currentContext's key : {} ", key1);
                                                Assert.assertNotNull(key1);
                                                Assert.assertEquals(key1, "value1");
                                                observedContextValues
                                                    .computeIfAbsent("key1", s -> {
                                                        log.info("Entering computeIfAbsent -> apply for string : {} ", s);
                                                        return new AtomicInteger(0);
                                                    })
                                                    .getAndIncrement();
                                            }
                                            log.info("Leaving doOnEach -> accept with signal : {} ", signal);
                                        })
                                        .subscriberContext(reactorContext);

        log.info("integerFlux instantiated");

        integerFlux
            .subscribe(integer -> {
                log.info("Entering subscribe -> accept with integer : {} ", integer);
                cdl.countDown();
                log.info("Leaving subscribe -> accept with integer : {} ", integer);
            });

        log.info("Leaving ContextTest : reactorContext");
    }
}
