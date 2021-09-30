package com.its.reactivespring.ch5;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.test.StepVerifier;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

/**
 * checkpoint operator can be more efficient at runtime than Hooks.onOperatorDebug() because it
 * isolates the places where Reactor captures stack traces to the site where you’ve placed a checkpoint
 * */
@Slf4j
public class CheckpointTest {

    @Test
    public void checkpoint() {
        log.info("Entering CheckpointTest : checkpoint");
        Hooks.onOperatorDebug();
        var stackTrace = new AtomicReference<String>();

        var checkpoint = Flux
                            .error(new IllegalArgumentException("Oops !!"))
                            .checkpoint()
                            .delayElements(Duration.ofMillis(1));
        log.info("Flux instantiated");
        StepVerifier
            .create(checkpoint)
            .expectErrorMatches(throwable -> {
                log.info("Entering expectErrorMatches -> test");
                stackTrace.set(stackTraceToString(throwable));
                log.info("Leaving expectErrorMatches -> test");
                return throwable instanceof IllegalArgumentException;
            })
            .verify();

        Assert
            .assertTrue(stackTrace
                            .get()
                            .contains("Error has been observed at the following site(s):" )
            );

        log.info("Leaving CheckpointTest : checkpoint");
    }

    private static String stackTraceToString(Throwable throwable) {
        log.info("Entering CheckpointTest : stackTraceToString");
        try (var sw = new StringWriter(); var pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
            log.info("Leaving CheckpointTest : stackTraceToString");
            return sw.toString();
        }
        catch (Exception ioEx) {
            throw new RuntimeException(ioEx);
        }
    }
}
