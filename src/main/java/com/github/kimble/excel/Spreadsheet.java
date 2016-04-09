package com.github.kimble.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.RETURN_BLANK_AS_NULL;

public class Spreadsheet {

    private final Workbook workbook;

    public Spreadsheet(Supplier<InputStream> iss) throws IOException {
        try (InputStream in = iss.get()) {
            workbook = new XSSFWorkbook(in);
        }
    }

    public static Spreadsheet fromClasspath(String cp) throws IOException {
        URL resource = Spreadsheet.class.getClassLoader().getResource(cp);
        if (resource == null) {
            throw new IllegalStateException("Resource not found: " + cp);
        }

        return new Spreadsheet(() -> {
            try {
                return resource.openStream();
            } catch (IOException ex) {
                throw new IllegalStateException("Failed to open stream for: " + cp);
            }
        });
    }


    public S getSheet(String name) {
        Sheet sheet = workbook.getSheet(name);
        return new S(sheet);
    }



    private static abstract class Element<E> {

        protected final E element;

        protected Element(E element) {
            this.element = element;
        }

        public final E getElement() {
            return element;
        }

        @Override
        public String toString() {
            return element.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Element)) return false;

            Element<?> element1 = (Element<?>) o;
            return element.equals(element1.element);
        }

        @Override
        public int hashCode() {
            return element.hashCode();
        }

    }


    public static class S extends Element<Sheet> {

        public S(Sheet sheet) {
            super(sheet);
        }

        public void streamRowsSkippingHeader(Consumer<R> rowConsumer) {
            int r = element.getLastRowNum();

            for (int i=1; i<=r; i++) {
                R row = new R(element.getRow(i));
                rowConsumer.accept(row);
            }
        }


    }


    public static class R extends Element<Row> {

        public R(Row row) {
            super(row);
        }

        public int intColumn(int nr) {
            Cell cell = element.getCell(nr, RETURN_BLANK_AS_NULL);
            if (cell == null) {
                throw new IllegalStateException("No cell " + nr + " in row " + element.getRowNum());
            }

            Double value = cell.getNumericCellValue();
            return value.intValue();
        }


    }



}
