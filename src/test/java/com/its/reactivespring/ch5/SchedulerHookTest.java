package com.its.reactivespring.ch5;

import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Schedulers.onScheduleHook - It lets you modify the Runnable that ultimately gets executed by the Reactor Scheduler.
 *
 * Use subscribeOn(Scheduler) on either Mono or Flux to specify on which Scheduler the runtime should run
 * subscribe, onSubscribe, and request. Placing this operator anywhere in the chain will also impact the
 * execution context of onNext, onError, and onComplete signals from the beginning of the chain up to the
 * next occurrence of a publishOn(Scheduler).
 *
 * Use publishOn(Scheduler) on either Mono or Flux to specify on which Scheduler the runtime should run
 * onNext, onComplete, and onError. This operator influences the threading context where the rest of the
 * operators in the chain below it will execute, up to the next occurrence of publishOn(Scheduler). This
 * operator is typically used to serialize or slow down fast publishers that have slow consumers.
 * */
@Log4j2
public class SchedulerHookTest {

    @Test
    public void onSchedulerHook() {
        log.info("Entering SchedulerHookTest : onSchedulerHook");
        var counter = new AtomicInteger();

        log.info("Configuring schedulers via schedulehook");
        Schedulers
            .onScheduleHook("my hook", new Function<Runnable, Runnable>() {
                @Override
                public Runnable apply(Runnable runnable) {
                    log.info("Entering Schedulers.onScheduleHook.apply");
                    return new Runnable() {
                        @Override
                        public void run() {
                            log.info("Entering Schedulers.onScheduleHook.apply.run");
                            var threadName = Thread.currentThread().getName();
                            counter.incrementAndGet();
                            log.info("Counter incremented : {} ", counter);
                            log.info("Before execution : {} " , threadName);
                            runnable.run();
                            log.info("After execution : {} " , threadName);
                        }
                    };
                }
            });
        log.info("Publishing data");
        Flux<Integer> integerFlux = Flux
                                        .just(1, 2, 3)
                                        .delayElements(Duration.ofMillis(1))
                                        .subscribeOn(Schedulers.immediate());

        log.info("Invoking StepVerifier");

        StepVerifier
                .create(integerFlux)
                .expectNext(1, 2, 3)
                .verifyComplete();
        Assert.assertEquals("Count should be 3", 3, counter.get());

        log.info("Leaving SchedulerHookTest : onSchedulerHook");
    }
}
