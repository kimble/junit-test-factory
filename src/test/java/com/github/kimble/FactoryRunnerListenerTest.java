package com.github.kimble;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

import java.util.List;
import java.util.function.BiConsumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

public class FactoryRunnerListenerTest {


    @Test
    public void listener() throws Exception {
        FactoryRunner runner = new FactoryRunner(Success.class);
        List<FactoryRunner.DescribedTest> children = runner.getChildren();

        RunNotifier notifier = new RunNotifier();
        CountingRunListener listener = new CountingRunListener();
        notifier.addListener(listener);

        runner.runChild(children.get(0), notifier);
        runner.runChild(children.get(1), notifier);
        runner.runChild(children.get(2), notifier);

        assertEquals("tests started", 3, listener.testsStarted);
        assertEquals("tests finished", 3, listener.testsFinished);
        assertEquals("tests failed", 1, listener.testFailures);
        assertEquals("assumptions failed", 1, listener.testsAssumptionFailed);
        assertEquals("tests ignored", 0, listener.testsIgnored);
    }


    public static class Success implements FactoryRunner.Producer {

        @Override
        public void produceTests(BiConsumer<String, FactoryRunner.Test> sink) {
            sink.accept("success", () -> { /* no problem */ });
            sink.accept("failure", () -> { fail("faen.."); });
            sink.accept("assumption", () -> { assumeTrue(false); });
        }

    }


    private static class CountingRunListener extends RunListener {

        volatile int runStarted, runFinished;
        volatile int testsStarted, testsFinished, testFailures, testsAssumptionFailed, testsIgnored;

        @Override
        public void testRunStarted(Description description) throws Exception {
            runStarted++;
        }

        @Override
        public void testRunFinished(Result result) throws Exception {
            runFinished++;
        }

        @Override
        public void testStarted(Description description) throws Exception {
            testsStarted++;
        }

        @Override
        public void testFinished(Description description) throws Exception {
            testsFinished++;
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
            testFailures++;
        }

        @Override
        public void testAssumptionFailure(Failure failure) {
            testsAssumptionFailed++;
        }

        @Override
        public void testIgnored(Description description) throws Exception {
            testsIgnored++;
        }

    }

}