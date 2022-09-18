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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.com.grieve.bcf.exception.EndOfLineException;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class DefaultParsedLineTest {
  @Test
  void emptyLine_1() {
    DefaultParsedLine line = new DefaultParsedLine("");
    assertEquals(0, line.getWords().size());
    assertEquals("", line.getLine());
    assertTrue(line.isEol());
    assertThrows(EndOfLineException.class, line::next);
    assertEquals("", line.getCurrentWord());
  }

  @Test
  void emptyLine_2() {
    DefaultParsedLine line = new DefaultParsedLine(Collections.singletonList(""));
    assertEquals(0, line.getWords().size());
    assertEquals("", line.getLine());
    assertTrue(line.isEol());
    assertThrows(EndOfLineException.class, line::next);
    assertEquals("", line.getCurrentWord());
  }

  @Test
  void emptyLine_3() {
    DefaultParsedLine line = new DefaultParsedLine(" ");
    assertEquals(0, line.getWords().size());
    assertEquals("", line.getLine());
    assertTrue(line.isEol());
    assertThrows(EndOfLineException.class, line::next);
    assertEquals("", line.getCurrentWord());
  }

  @Test
  void emptyLine_4() {
    DefaultParsedLine line = new DefaultParsedLine(Collections.singletonList(" "));
    assertEquals(0, line.getWords().size());
    assertEquals("", line.getLine());
    assertTrue(line.isEol());
    assertThrows(EndOfLineException.class, line::next);
    assertEquals("", line.getCurrentWord());
  }

  @Test
  void trailing_1() throws EndOfLineException {
    DefaultParsedLine line = new DefaultParsedLine("word ");
    assertEquals(2, line.getWords().size());
    assertEquals("word ", line.getLine());
    assertFalse(line.isEol());
    assertEquals("word", line.next());
    assertEquals("", line.next());
  }

  @Test
  void trailing_2() throws EndOfLineException {
    DefaultParsedLine line = new DefaultParsedLine("  word   ");
    assertEquals(2, line.getWords().size());
    assertEquals("word ", line.getLine());
    assertFalse(line.isEol());
    assertEquals("word", line.next());
    assertEquals("", line.next());
  }

  @Test
  void singleWord_1() throws EndOfLineException {
    DefaultParsedLine line = new DefaultParsedLine("word");
    assertEquals(1, line.getWords().size());
    assertEquals("word", line.getLine());
    assertFalse(line.isEol());
    assertEquals("word", line.getCurrentWord());
    assertEquals("word", line.next());
  }

  @Test
  void singleWord_2() throws EndOfLineException {
    DefaultParsedLine line = new DefaultParsedLine(Collections.singletonList("word"));
    assertEquals(1, line.getWords().size());
    assertEquals("word", line.getLine());
    assertFalse(line.isEol());
    assertEquals("word", line.getCurrentWord());
    assertEquals("word", line.next());
  }

  @Test
  void multiWord_1() throws EndOfLineException {
    DefaultParsedLine line = new DefaultParsedLine("word1 word2   word3");
    assertEquals(3, line.getWords().size());
    assertEquals("word1 word2 word3", line.getLine());
    assertFalse(line.isEol());
    assertEquals("word1", line.next());
    assertEquals("word2", line.next());
    assertEquals("word3", line.getCurrentWord());
    assertEquals("word3", line.next());
  }

  @Test
  void multiWord_2() throws EndOfLineException {
    DefaultParsedLine line =
        new DefaultParsedLine(Arrays.asList("word1", "word2", "", " ", "word3"));
    assertEquals(3, line.getWords().size());
    assertEquals("word1 word2 word3", line.getLine());
    assertFalse(line.isEol());
    assertEquals("word1", line.next());
    assertEquals("word2", line.next());
    assertEquals("word3", line.getCurrentWord());
    assertEquals("word3", line.next());
  }

  @Test
  void insert_1() throws EndOfLineException {
    DefaultParsedLine line = new DefaultParsedLine("word1 word2 word3");
    assertEquals("word1", line.next());

    line.insert("word5 word6");
    assertEquals("word5", line.next());
    assertEquals("word6", line.next());
    assertEquals("word2", line.next());
    assertEquals("word3", line.next());
    assertThrows(EndOfLineException.class, line::next);
  }

  @Test
  void insert_2() throws EndOfLineException {
    DefaultParsedLine line = new DefaultParsedLine("word1 word2 word3");
    assertEquals("word1", line.next());

    line.insert("");
    assertEquals("word2", line.next());
    assertEquals("word3", line.next());
    assertThrows(EndOfLineException.class, line::next);
  }

  @Test
  void insert_3() throws EndOfLineException {
    DefaultParsedLine line = new DefaultParsedLine("word1 word2 word3");
    assertEquals("word1", line.next());

    line.insert("   ");
    assertEquals("word2", line.next());
    assertEquals("word3", line.next());
    assertThrows(EndOfLineException.class, line::next);
  }

  @Test
  void insert_4() throws EndOfLineException {
    DefaultParsedLine line = new DefaultParsedLine("word1 word2 word3");
    assertEquals("word1", line.next());

    line.insert(" word5    word6  ");
    assertEquals("word5", line.next());
    assertEquals("word6", line.next());
    assertEquals("word2", line.next());
    assertEquals("word3", line.next());
    assertThrows(EndOfLineException.class, line::next);
  }
}
