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
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;


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


        try {
            factoryInstance.produceTests((name, test) -> {
                Description description = Description.createTestDescription(testClass, name);



                // Todo: Warn if test produces the same test twice
                tests.add(new DescribedTest(description, test));
            });
        }
        catch (Throwable trouble) {
            onTestProductionFailure(testClass, trouble);
        }

        return tests;
    }

    /**
     * This is a bit of a hack.. if we let the exception bubble up nobody will
     * ever pick it up causing confusing. Instead we create a dummy test that will
     * throw the exception when it is executed and thereby bringing it into the light.
     */
    private void onTestProductionFailure(Class<?> testClass, Throwable trouble) {
        Description description = Description.createTestDescription(testClass, "test-production-failure");

        tests.add(new DescribedTest(description, () -> {
            throw new TestProductionFailure("Exception was thrown during 'produceTests' of " + testClass.getName(), trouble);
        }));
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


        try {
            Statement statement = createStatement(describedTest);
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



    private Statement createStatement(DescribedTest describedTest) throws InitializationError {
        TestClass tk = getTestClass();

        // This statement will invoke the actual test
        GeneratedTestStatementAdapter invokeTest = new GeneratedTestStatementAdapter(describedTest.test());

        // Invoke @Before, @After and @Rule on the test factory instance
        Statement withBefores = withBefores(tk, invokeTest, factoryInstance);
        Statement withAfters = withAfters(tk, withBefores, factoryInstance);
        Statement outerRules = withRules(tk, describedTest.description(), withAfters, factoryInstance);

        // Invoke @Before, @After and @Rule on the dynamically produced test
        Statement withInnerBefores = withBefores(invokeTest.simlatedTestClass, outerRules, invokeTest.test);
        Statement withInnerAfters = withAfters(invokeTest.simlatedTestClass, withInnerBefores, invokeTest.test);
        return withRules(invokeTest.simlatedTestClass, describedTest.description(), withInnerAfters, invokeTest.test);
    }

    private Statement withBefores(TestClass tk, Statement statement, Object target) {
        List<FrameworkMethod> befores = methodsAnnotatedWith(tk, Before.class);

        if (befores.isEmpty()) {
            return statement;
        }
        else {
            return new RunBefores(statement, befores, target);
        }
    }

    private Statement withAfters(TestClass tk, Statement statement, Object target) {
        List<FrameworkMethod> afters = methodsAnnotatedWith(tk, After.class);

        if (afters.isEmpty()) {
            return statement;
        }
        else {
            return new RunAfters(statement, afters, target);
        }
    }

    private RunRules withRules(TestClass tk, Description description, Statement statement, Object target) {
        List<TestRule> result = tk.getAnnotatedMethodValues(target, Rule.class, TestRule.class);
        result.addAll(tk.getAnnotatedFieldValues(target, Rule.class, TestRule.class));

        return new RunRules(statement, result, description);
    }

    private List<FrameworkMethod> methodsAnnotatedWith(TestClass tk, Class<? extends Annotation> annotationClass) {
        return tk.getAnnotatedMethods(annotationClass);
    }


    private static class GeneratedTestStatementAdapter extends Statement {

        private final Test test;
        private final TestClass simlatedTestClass;

        private GeneratedTestStatementAdapter(Test test) throws InitializationError {
            this.simlatedTestClass = new TestClass(test.getClass());
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

        void produceTests(TestConsumer sink) throws Throwable;

    }


    public interface TestConsumer {

        void accept(String name, Test test) throws Throwable;

    }

    public static class TestProductionFailure extends IllegalStateException {
        TestProductionFailure(String message, Throwable cause) {
            super(message, cause);
        }
    }

    static class DescribedTest {

        private final Test test;
        private final Description description;

        DescribedTest(Description description, Test test) {
            this.description = description;
            this.test = test;
        }

        Test test() {
            return test;
        }

        Description description() {
            return description;
        }

    }


}
