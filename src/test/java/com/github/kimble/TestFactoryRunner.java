package com.github.kimble;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.util.function.BiConsumer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(FactoryRunner.class)
public class TestFactoryRunner implements FactoryRunner.Producer {

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

    @BeforeClass
    public static void doBeforeAnythingElse() {
        System.out.println("Before anything else");
    }

    @AfterClass
    public static void doAfterEverything() {
        System.out.println("This is the very last thing to be done");
    }

    @Override
    public void produceTests(BiConsumer<String, FactoryRunner.Test> sink) {
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