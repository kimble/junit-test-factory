package com.github.kimble;


import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class TestFactoryRunner extends ParentRunner<GeneratedTest> {

    private final TestFactory mill;

    private final Map<GeneratedTest, Description> tests = new LinkedHashMap<>();

    public TestFactoryRunner(Class<?> testClass) throws InitializationError {
        super(verify(testClass));

        mill = createInstance(testClass);
    }

    private TestFactory createInstance(Class<?> testClass) throws InitializationError {
        try {
            return (TestFactory) testClass.newInstance();
        }
        catch (Exception ex) {
            throw new InitializationError(ex);
        }
    }

    @Override
    protected List<GeneratedTest> getChildren() {
        Class<?> testClass = mill.getClass();

        mill.produceTests((name, test) -> {
            Description description = Description.createTestDescription(testClass, name);
            tests.put(test, description);
        });

        return tests.keySet()
                .stream()
                .collect(Collectors.toList());
    }

    @Override
    protected Description describeChild(GeneratedTest child) {
        return tests.get(child);
    }

    @Override
    protected void runChild(GeneratedTest test, RunNotifier notifier) {
        Description description = tests.get(test);
        EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
        eachNotifier.fireTestStarted();

        try {
            test.execute();
        }
        catch (AssumptionViolatedException e) {
            eachNotifier.addFailedAssumption(e);
        }
        catch (Throwable e) {
            eachNotifier.addFailure(e);
        }
        finally {
            eachNotifier.fireTestFinished();
        }
    }

    private static Class<?> verify(Class<?> testClass) throws InitializationError {
        if (!TestFactory.class.isAssignableFrom(testClass)) {
            throw new InitializationError(testClass + " must implement " + TestFactory.class);
        }

        return testClass;
    }

}
