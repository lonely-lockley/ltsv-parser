package com.github.lolo.ltsv;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

    private boolean trimKeys = false;

    private boolean trimValues = false;

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

    private void putEntry(Map<String, String> result, ByteArrayOutputStream key, ByteArrayOutputStream value, int lineNum, int position) {
        if (key.size() > 0) {
            if (value.size() == 0) {
                if (!skipNullValues) {
                    if (trimKeys) {
                        result.put(new String(key.toByteArray(), StandardCharsets.UTF_8).trim(), null);
                    }
                    else {
                        result.put(new String(key.toByteArray(), StandardCharsets.UTF_8), null);
                    }
                }
            }
            else {
                if (trimKeys && trimValues) {
                    result.put(new String(key.toByteArray(), StandardCharsets.UTF_8).trim(), new String(value.toByteArray(), StandardCharsets.UTF_8).trim());
                }
                else
                if (trimKeys && !trimValues) {
                    result.put(new String(key.toByteArray(), StandardCharsets.UTF_8).trim(), new String(value.toByteArray(), StandardCharsets.UTF_8));
                }
                else
                if (!trimKeys && trimValues) {
                    result.put(new String(key.toByteArray(), StandardCharsets.UTF_8), new String(value.toByteArray(), StandardCharsets.UTF_8).trim());
                }
                else {
                    result.put(new String(key.toByteArray(), StandardCharsets.UTF_8), new String(value.toByteArray(), StandardCharsets.UTF_8));
                }
            }
        }
        else {
            if (value.size() > 0) {
                if (strict) {
                    throw new ParseLtsvException(String.format("Empty key detected at line [%d] position [%d]", lineNum, position));
                }
                else {
                    if (trimValues) {
                        result.put(null, new String(value.toByteArray(), StandardCharsets.UTF_8).trim());
                    }
                    else {
                        result.put(null, new String(value.toByteArray(), StandardCharsets.UTF_8));
                    }
                }
            }
        }
        key.reset();
        value.reset();
    }

    /**
     * From now on in comments:
     * <ul>
     *     <li>key = kkk</li>
     *     <li>value = vvv</li>
     *     <li>entryDelimiter = _</li>
     *     <li>kvDelimiter = :</li>
     *     <li>escapeChar = \</li>
     *     <li>quoteChar = "</li>
     *     <li>lineEnding = n</li>
     * </ul>
     */
    private Map<String, String> parseLine(InputStream data, int lineNum) throws IOException {
        mode.push(KEY);
        ByteArrayOutputStream key = new ByteArrayOutputStream(1024);
        ByteArrayOutputStream value = new ByteArrayOutputStream(1024);
        Map<String, String> result = new HashMap<>();
        int position = 0;
        while (data.available() > 0 && mode.peek() != EOL) {
            int c = data.read();
            position++;
            switch (mode.peek()) {
                case KEY: {
                    // kkk:vvvn
                    //        ^
                    if (c == lineEnding) {
                        mode.pop();
                        mode.push(EOL);
                        break;
                    }
                    // kkk_kkk:vvv
                    //    ^
                    if (c == entryDelimiter) {
                        if (strict) {
                            throw new ParseLtsvException(String.format("Key without a value at line [%d] position [%d]", lineNum, position));
                        }
                        key.write(c);
                        continue;
                    }
                    // k"kk:vvv
                    //  ^
                    if (c == quoteChar) {
                        if (strict) {
                            throw new ParseLtsvException(String.format("Unexpected quote token [%c] at line [%d] position [%d]", c, lineNum, position));
                        }
                        key.write(c);
                        continue;
                    }
                    // k\kk:vvv
                    //  ^
                    if (c == escapeChar) {
                        if (strict) {
                            throw new ParseLtsvException(String.format("Unexpected escape token [%c] at line [%d] position [%d]", c, lineNum, position));
                        }
                        mode.push(ESCAPED);
                        continue;
                    }
                    // kkk:vvv
                    //    ^
                    if (c == kvDelimiter) {
                        if (key.size() == 0 && strict) {
                            throw new ParseLtsvException(String.format("Empty key detected at line [%d] position [%d]", lineNum, position));
                        }
                        mode.pop();
                        mode.push(VALUE);
                        continue;
                    }
                    // kkk:vvv
                    //  ^
                    else {
                        key.write(c);
                    }
                    break;
                }
                case VALUE: {
                    // kkk:vvvn
                    //        ^
                    if (c == lineEnding) {
                        mode.pop();
                        mode.push(EOL);
                        break;
                    }
                    // kkk:"vvv"
                    //     ^
                    if (c == quoteChar && value.size() == 0) {
                        mode.push(QUOTED);
                        continue;
                    }
                    // kkk:v\vv
                    //      ^
                    if (c == escapeChar) {
                        mode.push(ESCAPED);
                        continue;
                    }
                    // kkk:vvv_kkk:vvv   or   kkk:"vvv"_kkk:vvv
                    //        ^                        ^
                    if (c == entryDelimiter) {
                        mode.push(ENTRY_DELIMITER);
                        continue;
                    }
                    value.write(c);
                    break;
                }
                // kkk:v\vv
                //       ^
                case ESCAPED: {
                    mode.pop();
                    if (mode.peek() == KEY) {
                        key.write(c);
                    }
                    else {
                        value.write(c);
                    }
                    break;
                }
                // kkk:"vvv"   or   kkk:v\vv   or   kkk:"vvv"
                //       ^               ^                  ^
                case QUOTED: {
                    if (c == escapeChar) {
                        mode.push(ESCAPED);
                        continue;
                    }
                    if (c == quoteChar) {
                        mode.pop();
                        if (strict) {
                            mode.pop();
                            mode.push(VALUE);
                            mode.push(ENTRY_DELIMITER);
                        }
                        continue;
                    }
                    if (mode.peekLast() == KEY) {
                        key.write(c);
                    }
                    else {
                        value.write(c);
                    }
                    break;
                }
                case ENTRY_DELIMITER: {
                    // kkk:vvv_n
                    //         ^
                    if (c == lineEnding) {
                        mode.pop();
                        mode.push(EOL);
                        break;
                    }
                    // kkk_kkk:vvv   or   kkk__kkk:vvv
                    //     ^                  ^
                    if (c == entryDelimiter) {
                        continue;
                    }
                    // kkk:vvv_\kkk:vvv
                    //         ^
                    if (c == escapeChar) {
                        if (strict) {
                            throw new ParseLtsvException(String.format("Unexpected quote token [%c] at line [%d] position [%d]", c, lineNum, position));
                        }
                        putEntry(result, key, value, lineNum, position);
                        mode.pop();
                        mode.pop();
                        mode.push(KEY);
                        continue;
                    }
                    // kkk:vvv_"kkk":vvv
                    //         ^
                    if (c == quoteChar) {
                        if (strict) {
                            throw new ParseLtsvException(String.format("Unexpected escape token [%c] at line [%d] position [%d]", c, lineNum, position));
                        }
                        putEntry(result, key, value, lineNum, position);
                        mode.pop();
                        mode.pop();
                        mode.push(KEY);
                        mode.push(QUOTED);
                        continue;
                    }
                    // kkk_:vvv
                    //     ^
                    if (c == kvDelimiter) {
                        putEntry(result, key, value, lineNum, position);
                        mode.pop();
                        mode.pop();
                        mode.push(VALUE);
                        continue;
                    }
                    mode.pop();
                    putEntry(result, key, value, lineNum, position);
                    if (mode.peek() == KEY) {
                        value.write(c);
                    }
                    else {
                        key.write(c);
                    }
                    mode.pop();
                    mode.push(KEY);
                    break;
                }
            }
        }

        // save last k-v pair
        putEntry(result, key, value, lineNum, position);
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
         * Sets up a mode when leading and trailing spaces for keys are eliminated
         * @return <b>this</b> for chaining
         */
        public Builder trimKeys() {
            LtsvParser.this.trimKeys = true;
            return this;
        }

        /**
         * Sets up a mode when leading and trailing spaces for values are eliminated
         * @return <b>this</b> for chaining
         */
        public Builder trimValues() {
            LtsvParser.this.trimValues = true;
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
