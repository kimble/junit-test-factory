package com.github.kimble.excel;

import com.github.kimble.FactoryRunner;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@RunWith(FactoryRunner.class)
public abstract class SpreadsheetDrivenTest implements FactoryRunner.Producer {


    @Override
    public final void produceTests(BiConsumer<String, FactoryRunner.Test> sink) {
        try {
            Spreadsheet spreadsheet = new Spreadsheet(supplySpreadsheet());
            produceTests(spreadsheet, sink);
        }
        catch (IOException ex) {
            throw new IllegalStateException("Failed to load spreadsheet for test", ex);
        }
    }

    protected abstract Supplier<InputStream> supplySpreadsheet();

    abstract void produceTests(Spreadsheet spreadsheet, BiConsumer<String, FactoryRunner.Test> sink);


    final Supplier<InputStream> fromClasspath(String cp) {
        return () -> {
            try {
                URL resource = getClass().getClassLoader().getResource(cp);
                if (resource != null) {
                    return resource.openStream();
                }
                else {
                    throw new IllegalStateException("Resource not found: " + cp);
                }
            } catch (IOException ex) {
                throw new IllegalStateException("Failed to load resource", ex);
            }
        };
    }

}
