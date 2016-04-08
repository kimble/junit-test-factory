package com.github.kimble;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.util.function.BiConsumer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(TestFactoryRunner.class)
public class TestFactoryRunnerTest implements TestFactory {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Override
    public void produceTests(BiConsumer<String, GeneratedTest> sink) {
        for (int i=0; i<10; i++) {
            final int number = i;
            final String name = String.format("Test %d", number);

            sink.accept(name, () -> {
                System.out.println("Yey! " + number);
                assertNotNull(tmp);
                assertTrue(tmp.getRoot().exists());
            });
        }
    }



}