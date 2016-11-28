package com.github.kimble;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

abstract class AbstractRunnerTest {

    protected JournalingRunListener runClass(Class<TestFactoryRunnerNameRule.TestClass> testClass) throws InitializationError {
        RunNotifier notifier = new RunNotifier();
        FactoryRunner runner = new FactoryRunner(testClass);
        JournalingRunListener listener = new JournalingRunListener();
        notifier.addListener(listener);
        runner.run(notifier);

        return listener;
    }

    static class JournalingRunListener extends RunListener {

        final Set<Description> finished = new HashSet<>();
        final Set<Failure> failures = new HashSet<>();

        @Override
        public void testFinished(Description description) throws Exception {
            finished.add(description);
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
            failures.add(failure);
        }


        Set<String> successfulTests() {
            Set<String> failed = failedTests();

            return finished.stream()
                    .map(Description::getMethodName)
                    .filter(name -> !failed.contains(name))
                    .collect(Collectors.toSet());
        }

        Set<String> failedTests() {
            return failures.stream()
                    .map(failure -> failure.getDescription().getMethodName())
                    .collect(Collectors.toSet());
        }

    }

}
