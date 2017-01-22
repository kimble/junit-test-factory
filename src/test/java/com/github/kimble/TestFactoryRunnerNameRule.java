package com.github.kimble;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class TestFactoryRunnerNameRule extends AbstractRunnerTest {

    @Test
    public void canAccessTestNameRule() throws Exception {
        JournalingRunListener listener = runClass(TestClass.class);

        assertThat(listener.finished)
                .as("Finished tests")
                .hasSize(2);

        assertThat(listener.successfulTests())
                .as("Successful tests")
                .containsOnly("The first test", "The second test");
    }


    @RunWith(FactoryRunner.class)
    public static class TestClass implements FactoryRunner.Producer {

        @Rule
        public TestName name = new TestName();

        @Override
        public void produceTests(BiConsumer<String, FactoryRunner.Test> sink) {
            sink.accept("The first test", () -> {
                assertEquals("The first test", name.getMethodName());
            });
            sink.accept("The second test", () -> {
                assertEquals("The second test", name.getMethodName());
            });
        }

    }

}