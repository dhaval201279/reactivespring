package com.its.reactivespring.ch5;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
public class HotStreamTest2 {
    @Test
    public void hot2() throws InterruptedException {
        log.info("Entering HotStreamTest2 : hot2");
        int factor = 10;
        var cdl = new CountDownLatch(2);
        /**
         * share() - Returns a new {@link Flux} that multicasts (shares) the original {@link Flux}.
         * As long as there is at least one {@link Subscriber} this {@link Flux} will be subscribed and
         * emitting data.
         * When all subscribers have cancelled it will cancel the source
         *
         * @return a {@link Flux} that upon first subscribe causes the source {@link Flux}
         * to subscribe once, late subscribers might therefore miss items.
         * */
        Flux<Integer> live = Flux
                                .range(0,10)
                                .delayElements(Duration.ofMillis(factor))
                                .share();
        log.info("Flux instantiated");

        var one = new ArrayList<Integer>();
        var two = new ArrayList<Integer>();

        live
            .doFinally(signalTypeConsumer(cdl))
            .subscribe(addEmittedItem(one));
        log.info("Live data subscribed by 1st consumer. So the list are - one : {}, two : {}", one, two);
        Thread.sleep(factor * 10);

        live
            .doFinally(signalTypeConsumer(cdl))
            .subscribe(addEmittedItem(two));
        log.info("Live data subscribed by 2nd consumer. So the list are - one : {}, two : {}", one, two);

        cdl.await(5, TimeUnit.SECONDS);
        Assert.assertTrue(one.size() > two.size());
        log.info("Leaving HotStreamTest2 : hot2 with two list as - one : {} and two : {} ", one, two);
    }

    private Consumer<Integer> addEmittedItem(ArrayList<Integer> collection) {
        return i -> {
            log.info("Entering HotStreamTest2 : addEmittedItem -> accept");
            collection.add(i);
            log.info("Leaving HotStreamTest2 : addEmittedItem -> accept");
        };
    }

    private Consumer<SignalType> signalTypeConsumer(CountDownLatch cdl) {
        return signalType -> {
            log.info("Entering HotStreamTest2 : signalTypeConsumer -> accept");
            if (signalType.equals(SignalType.ON_COMPLETE)) {
                log.info("SignalType is ON_COMPLETE");
                try {
                    cdl.countDown();
                    log.info("await . . . .");
                } catch (Exception e) {
                    log.error("Exception occurred : {} ", e);
                    throw new RuntimeException(e);
                }
            }
            log.info("Leaving HotStreamTest2 : signalTypeConsumer -> accept");
        };
    }
}
