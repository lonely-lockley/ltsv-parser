package com.github.lolo.ltsv;

import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class LtsvParserSingleLineLenientTest {

    @Test
    public void testSingleLineLenientDefaults() {
        LtsvParser parser = LtsvParser.builder().lenient().build();
        Iterator<Map<String, String>> it = parser.parse("abc:1\tdef:2", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("abc", "1"));
        assertThat(data, hasEntry("def", "2"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineLenientDefaultsNonEnglish() {
        LtsvParser parser = LtsvParser.builder().lenient().build();
        Iterator<Map<String, String>> it = parser.parse("абв:где\tжзи:2", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("абв", "где"));
        assertThat(data, hasEntry("жзи", "2"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineLenientDefaults2() {
        LtsvParser parser = LtsvParser.builder().lenient().build();
        Iterator<Map<String, String>> it = parser.parse("abc:1\tdef\t:hij\tklm", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains three entries", 3, data.size());
        assertThat(data, hasEntry("abc", "1"));
        assertThat(data, hasEntry("def\t", "hij"));
        assertThat(data, hasEntry("klm", null));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleEmptyLineLenientDefaults() {
        LtsvParser parser = LtsvParser.builder().lenient().build();
        Iterator<Map<String, String>> it = parser.parse("", StandardCharsets.UTF_8);
        assertFalse("Iterator does not have any items left", it.hasNext());
        parser = LtsvParser.builder().lenient().build();
        it = parser.parse("", StandardCharsets.UTF_8);
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineLenientHasExcessiveEntryDelimiter() {
        LtsvParser parser = LtsvParser.builder().lenient().build();
        Iterator<Map<String, String>> it = parser.parse("abc:1\tdef:2\t\n", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("abc", "1"));
        assertThat(data, hasEntry("def", "2"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineLenientHasExcessiveEntryDelimiterAndEmptyValue() {
        LtsvParser parser = LtsvParser.builder().lenient().build();
        Iterator<Map<String, String>> it = parser.parse("abc:1\tdef:\t\n", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("abc", "1"));
        assertThat(data, hasEntry("def", null));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineLenientQuote() {
        LtsvParser parser = LtsvParser.builder().lenient().withQuoteChar('`').build();
        Iterator<Map<String, String>> it = parser.parse("abc:`1 \n\t\t 2`\tdef:3", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("abc", "1 \n\t\t 2"));
        assertThat(data, hasEntry("def", "3"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineLenientKv() {
        LtsvParser parser = LtsvParser.builder().lenient().withQuoteChar('`').withKvDelimiter('=').build();
        Iterator<Map<String, String>> it = parser.parse("abc=`1 \n\t\t 2`\tdef=3", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("abc", "1 \n\t\t 2"));
        assertThat(data, hasEntry("def", "3"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineLenientEscape() {
        LtsvParser parser = LtsvParser.builder().lenient().withQuoteChar('`').withKvDelimiter('=').withEscapeChar('#').build();
        Iterator<Map<String, String>> it = parser.parse("abc=`1 \n\t\t 2#` 3`\tdef=4#\t5", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("abc", "1 \n\t\t 2` 3"));
        assertThat(data, hasEntry("def", "4\t5"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineLenientDelimiter() {
        LtsvParser parser = LtsvParser.builder().lenient().withQuoteChar('`').withKvDelimiter('=').withEscapeChar('#').withEntryDelimiter('|').build();
        Iterator<Map<String, String>> it = parser.parse("abc=`1 \n|\t 2#` 3`|def=4#|5", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("abc", "1 \n|\t 2` 3"));
        assertThat(data, hasEntry("def", "4|5"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineLenientEmptyKey() {
        LtsvParser parser = LtsvParser.builder().lenient().build();
        Iterator<Map<String, String>> it = parser.parse("abc:1\t:2", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("abc", "1"));
        assertThat(data, hasEntry(null, "2"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineLenientEmptyKey2() {
        LtsvParser parser = LtsvParser.builder().lenient().build();
        Iterator<Map<String, String>> it = parser.parse(":", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result should be empty", 0, data.size());
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineLenientUnexpectedKvDelimiter() {
        LtsvParser parser = LtsvParser.builder().lenient().build();
        Iterator<Map<String, String>> it = parser.parse(":abc:1", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains one entry", 1, data.size());
        assertThat(data, hasEntry(null, "abc:1"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineLenientUnexpectedQuoteKey() {
        LtsvParser parser = LtsvParser.builder().lenient().withQuoteChar('`').build();
        Iterator<Map<String, String>> it = parser.parse("`abc`:1", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains one entry", 1, data.size());
        assertThat(data, hasEntry("`abc`", "1"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineLenientUnexpectedEscape() {
        LtsvParser parser = LtsvParser.builder().lenient().withEscapeChar('#').build();
        Iterator<Map<String, String>> it = parser.parse("#:abc:1", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains one entry", 1, data.size());
        assertThat(data, hasEntry(":abc", "1"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineLenientEmptyValue() {
        LtsvParser parser = LtsvParser.builder().lenient().build();
        Iterator<Map<String, String>> it = parser.parse("abc:1\thij:", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("abc", "1"));
        assertThat(data, hasEntry("hij", null));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineLenientQuotedEmptyValue() {
        LtsvParser parser = LtsvParser.builder().lenient().withQuoteChar('`').build();
        Iterator<Map<String, String>> it = parser.parse("abc:1\thij:``", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("abc", "1"));
        assertThat(data, hasEntry("hij", null));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineLenientTrimKeys() {
        LtsvParser parser = LtsvParser.builder().lenient().withQuoteChar('`').trimKeys().build();
        Iterator<Map<String, String>> it = parser.parse("abc :1 \t hij :``\t: klm", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains three entries", 3, data.size());
        assertThat(data, hasEntry("abc", "1 "));
        assertThat(data, hasEntry("hij", null));
        assertThat(data, hasEntry(null, " klm"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineLenientTrimValues() {
        LtsvParser parser = LtsvParser.builder().lenient().withQuoteChar('`').trimValues().build();
        Iterator<Map<String, String>> it = parser.parse("abc :1 \t hij :``\t: klm", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains three entries", 3, data.size());
        assertThat(data, hasEntry("abc ", "1"));
        assertThat(data, hasEntry(" hij ", null));
        assertThat(data, hasEntry(null, "klm"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineLenientTrimBoth() {
        LtsvParser parser = LtsvParser.builder().lenient().withQuoteChar('`').trimKeys().trimValues().build();
        Iterator<Map<String, String>> it = parser.parse("abc :1 \t hij :``\t: klm", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains three entries", 3, data.size());
        assertThat(data, hasEntry("abc", "1"));
        assertThat(data, hasEntry("hij", null));
        assertThat(data, hasEntry(null, "klm"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineLenientKeyStartsWithQoute() {
        LtsvParser parser = LtsvParser.builder().lenient().withQuoteChar('`').build();
        Iterator<Map<String, String>> it = parser.parse("abc:1\t`hij`:2", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("abc", "1"));
        assertThat(data, hasEntry("hij", "2"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineLenientKeyStartsWithEscape() {
        LtsvParser parser = LtsvParser.builder().lenient().withEscapeChar('|').withQuoteChar('`').build();
        Iterator<Map<String, String>> it = parser.parse("abc:1\t|`hij:", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("abc", "1"));
        assertThat(data, hasEntry("`hij", null));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineLenientKeyContainsEntryDelimiter() {
        LtsvParser parser = LtsvParser.builder().lenient().build();
        Iterator<Map<String, String>> it = parser.parse("a\tbc:1\thij:2", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("a\tbc", "1"));
        assertThat(data, hasEntry("hij", "2"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineLenientEmptyValueSkip() {
        LtsvParser parser = LtsvParser.builder().lenient().skipNullValues().build();
        Iterator<Map<String, String>> it = parser.parse("abc:1\thij:", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains one entry", 1, data.size());
        assertThat(data, hasEntry("abc", "1"));
        assertThat(data, not(hasEntry("hij", null)));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineLenientUnexpectedEntryDelimiter() {
        LtsvParser parser = LtsvParser.builder().lenient().build();
        Iterator<Map<String, String>> it = parser.parse("\tabc:1", StandardCharsets.UTF_8);
        Map<String, String> data = it.next();
        assertEquals("Result contains one entry", 1, data.size());
        assertThat(data, hasEntry("\tabc", "1"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineLenientUnexpectedQuoteValue() {
        LtsvParser parser = LtsvParser.builder().withQuoteChar('`').lenient().build();
        Iterator<Map<String, String>> it = parser.parse("abc:`0`1`\tdef:2\thij:`3`\tklm:4", StandardCharsets.UTF_8);
        Map<String, String> data = it.next();
        assertEquals("Result contains four entries", 4, data.size());
        assertThat(data, hasEntry("abc", "01`"));
        assertThat(data, hasEntry("def", "2"));
        assertThat(data, hasEntry("hij", "3"));
        assertThat(data, hasEntry("klm", "4"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

}
