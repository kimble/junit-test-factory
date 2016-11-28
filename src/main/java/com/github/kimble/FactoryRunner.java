package com.github.kimble;


import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.rules.RunRules;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;


public class FactoryRunner extends ParentRunner<FactoryRunner.Test> {

    private final Producer factoryInstance;

    private final Map<Test, Description> tests = new LinkedHashMap<>();

    public FactoryRunner(Class<?> testClass) throws InitializationError {
        super(verify(testClass));

        factoryInstance = createInstance(testClass);
    }

    private Producer createInstance(Class<?> testClass) throws InitializationError {
        try {
            return (Producer) testClass.newInstance();
        }
        catch (Exception ex) {
            throw new InitializationError(ex);
        }
    }

    @Override
    protected List<Test> getChildren() {
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
    protected Description describeChild(Test child) {
        return tests.get(child);
    }

    private static Class<?> verify(Class<?> testClass) throws InitializationError {
        if (!Producer.class.isAssignableFrom(testClass)) {
            throw new InitializationError(testClass + " must implement " + Producer.class);
        }

        return testClass;
    }

    @Override
    protected void runChild(Test test, RunNotifier notifier) {
        Description description = tests.get(test);
        EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
        eachNotifier.fireTestStarted();

        Statement statement = createStatement(test, description);

        try {
            statement.evaluate();
        }
        catch (AssumptionViolatedException ex) {
            eachNotifier.addFailedAssumption(ex);
        }
        catch (Throwable ex) {
            eachNotifier.addFailure(ex);
        }
        finally {
            eachNotifier.fireTestFinished();
        }
    }

    private Statement createStatement(Test test, Description description) {
        Statement invokeTest = new GeneratedTestStatementAdapter(test);
        Statement withBefores = withBefores(invokeTest);
        Statement withAfters = withAfters(withBefores);


        return withRules(description, withAfters);
    }

    private Statement withBefores(Statement statement) {
        List<FrameworkMethod> befores = methodsAnnotatedWith(Before.class);

        if (befores.isEmpty()) {
            return statement;
        }
        else {
            return new RunBefores(statement, befores, factoryInstance);
        }
    }

    private Statement withAfters(Statement statement) {
        List<FrameworkMethod> afters = methodsAnnotatedWith(After.class);

        if (afters.isEmpty()) {
            return statement;
        }
        else {
            return new RunAfters(statement, afters, factoryInstance);
        }
    }

    private RunRules withRules(Description description, Statement invokeTest) {
        List<TestRule> result = getTestClass().getAnnotatedMethodValues(factoryInstance, Rule.class, TestRule.class);
        result.addAll(getTestClass().getAnnotatedFieldValues(factoryInstance, Rule.class, TestRule.class));

        return new RunRules(invokeTest, result, description);
    }

    private List<FrameworkMethod> methodsAnnotatedWith(Class<? extends Annotation> annotationClass) {
        return getTestClass().getAnnotatedMethods(annotationClass);
    }


    private static class GeneratedTestStatementAdapter extends Statement {

        private final Test test;

        private GeneratedTestStatementAdapter(Test test) {
            this.test = test;
        }

        @Override
        public void evaluate() throws Throwable {
            test.execute();
        }

    }

    @FunctionalInterface
    public interface Test {

        void execute() throws Throwable;

    }

    @FunctionalInterface
    public interface Producer {

        void produceTests(BiConsumer<String, Test> sink);

    }


}
