package com.its.reactivespring.ch5;

import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.FluxSink;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Cold streams - they represent a  * sequence of data that materialized when we started subscribing to the data. We could
 * subscribe again and get the same data back. The source of data is produced by the act of subscription. The producer of
 * the data is created by the consumer, in this case.
 *
 * Hot streams - A stream is said to be hot when the consumer of the data does not create the producer of the data.
 * when the data stream exists independent of any particular subscriber. A hot stream is more like our notion of a real stream of
 * water: each time you step foot (or, in this case, subscribe to it) into it, you’re stepping into a different
 * stream.
 *
 * This example shows how to use an EmitterProcessor (which are like synchronous Queue<T>s) to
 * publish three items of data. The first subscriber sees the first two elements. The second subscriber
 * subscribes. Then a third item is published, and both the first and the second subscribers see it. The fact
 * that the producer is hot means that the second subscriber observes only the last element, not the first
 * two.
 * */
@Log4j2
public class HotStreamTest1 {
    @Test
    public void hot() {
        log.info("Entering HotStreamTest1 : hot");
        var first = new ArrayList<Integer>();
        var second = new ArrayList<Integer>();

        EmitterProcessor emitter = EmitterProcessor
                                        .create();
        FluxSink<Integer> sink = emitter.sink();
        log.info("Sink obtained from instantiated Emitterprocessor");

        emitter
            .subscribe(addEmittedItem(first));
        log.info("Emitter subscribed with first list");
        sink.next(1);
        sink.next(2);
        log.info("Emitted 1 and 2, so lists are - first : {} & second : {} ", first, second);

        emitter
            .subscribe(addEmittedItem(second));
        log.info("Emitter subscribed with second list");
        sink.next(3);
        sink.complete();
        log.info("Emitted 3, so lists are - first : {} & second : {} ", first, second);

        Assert.assertTrue(first.size() > second.size());

        log.info("Leaving HotStreamTest1 : hot");
    }

    private Consumer<Integer> addEmittedItem(ArrayList<Integer> collection) {
        return i -> {
            log.info("Entering emitter.subscribe.accept");
            collection.add(i);
        };
    }
}
