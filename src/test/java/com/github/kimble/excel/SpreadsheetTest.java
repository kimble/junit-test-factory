package com.github.kimble.excel;

import com.github.kimble.GeneratedTest;

import java.io.InputStream;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class SpreadsheetTest extends SpreadsheetDrivenTest {

    @Override
    protected Supplier<InputStream> supplySpreadsheet() {
        return fromClasspath("excel/simple.xlsx");
    }

    @Override
    void produceTests(Spreadsheet spreadsheet, BiConsumer<String, GeneratedTest> sink) {
        spreadsheet.sheetNamed("plus").streamRowsSkippingHeader(r -> {
            int a = r.intColumn(0);
            int b = r.intColumn(1);
            int sum = r.intColumn(2);

            String name = String.format("Addition: %d + %d = %d", a, b, sum);
            sink.accept(name, () -> assertEquals(name, sum, a + b));
        });

        spreadsheet.sheetNamed("minus").streamRowsSkippingHeader(r -> {
            int a = r.intColumn(0);
            int b = r.intColumn(1);
            int sum = r.intColumn(2);

            String name = String.format("Subtraction: %d - %d = %d", a, b, sum);
            sink.accept(name, () -> assertEquals(name, sum, a - b));
        });
    }

}