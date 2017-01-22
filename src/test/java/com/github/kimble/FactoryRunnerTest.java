package com.github.kimble;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.InitializationError;

import java.util.List;
import java.util.function.BiConsumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class FactoryRunnerTest {


    @Test
    public void test_production_failure() throws Exception {
        FactoryRunner runner = new FactoryRunner(BadFactory.class);
        List<FactoryRunner.DescribedTest> children = runner.getChildren();

        assertEquals("the only test is the one that will throw the already cought exception", 1, children.size());

        Description description = children.get(0).description();
        assertEquals("test-production-failure", description.getMethodName());
        assertEquals("BadFactory", description.getTestClass().getSimpleName());

        try {
            FactoryRunner.Test test = children.get(0).test();
            test.execute();
            fail("Expected this to fail");
        }
        catch (Throwable throwable) {
            assertEquals("Exception was thrown during 'produceTests' of com.github.kimble.FactoryRunnerTest$BadFactory", throwable.getMessage());
            assertEquals("Faen..", throwable.getCause().getMessage());
        }
    }



    public static class BadFactory implements FactoryRunner.Producer {

        @Override
        public void produceTests(BiConsumer<String, FactoryRunner.Test> sink) {
            throw new IllegalStateException("Faen..");
        }

    }


    @Test(expected = InitializationError.class)
    public void runners_should_specify_runner() throws Exception {
        new FactoryRunner(RunnerNotImplementingInterface.class);
    }

    public static class RunnerNotImplementingInterface { }

}