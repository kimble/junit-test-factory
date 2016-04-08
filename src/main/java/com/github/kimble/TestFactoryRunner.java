package com.github.kimble;


import org.junit.Rule;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class TestFactoryRunner extends ParentRunner<GeneratedTest> {

    private final TestFactory factoryInstance;

    private final Map<GeneratedTest, Description> tests = new LinkedHashMap<>();

    public TestFactoryRunner(Class<?> testClass) throws InitializationError {
        super(verify(testClass));

        factoryInstance = createInstance(testClass);
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
        Class<?> testClass = factoryInstance.getClass();

        factoryInstance.produceTests((name, test) -> {
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

        Statement statement = createStatement(test, description);

        try {
            statement.evaluate();
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

    private Statement createStatement(GeneratedTest test, Description description) {
        Statement invokeTest = new InvokeGeneratedTest(test);
        RunRules withRules = new RunRules(invokeTest, getTestRules(factoryInstance), description);


        return withRules;
    }

    private static Class<?> verify(Class<?> testClass) throws InitializationError {
        if (!TestFactory.class.isAssignableFrom(testClass)) {
            throw new InitializationError(testClass + " must implement " + TestFactory.class);
        }

        return testClass;
    }

    private List<TestRule> getTestRules(Object target) {
        List<TestRule> result = getTestClass().getAnnotatedMethodValues(target, Rule.class, TestRule.class);
        result.addAll(getTestClass().getAnnotatedFieldValues(target, Rule.class, TestRule.class));

        return result;
    }


    private static class InvokeGeneratedTest extends Statement {

        private final GeneratedTest generatedTest;

        private InvokeGeneratedTest(GeneratedTest generatedTest) {
            this.generatedTest = generatedTest;
        }

        @Override
        public void evaluate() throws Throwable {
            generatedTest.execute();
        }

    }

}
