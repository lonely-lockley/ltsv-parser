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

public class LtsvParserSingleLineStrictTest {

    @Test
    public void testSingleLineStrictDefaults() {
        LtsvParser parser = LtsvParser.builder().strict().build();
        Iterator<Map<String, String>> it = parser.parse("abc:1\tdef:2", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("abc", "1"));
        assertThat(data, hasEntry("def", "2"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineStrictDefaultsNonEnglish() {
        LtsvParser parser = LtsvParser.builder().strict().build();
        Iterator<Map<String, String>> it = parser.parse("абв:где\tжзи:2", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("абв", "где"));
        assertThat(data, hasEntry("жзи", "2"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineStrictMultipleEntryDelimiters() {
        LtsvParser parser = LtsvParser.builder().strict().build();
        Iterator<Map<String, String>> it = parser.parse("abc:1\t\t\tdef:2", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("abc", "1"));
        assertThat(data, hasEntry("def", "2"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleEmptyLineStrictDefaults() {
        LtsvParser parser = LtsvParser.builder().strict().build();
        Iterator<Map<String, String>> it = parser.parse("", StandardCharsets.UTF_8);
        assertFalse("Iterator does not have any items left", it.hasNext());
        parser = LtsvParser.builder().lenient().build();
        it = parser.parse("", StandardCharsets.UTF_8);
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineStrictHasExcessiveEntryDelimiter() {
        LtsvParser parser = LtsvParser.builder().strict().build();
        Iterator<Map<String, String>> it = parser.parse("abc:1\tdef:2\t", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("abc", "1"));
        assertThat(data, hasEntry("def", "2"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineStrictHasExcessiveEntryDelimiterAndEmptyValue() {
        LtsvParser parser = LtsvParser.builder().strict().build();
        Iterator<Map<String, String>> it = parser.parse("abc:1\tdef:\t", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("abc", "1"));
        assertThat(data, hasEntry("def", null));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineStrictQuote() {
        LtsvParser parser = LtsvParser.builder().strict().withQuoteChar('`').build();
        Iterator<Map<String, String>> it = parser.parse("abc:`1 \n\t\t 2`\tdef:3", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("abc", "1 \n\t\t 2"));
        assertThat(data, hasEntry("def", "3"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineStrictKv() {
        LtsvParser parser = LtsvParser.builder().strict().withQuoteChar('`').withKvDelimiter('=').build();
        Iterator<Map<String, String>> it = parser.parse("abc=`1 \n\t\t 2`\tdef=3", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("abc", "1 \n\t\t 2"));
        assertThat(data, hasEntry("def", "3"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineStrictEscape() {
        LtsvParser parser = LtsvParser.builder().strict().withQuoteChar('`').withKvDelimiter('=').withEscapeChar('#').build();
        Iterator<Map<String, String>> it = parser.parse("abc=`1 \n\t\t 2#` 3`\tdef=4#\t5", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("abc", "1 \n\t\t 2` 3"));
        assertThat(data, hasEntry("def", "4\t5"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineStrictDelimiter() {
        LtsvParser parser = LtsvParser.builder().strict().withQuoteChar('`').withKvDelimiter('=').withEscapeChar('#').withEntryDelimiter('|').build();
        Iterator<Map<String, String>> it = parser.parse("abc=`1 \n|\t 2#` 3`|def=4#|5", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("abc", "1 \n|\t 2` 3"));
        assertThat(data, hasEntry("def", "4|5"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineStrictEmptyValue() {
        LtsvParser parser = LtsvParser.builder().strict().build();
        Iterator<Map<String, String>> it = parser.parse("abc:1\thij:", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("abc", "1"));
        assertThat(data, hasEntry("hij", null));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testSingleLineStrictQuotedEmptyValue() {
        LtsvParser parser = LtsvParser.builder().strict().withQuoteChar('`').build();
        Iterator<Map<String, String>> it = parser.parse("abc:1\thij:``", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("abc", "1"));
        assertThat(data, hasEntry("hij", null));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test(expected = ParseLtsvException.class)
    public void testSingleLineStrictEmptyKey() {
        LtsvParser parser = LtsvParser.builder().strict().build();
        Iterator<Map<String, String>> it = parser.parse("abc:1\t:2", StandardCharsets.UTF_8);
        it.next();
    }

    @Test(expected = ParseLtsvException.class)
    public void testSingleLineStrictEmptyKey2() {
        LtsvParser parser = LtsvParser.builder().strict().build();
        Iterator<Map<String, String>> it = parser.parse(":", StandardCharsets.UTF_8);
        it.next();
    }

    @Test(expected = ParseLtsvException.class)
    public void testSingleLineStrictUnexpectedKvDelimiter() {
        LtsvParser parser = LtsvParser.builder().strict().build();
        Iterator<Map<String, String>> it = parser.parse(":abc:1", StandardCharsets.UTF_8);
        it.next();
    }

    @Test(expected = ParseLtsvException.class)
    public void testSingleLineStrictUnexpectedQuoteKey() {
        LtsvParser parser = LtsvParser.builder().strict().withQuoteChar('`').build();
        Iterator<Map<String, String>> it = parser.parse("`abc`:1", StandardCharsets.UTF_8);
        it.next();
    }

    @Test(expected = ParseLtsvException.class)
    public void testSingleLineStrictUnexpectedEscape() {
        LtsvParser parser = LtsvParser.builder().strict().withEscapeChar('#').build();
        Iterator<Map<String, String>> it = parser.parse("#:abc:1", StandardCharsets.UTF_8);
        it.next();
    }

    @Test(expected = ParseLtsvException.class)
    public void testSingleLineStrictUnexpectedEntryDelimiter() {
        LtsvParser parser = LtsvParser.builder().strict().build();
        Iterator<Map<String, String>> it = parser.parse("\tabc:1", StandardCharsets.UTF_8);
        it.next();
    }

    @Test(expected = ParseLtsvException.class)
    public void testSingleLineStrictUnexpectedQuoteValue() {
        LtsvParser parser = LtsvParser.builder().withQuoteChar('`').strict().build();
        Iterator<Map<String, String>> it = parser.parse("abc:`0`1`\tdef:2\thij:`3`\tklm:4", StandardCharsets.UTF_8);
        it.next();
    }

}
