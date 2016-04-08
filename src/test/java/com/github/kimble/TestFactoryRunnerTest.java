package com.github.kimble;

import org.junit.After;
import org.junit.Before;
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

    @Before
    public void doBeforeEachTest() {
        System.out.println("Before...");
    }

    @After
    public void doAfterEachTest() {
        System.out.println("After...");
    }

    @Override
    public void produceTests(BiConsumer<String, GeneratedTest> sink) {
        for (int i=0; i<10; i++) {
            final int number = i;
            final String name = String.format("Test %d", number);

            sink.accept(name, () -> {
                System.out.println("Yey! " + number);
                System.out.println(" => " + tmp.getRoot());

                assertNotNull(tmp);
                assertTrue(tmp.getRoot().exists());
            });
        }
    }

}