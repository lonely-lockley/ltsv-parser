package com.github.lolo.ltsv;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

public class LineIterator implements Iterator<Map<String, String>> {

    private final InputStream data;

    private final CheckedBiFunction<InputStream, Integer, Map<String, String>> parseLine;

    private int line = 0;

    private LineIterator(InputStream data, CheckedBiFunction<InputStream, Integer, Map<String, String>> parseLine) {
        this.data = data;
        this.parseLine = parseLine;
    }

    static Iterator<Map<String, String>> newIterator(InputStream data, CheckedBiFunction<InputStream, Integer, Map<String, String>> parseLine) {
        return new LineIterator(data, parseLine);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        try {
            return data.available() > 0;
        }
        catch (IOException ex) {
            throw new ParseLtsvException("Error reading data source", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> next() {
        try {
            Map<String, String> result = parseLine.apply(data, line);
            line++;
            return result;
        }
        catch (IOException ex) {
            throw new ParseLtsvException("Error reading data source", ex);
        }
    }

}
