package com.github.kimble;

import java.util.function.BiConsumer;

public interface TestFactory {

    void produceTests(BiConsumer<String, GeneratedTest> sink);

}
