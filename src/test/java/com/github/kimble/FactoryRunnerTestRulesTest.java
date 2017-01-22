package com.github.kimble;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertTrue;

public class FactoryRunnerTestRulesTest {


    @Test
    public void verify_before_after_methods() throws Exception {
        AtomicReference<Failure> f = new AtomicReference<>();

        FactoryRunner runner = new FactoryRunner(DynRules.class);
        RunNotifier notifier = new RunNotifier();
        notifier.addListener(new RunListener() {

            @Override
            public void testFailure(Failure failure) throws Exception {
                f.set(failure);
            }

        });

        runner.run(notifier);

        if (f.get() != null) {
            throw new Exception("Fail: " + f.get().getMessage(), f.get().getException());
        }


        // Todo: How to verify @After method..?
    }


    public static class DynRules implements FactoryRunner.Producer {

        @Override
        public void produceTests(FactoryRunner.TestConsumer sink) throws Throwable {
            sink.accept("test", new MyTest());
        }

        public static class MyTest implements FactoryRunner.Test {

            private volatile boolean beforeExecuted = false;
            private volatile boolean afterExecuted = false;


            @Before
            public void vorspiel() throws Exception {
                beforeExecuted = true;
            }

            @After
            public void nachspiel() throws Exception {
                afterExecuted = true;
            }

            @Override
            public void execute() throws Throwable {
                assertTrue("@Before annotated method has not been executed", beforeExecuted);
            }

        }
    }

}