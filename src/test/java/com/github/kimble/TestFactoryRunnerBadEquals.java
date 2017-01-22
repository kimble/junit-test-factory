package com.github.kimble;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(FactoryRunner.class)
public class TestFactoryRunnerBadEquals implements FactoryRunner.Producer {

    private static Set<String> executed = new HashSet<>();

    @Rule
    public TestName testName = new TestName();

    @AfterClass
    public static void afterClass() {
        Set<String> expected = new HashSet<>();
        expected.add("a");
        expected.add("b");

        assertEquals(expected, executed);
    }

    @Override
    public void produceTests(FactoryRunner.TestConsumer sink) throws Throwable {
        sink.accept("a", () -> { executed.add(testName.getMethodName()); });
        sink.accept("b", () -> { executed.add(testName.getMethodName()); });
    }

}
