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

package au.com.grieve.bcf.impl.line;

import au.com.grieve.bcf.exception.EndOfLineException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class DefaultParsedLineTest {
    @Test
    void simple() throws EndOfLineException {
        DefaultParsedLine line;

        line = new DefaultParsedLine("");
        assertEquals(0, line.getWords().size());
        assertEquals("", line.getLine());
        assertTrue(line.isEol());
        assertThrows(EndOfLineException.class, line::next);
        assertThrows(EndOfLineException.class, line::getCurrentWord);

        line = new DefaultParsedLine(Collections.singletonList(""));
        assertEquals(0, line.getWords().size());
        assertEquals("", line.getLine());
        assertTrue(line.isEol());
        assertThrows(EndOfLineException.class, line::next);
        assertThrows(EndOfLineException.class, line::getCurrentWord);

        line = new DefaultParsedLine("word");
        assertEquals(1, line.getWords().size());
        assertEquals("word", line.getLine());
        assertFalse(line.isEol());
        assertEquals("word", line.getCurrentWord());
        assertEquals("word", line.next());

        line = new DefaultParsedLine(Collections.singletonList("word"));
        assertEquals(1, line.getWords().size());
        assertEquals("word", line.getLine());
        assertFalse(line.isEol());
        assertEquals("word", line.getCurrentWord());
        assertEquals("word", line.next());


        line = new DefaultParsedLine("word1 word2   word3");
        assertEquals(3, line.getWords().size());
        assertEquals("word1 word2 word3", line.getLine());
        assertFalse(line.isEol());
        assertEquals("word1", line.next());
        assertEquals("word2", line.next());
        assertEquals("word3", line.getCurrentWord());
        assertEquals("word3", line.next());

        line = new DefaultParsedLine(Arrays.asList("word1", "word2", "", " ", "word3"));
        assertEquals(3, line.getWords().size());
        assertEquals("word1 word2 word3", line.getLine());
        assertFalse(line.isEol());
        assertEquals("word1", line.next());
        assertEquals("word2", line.next());
        assertEquals("word3", line.getCurrentWord());
        assertEquals("word3", line.next());
    }

}