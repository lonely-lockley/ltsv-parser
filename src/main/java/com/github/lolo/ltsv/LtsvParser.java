package com.github.lolo.ltsv;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import static com.github.lolo.ltsv.ParseMode.*;

public class LtsvParser {

    private int entryDelimiter = '\t';

    private int kvDelimiter = ':';

    private int escapeChar = '\\';

    private int quoteChar = '\"';

    private int lineEnding = '\n';

    private boolean strict = true;

    private LinkedList<ParseMode> mode = new LinkedList<>();

    public LtsvParser() {}

    public LtsvParser(char entryDelimiter, char kvDelimiter, char escapeChar, char quoteChar, char lineEnding, boolean strict) {
        this.entryDelimiter = entryDelimiter;
        this.kvDelimiter = kvDelimiter;
        this.escapeChar = escapeChar;
        this.quoteChar = quoteChar;
        this.lineEnding = lineEnding;
        this.strict = strict;
    }

    public Iterator<Map<String, String>> parse(String data, Charset charset) {
        return parse(new ByteArrayInputStream(data.getBytes(charset)));
    }

    public Iterator<Map<String, String>> parse(InputStream data) {
        return LineIterator.newIterator(data, this::parseLine);
    }

    private Map<String, String> parseLine(InputStream data, int lineNum) throws IOException {
        mode.push(KEY);
        StringBuilder key = new StringBuilder(128);
        StringBuilder value = new StringBuilder(1024);
        Map<String, String> result = new HashMap<>();
        int position = 0;
        while (data.available() > 0) {
            int c = data.read();
            position++;
            switch (mode.peek()) {
                case KEY: {
                    if (c == lineEnding) {
                        continue;
                    }
                    if (c == entryDelimiter) {
                        if (strict) {
                            throw new ParseLtsvException(String.format("Key without a value at line [%d] position [%d]", lineNum, position));
                        }
                        if (key.length() > 0) {
                            result.put(key.toString(), null);
                            key.setLength(0);
                        }
                        continue;
                    }
                    if (c == escapeChar || c == quoteChar || (c == kvDelimiter && key.length() == 0)) {
                        throw new ParseLtsvException(String.format("Unexpected token [%c] at line [%d] position [%d]", c, lineNum, position));
                    }
                    if (c == kvDelimiter) {
                        if (key.length() == 0) {
                            throw new ParseLtsvException(String.format("Empty key detected at line [%d] position [%d]", lineNum, position));
                        }
                        mode.pop();
                        mode.push(VALUE);
                        continue;
                    }
                    else {
                        key.append((char) c);
                    }
                    break;
                }
                case VALUE: {
                    if (c == quoteChar && value.length() == 0) {
                        mode.push(QUOTED);
                        continue;
                    }
                    if (c == escapeChar) {
                        mode.push(ESCAPED);
                        continue;
                    }
                    if (c == entryDelimiter) {
                        if (value.length() == 0) {
                            result.put(key.toString(), null);
                        }
                        else {
                            result.put(key.toString(), value.toString());
                        }
                        key.setLength(0);
                        value.setLength(0);
                        mode.pop();
                        mode.push(KEY);
                        continue;
                    }
                    value.append((char) c);
                    break;
                }
                case ESCAPED: {
                    value.append((char) c);
                    mode.pop();
                    break;
                }
                case QUOTED: {
                    if (c == quoteChar) {
                        mode.pop();
                        continue;
                    }
                    value.append((char) c);
                    break;
                }
            }
        }
        if (key.length() > 0) {
            if (value.length() == 0) {
                result.put(key.toString(), null);
            }
            else {
                result.put(key.toString(), value.toString());
            }
        }
        else
        if (strict) {
            throw new ParseLtsvException(String.format("Key without a value at line [%d] position [%d]", lineNum, position));
        }
        mode.clear();
        return result;
    }
}
