package com.its.reactivespring.ch5;

import lombok.extern.log4j.Log4j2;
import org.aopalliance.intercept.MethodInterceptor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.aop.framework.ProxyFactoryBean;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

/**
 * Reactor is an event loop: it spins up a Scheduler (sort of like a thread pool) to move work on and off of the CPU as
 * quickly as possible.
 *
 * This global, default Scheduler creates one thread per core on your machine. So, if you have four cores, then you’ll
 * have four threads.
 *
 * If you do something that blocks - if you genuinely have something that can only scaleout
 * by adding threads - you must offload that work to another Scheduler, one designed to scale up and
 * down to accommodate extra work.
 *
 * if you genuinely have something that can only scaleout by adding threads - you must offload that work to another Scheduler,
 * one designed to scale up and down to accommodate extra work.
 * */
@Log4j2
public class SchedulersExecutorServiceDecoratorsTest {
    private final AtomicInteger methodInvocationCounts = new AtomicInteger();
    private String rsb = "rsb";

    @Before
    public void before() {
        log.info("Entering SchedulersExecutorServiceDecoratorsTest : before");
        Schedulers
            .resetFactory();

        Schedulers
            .addExecutorServiceDecorator(this.rsb, new BiFunction<Scheduler, ScheduledExecutorService, ScheduledExecutorService>() {
                @Override
                public ScheduledExecutorService apply(Scheduler scheduler, ScheduledExecutorService scheduledExecutorService) {
                    log.info("Entering apply method to decorate scheduledExecutorService");
                    return decorate(scheduledExecutorService);
                }
            });
        log.info("Leaving SchedulersExecutorServiceDecoratorsTest : before");
    }

    private ScheduledExecutorService decorate(ScheduledExecutorService scheduledExecutorService) {
        log.info("Entering SchedulersExecutorServiceDecoratorsTest : decorate");
        try {
            var pfb = new ProxyFactoryBean();
            pfb.setProxyInterfaces(new Class[] { ScheduledExecutorService.class });
            pfb.addAdvice((MethodInterceptor) methodInvocation -> {
                log.info("Adding advice to proxy factory bean");
                var methodName = methodInvocation.getMethod().getName().toLowerCase();
                this.methodInvocationCounts.incrementAndGet();
                log.info("methodName: ( " + methodName + " ) incrementing...");
                return methodInvocation.proceed();
            });
            pfb.setSingleton(true);
            pfb.setTarget(scheduledExecutorService);
            log.info("Returning instantiated ProxyFactoryBean : {} ", pfb);
            return (ScheduledExecutorService) pfb.getObject();
        } catch (ClassNotFoundException e) {
            log.error("Exception occurred : {} ", e);
            e.printStackTrace();
        }
        log.info("Leaving SchedulersExecutorServiceDecoratorsTest : decorate");
        return null;
    }

    @Test
    public void changeDefaultDecorator() {
        log.info("Entering SchedulersExecutorServiceDecoratorsTest : changeDefaultDecorator");
        Flux<Integer> integerFlux = Flux
                                        .just(1)
                                        .delayElements(Duration.ofMillis(1));
        log.info("Invoking StepVerifier");

        StepVerifier
            .create(integerFlux)
            .thenAwait(Duration.ofMillis(10))
            .expectNextCount(1)
            .verifyComplete();
        Assert.assertEquals(1, this.methodInvocationCounts.get());
        log.info("Leaving SchedulersExecutorServiceDecoratorsTest : changeDefaultDecorator");
    }

    @After
    public void after() {
        log.info("Entering SchedulersExecutorServiceDecoratorsTest : after");
        Schedulers.resetFactory();
        Schedulers.removeExecutorServiceDecorator(this.rsb);
        log.info("Leaving SchedulersExecutorServiceDecoratorsTest : after");
    }
}
