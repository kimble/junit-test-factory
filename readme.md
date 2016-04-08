JUnit test factory runner
=========================

Simple JUnit runner allowing you to dynamically generate tests with human readable names.
This can be pretty convenient for adding customer generated test data into your test suites.

Btw, as far as I can tell, doing these things will be easier with JUnit 5.

Teaser
------

    package com.github.kimble;

    import org.junit.runner.RunWith;

    import java.util.function.BiConsumer;

    import static org.junit.Assert.assertNotNull;
    import static org.junit.Assert.assertTrue;

    @RunWith(TestFactoryRunner.class)
    public class TestFactoryRunnerTest implements TestFactory {

        @Override
        public void produceTests(BiConsumer<String, GeneratedTest> sink) {
            for (int i=0; i<10; i++) {
                final int number = i;
                final String name = String.format("Test %d", number);

                sink.accept(name, () -> {
                    System.out.println("This is test #" + number);
                });
            }
        }

    }