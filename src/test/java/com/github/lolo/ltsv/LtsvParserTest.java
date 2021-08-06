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

public class LtsvParserTest {

    @Test
    public void testMultipleLineStrictDefaults() {
        LtsvParser parser = LtsvParser.builder().strict().build();
        Iterator<Map<String, String>> it = parser.parse("abc:1\tdef:2\nhij:3\tklm:4", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("abc", "1"));
        assertThat(data, hasEntry("def", "2"));
        assertTrue("Iterator must be non-empty", it.hasNext());
        data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("hij", "3"));
        assertThat(data, hasEntry("klm", "4"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testMultipleLineStrictLineEnding() {
        LtsvParser parser = LtsvParser.builder().strict().withLineEnding('\r').build();
        Iterator<Map<String, String>> it = parser.parse("abc:1\tdef:2\n\rhij:3\tklm:4", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("abc", "1"));
        assertThat(data, hasEntry("def", "2\n"));
        assertTrue("Iterator must be non-empty", it.hasNext());
        data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("hij", "3"));
        assertThat(data, hasEntry("klm", "4"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testMultipleLineLenientDefaults() {
        LtsvParser parser = LtsvParser.builder().lenient().build();
        Iterator<Map<String, String>> it = parser.parse("abc:1\tdef\nhij:3\tklm", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("abc", "1"));
        assertThat(data, hasEntry("def", null));
        assertTrue("Iterator must be non-empty", it.hasNext());
        data = it.next();
        assertEquals("Result contains two entries", 2, data.size());
        assertThat(data, hasEntry("hij", "3"));
        assertThat(data, hasEntry("klm", null));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test
    public void testMultipleEmptyLineDefaults() {
        LtsvParser parser = LtsvParser.builder().strict().build();
        Iterator<Map<String, String>> it = parser.parse("\n\n\n\n", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        for (int i = 0; i < 4; i++) {
            assertTrue(it.hasNext());
            Map<String, String> data = it.next();
            assertThat("Empty maps returned for empty lines", 0, is(data.size()));
        }
        assertFalse("Iterator does not have any items left", it.hasNext());
    }

    @Test(expected = ParseLtsvException.class)
    public void testStreamExceptionHasNext() throws Exception {
        LtsvParser parser = LtsvParser.builder().strict().build();
        InputStream in = Mockito.mock(InputStream.class);
        Mockito.when(in.available()).thenThrow(IOException.class);
        Iterator<Map<String, String>> it = parser.parse(in);
        it.hasNext();
    }

    @Test(expected = ParseLtsvException.class)
    public void testStreamExceptionNext() throws Exception {
        LtsvParser parser = LtsvParser.builder().strict().build();
        InputStream in = Mockito.mock(InputStream.class);
        Mockito.when(in.available()).thenReturn(100);
        Mockito.when(in.read()).thenThrow(IOException.class);
        Iterator<Map<String, String>> it = parser.parse(in);
        it.next();
    }

}
