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
import java.util.function.Predicate;

/**
 *
 * */
@Slf4j
public class HooksOnOperatorDebugTest {

    @Test
    public void onOperatorDebugViaHooks() {
        log.info("Entering HooksOnOperatorDebugTest : onOperatorDebugViaHooks");
        Hooks.onOperatorDebug();
        var stackTrace = new AtomicReference<String>();

        var errorFlux = Flux
                            .error(new IllegalArgumentException("Oops !!"))
                            .checkpoint()
                            .delayElements(Duration.ofMillis(1));
        log.info("Flux instantiated");
        StepVerifier
            .create(errorFlux)
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
                            .contains("Flux.error ⇢ at " + HooksOnOperatorDebugTest.class.getName()
            ));

        log.info("Leaving HooksOnOperatorDebugTest : onOperatorDebugViaHooks");
    }

    private static String stackTraceToString(Throwable throwable) {
        log.info("Entering HooksOnOperatorDebugTest : stackTraceToString");
        try (var sw = new StringWriter(); var pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
            log.info("Leaving HooksOnOperatorDebugTest : stackTraceToString");
            return sw.toString();
        }
        catch (Exception ioEx) {
            throw new RuntimeException(ioEx);
        }
    }
}
