package com.its.reactivespring.ch5;

import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Signal;
import reactor.core.publisher.SignalType;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

@Log4j2
public class DoOnOrLifecycleTest {
    @Test
    public void doOn() {
        log.info("Entering DoOnOrLifecycleTest : doOn");
        var signals = new ArrayList<Signal<Integer>>();
        var nextValues = new ArrayList<Integer>();
        var subscriptions = new ArrayList<Subscription>();
        var exceptions = new ArrayList<Throwable>();
        var finallySignals = new ArrayList<SignalType>();

        Flux<Integer> on = Flux
                            .<Integer>create(sink -> {
                                log.info("--Entering Flux.create : accept");
                                sink.next(1);
                                sink.next(2);
                                sink.next(3);
                                sink.error(new IllegalArgumentException("oops !!!"));
                                sink.complete();
                                log.info("--Leaving Flux.create : accept");
                            })
                            .doOnNext(new Consumer<Integer>() {
                                @Override
                                public void accept(Integer integer) {
                                    log.info("Entering Flux.create.doOnNext to add an integer to list");
                                    nextValues.add(integer);
                                    log.info("Leaving Flux.create.doOnNext after adding an integer to list");
                                }
                            })
                            .doOnEach(new Consumer<Signal<Integer>>() {
                                @Override
                                public void accept(Signal<Integer> integerSignal) {
                                    log.info("Entering Flux.create.doOnEach to add a signal to list");
                                    signals.add(integerSignal);
                                    log.info("Leaving Flux.create.doOnEach after adding a signal to list");
                                }
                            })
                            .doOnSubscribe(new Consumer<Subscription>() {
                                @Override
                                public void accept(Subscription subscription) {
                                    log.info("Entering Flux.create.doOnSubscribe to add a subscription to list");
                                    subscriptions.add(subscription);
                                    log.info("Leaving Flux.create.doOnSubscribe after adding a subscription to list");
                                }
                            })
                            .doOnError(IllegalArgumentException.class, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) {
                                    log.info("Entering Flux.create.doOnError to add a throwable to list");
                                    exceptions.add(throwable);
                                    log.info("Leaving Flux.create.doOnError after adding a throwable to list");
                                }
                            })
                            .doFinally(new Consumer<SignalType>() {
                                @Override
                                public void accept(SignalType signalType) {
                                    log.info("Entering Flux.create.doFinally to add a signalType to list");
                                    finallySignals.add(signalType);
                                    log.info("Leaving Flux.create.doFinally after adding a signalType to list");
                                }
                            });

        log.info("Executing StepVerifier");
        StepVerifier
                .create(on)
                .expectNext(1,2,3)
                .expectError(IllegalArgumentException.class)
                .verify();

        log.info("Logging and asserting signals info");
        signals
            .forEach(new Consumer<Signal<Integer>>() {
                @Override
                public void accept(Signal<Integer> integerSignal) {
                    log.info("Entering and leaving signals.forEach with parameter : {} ", integerSignal);
                }
            });
        Assert.assertEquals(4, signals.size());


        log.info("Logging and asserting finallysignal info");
        finallySignals
                .forEach(new Consumer<SignalType>() {
                    @Override
                    public void accept(SignalType signalType) {
                        log.info("Entering and leaving finallySignals.forEach with parameter : {} ", signalType);
                    }
                });
        Assert.assertEquals(finallySignals.size(), 1);

        log.info("Logging and asserting subscriptions info");
        subscriptions
            .forEach(new Consumer<Subscription>() {
                @Override
                public void accept(Subscription subscription) {
                    log.info("Entering and leaving subscriptions.forEach with parameter : {} ", subscription);
                }
            });
        Assert.assertEquals(subscriptions.size(), 1);

        log.info("Logging and asserting exceptions info");
        exceptions
            .forEach(new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) {
                    log.info("Entering and leaving exceptions.forEach with parameter : {} ", throwable);
                }
            });
        Assert.assertEquals(exceptions.size(), 1);
        Assert.assertTrue(exceptions.get(0) instanceof IllegalArgumentException);

        log.info("Logging and asserting next values info");
        nextValues
            .forEach(new Consumer<Integer>() {
                @Override
                public void accept(Integer integer) {
                    log.info("Entering and leaving nextValues.forEach with parameter : {} ", integer);
                }
            });
        Assert.assertEquals(nextValues.size(), 3);
        Assert.assertEquals(Arrays.asList(1, 2, 3), nextValues);

        log.info("Leaving DoOnOrLifecycleTest : doOn");
    }
}
