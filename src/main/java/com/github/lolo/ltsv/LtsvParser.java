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

    private boolean skipNullValues = false;

    private LinkedList<ParseMode> mode = new LinkedList<>();

    private LtsvParser() {}

    /**
     * Creates a new builder for a parser with default configuration <br>
     * <ul>
     *     <li>entryDelimiter = '\t'</li>
     *     <li>kvDelimiter = ':'</li>
     *     <li>escapeChar = '\\'</li>
     *     <li>quoteChar = '\"'</li>
     *     <li>lineEnding = '\n'</li>
     *     <li>strict = true</li>
     *     <li>skipNullValues = false</li>
     * </ul>
     * ready to parse regular LTSV format <br>
     * <pre>
     *     abc:1    def:2       jhi:3\"
     *     klm:4    nop:"5 6"   qrs:7
     * </pre>
     * @return a new builder
     */
    public static Builder builder() {
        return new LtsvParser().new Builder();
    }

    /**
     * Parses a given string
     * @param data a string to parse
     * @param charset character encoding to extract raw bytes correctly
     * @return iterator containing a new HashMap for each row. If a row is empty, method returns empty Map, otherwise
     * it will be populated with extracted values
     */
    public Iterator<Map<String, String>> parse(String data, Charset charset) {
        return parse(new ByteArrayInputStream(data.getBytes(charset)));
    }

    /**
     * Parses a given input stream to the end
     * @param data a stream to parse
     * @return iterator containing a new HashMap for each row. If a row is empty, method returns empty Map, otherwise
     * it will be populated with extracted values
     */
    public Iterator<Map<String, String>> parse(InputStream data) {
        return LineIterator.newIterator(data, this::parseLine);
    }

    private Map<String, String> parseLine(InputStream data, int lineNum) throws IOException {
        mode.push(KEY);
        StringBuilder key = new StringBuilder(128);
        StringBuilder value = new StringBuilder(1024);
        Map<String, String> result = new HashMap<>();
        int position = 0;
        while (data.available() > 0 && mode.peek() != EOL) {
            int c = data.read();
            position++;
            switch (mode.peek()) {
                case KEY: {
                    if (c == lineEnding) {
                        mode.pop();
                        mode.push(EOL);
                        break;
                    }
                    if (c == entryDelimiter) {
                        if (strict) {
                            throw new ParseLtsvException(String.format("Key without a value at line [%d] position [%d]", lineNum, position));
                        }
                        if (key.length() > 0) {
                            if (skipNullValues == false) result.put(key.toString(), null);
                            key.setLength(0);
                        }
                        continue;
                    }
                    if (c == escapeChar || c == quoteChar) {
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
                    if (c == lineEnding) {
                        mode.pop();
                        mode.push(EOL);
                        break;
                    }
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
                            if (skipNullValues == false) result.put(key.toString(), null);
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
                    if (c == escapeChar) {
                        mode.push(ESCAPED);
                        continue;
                    }
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
                if (strict) {
                    throw new ParseLtsvException(String.format("Key without a value at line [%d] position [%d]", lineNum, position));
                }
                else {
                    if (skipNullValues == false) result.put(key.toString(), null);
                }
            }
            else {
                result.put(key.toString(), value.toString());
            }
        }

        mode.clear();
        return result;
    }

    public class Builder {

        private Builder() {}

        /**
         * Do not tolerate some recoverable errors <br>
         * This is a default mode for parser
         * @return <b>this</b> for chaining
         */
        public Builder strict() {
            LtsvParser.this.strict = true;
            return this;
        }

        /**
         * Recover parser after some kind of errors <br>
         * For example, a key without a value
         * @return <b>this</b> for chaining
         */
        public Builder lenient() {
            LtsvParser.this.strict = false;
            return this;
        }

        /**
         * Sets up a new entry delimiter
         * @param delim new value
         * @return <b>this</b> for chaining
         */
        public Builder withEntryDelimiter(char delim) {
            LtsvParser.this.entryDelimiter = delim;
            return this;
        }

        /**
         * Sets up a new key-value delimiter
         * @param delim new value
         * @return <b>this</b> for chaining
         */
        public Builder withKvDelimiter(char delim) {
            LtsvParser.this.kvDelimiter = delim;
            return this;
        }

        /**
         * Sets up a new escape character
         * @param escape new value
         * @return <b>this</b> for chaining
         */
        public Builder withEscapeChar(char escape) {
            LtsvParser.this.escapeChar = escape;
            return this;
        }

        /**
         * Sets up a new quote character
         * @param quote new value
         * @return <b>this</b> for chaining
         */
        public Builder withQuoteChar(char quote) {
            LtsvParser.this.quoteChar = quote;
            return this;
        }

        /**
         * Sets up a new character for new line detection
         * @param eol new value
         * @return <b>this</b> for chaining
         */
        public Builder withLineEnding(char eol) {
            LtsvParser.this.lineEnding = eol;
            return this;
        }

        /**
         * Sets up a mode when null (e.g. empty string) values will be skipped
         * @return <b>this</b> for chaining
         */
        public Builder skipNullValues() {
            LtsvParser.this.skipNullValues = true;
            return this;
        }

        /**
         * Finishes build process and returns a new parser
         * @return a newly configured LTSV parser
         */
        public LtsvParser build() {
            return LtsvParser.this;
        }
    }
}
