package com.github.lolo.ltsv;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class LtsvParserTest {

    @Test
    public void testSingleLineDefaults() {
        LtsvParser parser = new LtsvParser();
        Iterator<Map<String, String>> it = parser.parse("abc:1\tdef:2", StandardCharsets.UTF_8);
        assertTrue("Iterator must be non-empty", it.hasNext());
        Map<String, String> data = it.next();
        assertEquals("Result contains two entries", data.size(), 2);
        assertThat(data, hasEntry("abc", "1"));
        assertThat(data, hasEntry("def", "2"));
        assertFalse("Iterator does not have any items left", it.hasNext());
    }
}
