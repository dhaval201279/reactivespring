package com.its.reactivespring.ch5;

import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/***/
@Log4j2
public class HotStreamTest3 {

    private List<Integer> one = new ArrayList<Integer>();
    private List<Integer> two = new ArrayList<Integer>();
    private List<Integer> three = new ArrayList<Integer>();

    @Test
    public void publish() {
        log.info("Entering HotStreamTest3 : publish");


        Flux<Integer> pileOn = Flux
                                .just(1,2,3)
                                .publish()
                                .autoConnect(3)
                                .subscribeOn(Schedulers.immediate());
        log.info("pileOn flux instantiated");

        pileOn
            .subscribe(subscribe(one));
        Assert.assertEquals(this.one.size(), 0);
        log.info("1st subscription done. Lists - one : {}, two : {}, three : {} ", one, two, three);

        pileOn
            .subscribe(subscribe(two));
        Assert.assertEquals(this.two.size(), 0);
        log.info("2nd subscription done. Lists - one : {}, two : {}, three : {} ", one, two, three);

        pileOn
            .subscribe(subscribe(three));
        Assert.assertEquals(this.three.size(), 3);
        Assert.assertEquals(this.two.size(), 3);
        Assert.assertEquals(this.three.size(), 3);
        log.info("3rd subscription done. Lists - one : {}, two : {}, three : {} ", one, two, three);

        log.info("Leaving HotStreamTest3 : publish");
    }

    private Consumer<Integer> subscribe(List<Integer> collection) {
        return i -> {
            log.info("Entering HotStreamTest3 : subscribe -> accept");
            collection.add(i);
        };
    }
}
