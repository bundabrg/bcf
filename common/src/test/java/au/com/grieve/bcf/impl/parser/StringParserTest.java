/*
 * Copyright (c) 2020-2022 Brendan Grieve (bundabrg) - MIT License
 *
 *  Permission is hereby granted, free of charge, to any person obtaining
 *  a copy of this software and associated documentation files (the
 *  "Software"), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish,
 *  distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to
 *  the following conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 *  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package au.com.grieve.bcf.impl.parser;

import au.com.grieve.bcf.ParsedLine;
import au.com.grieve.bcf.exception.EndOfLineException;
import au.com.grieve.bcf.impl.line.DefaultParsedLine;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StringParserTest {

    @Test
    void complete() {
    }

    @Test
    void parseSimple_1() {
        StringParser stringParser = new StringParser(new HashMap<>());
        ParsedLine line = new DefaultParsedLine("");

        assertThrows(EndOfLineException.class, () -> stringParser.parse(line));
        assertEquals(0, line.getWordIndex());
    }

    @Test
    void parseSimple_2() throws EndOfLineException {
        StringParser stringParser = new StringParser(new HashMap<>());
        ParsedLine line = new DefaultParsedLine("1");

        assertEquals("1", stringParser.parse(line));
        assertEquals(1, line.getWordIndex());
    }

    @Test
    void parseSimple_3() throws EndOfLineException {
        StringParser stringParser = new StringParser(new HashMap<>());
        ParsedLine line = new DefaultParsedLine("1 a");

        assertEquals("1", stringParser.parse(line));
        assertEquals(1, line.getWordIndex());
    }

    @Test
    void parseSimple_4() throws EndOfLineException {
        StringParser stringParser = new StringParser(new HashMap<>());
        ParsedLine line = new DefaultParsedLine("a 1");

        assertEquals("a", stringParser.parse(line));
        assertEquals(1, line.getWordIndex());
    }

    @Test
    void singleOption_1() throws EndOfLineException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("options", "option1");
        StringParser sp1 = new StringParser(parameters);
        ParsedLine line = new DefaultParsedLine("option1");

        assertEquals("option1", sp1.parse(line));
        assertEquals(1, line.getWordIndex());
    }

    @Test
    void singleOption_2() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("options", "option1");
        StringParser sp1 = new StringParser(parameters);
        ParsedLine line = new DefaultParsedLine("bob");

        assertThrows(IllegalArgumentException.class, () -> sp1.parse(line));
        assertEquals(0, line.getWordIndex());
    }

    @Test
    void multipleOptions_1() throws EndOfLineException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("options", "option1|option2|option3");
        StringParser sp2 = new StringParser(parameters);
        ParsedLine line = new DefaultParsedLine("option1 option2 option3 bob");

        assertEquals("option1", sp2.parse(line));
        assertEquals(1, line.getWordIndex());
        assertEquals("option2", sp2.parse(line));
        assertEquals(2, line.getWordIndex());
        assertEquals("option3", sp2.parse(line));
        assertEquals(3, line.getWordIndex());
        assertThrows(IllegalArgumentException.class, () -> sp2.parse(line));
        assertEquals(3, line.getWordIndex());
    }
}