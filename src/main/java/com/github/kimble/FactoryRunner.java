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
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;


public class FactoryRunner extends ParentRunner<FactoryRunner.DescribedTest> {

    private final Producer factoryInstance;

    private final List<DescribedTest> tests = new ArrayList<>();

    @SuppressWarnings("WeakerAccess")
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
    protected List<DescribedTest> getChildren() {
        Class<?> testClass = factoryInstance.getClass();

        factoryInstance.produceTests((name, test) -> {
            Description description = Description.createTestDescription(testClass, name);
            tests.add(new DescribedTest(test, description));
        });

        return tests;
    }

    @Override
    protected Description describeChild(DescribedTest describedTest) {
        return describedTest.description();
    }

    private static Class<?> verify(Class<?> testClass) throws InitializationError {
        if (!Producer.class.isAssignableFrom(testClass)) {
            throw new InitializationError(testClass + " must implement " + Producer.class);
        }

        return testClass;
    }

    @Override
    protected void runChild(DescribedTest describedTest, RunNotifier notifier) {
        EachTestNotifier eachNotifier = new EachTestNotifier(notifier, describedTest.description());
        eachNotifier.fireTestStarted();

        Statement statement = createStatement(describedTest);

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



    private Statement createStatement(DescribedTest describedTest) {
        Statement invokeTest = new GeneratedTestStatementAdapter(describedTest.test());
        Statement withBefores = withBefores(invokeTest);
        Statement withAfters = withAfters(withBefores);

        Description description = describedTest.description();
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

    static class DescribedTest {

        private final Test test;
        private final Description description;

        DescribedTest(Test test, Description description) {
            this.description = description;
            this.test = test;
        }

        Test test() {
            return test;
        }

        Description description() {
            return description;
        }

        @Override
        public String toString() {
            return description.toString();
        }

    }


}
