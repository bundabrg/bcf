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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import au.com.grieve.bcf.CompletionCandidateGroup;
import au.com.grieve.bcf.ParsedLine;
import au.com.grieve.bcf.exception.EndOfLineException;
import au.com.grieve.bcf.exception.ParserSyntaxException;
import au.com.grieve.bcf.impl.line.DefaultParsedLine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class StringParserTest {

  @Test
  void parseSimple_1() {
    StringParser stringParser = new StringParser(new HashMap<>());
    ParsedLine line = new DefaultParsedLine("");

    assertThrows(EndOfLineException.class, () -> stringParser.parse(null, line));
  }

  @Test
  void complete() {}

  @Test
  void parseSimple_2() throws EndOfLineException, ParserSyntaxException {
    StringParser stringParser = new StringParser(new HashMap<>());
    ParsedLine line = new DefaultParsedLine("1");

    assertEquals("1", stringParser.parse(null, line));
  }

  @Test
  void parseSimple_3() throws EndOfLineException, ParserSyntaxException {
    StringParser stringParser = new StringParser(new HashMap<>());
    ParsedLine line = new DefaultParsedLine("1 a");

    assertEquals("1", stringParser.parse(null, line));
  }

  @Test
  void parseSimple_4() throws EndOfLineException, ParserSyntaxException {
    StringParser stringParser = new StringParser(new HashMap<>());
    ParsedLine line = new DefaultParsedLine("a 1");

    assertEquals("a", stringParser.parse(null, line));
  }

  @Test
  void singleOption_1() throws EndOfLineException, ParserSyntaxException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("options", "option1");
    StringParser sp1 = new StringParser(parameters);
    ParsedLine line = new DefaultParsedLine("option1");

    assertEquals("option1", sp1.parse(null, line));
  }

  @Test
  void singleOption_2() {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("options", "option1");
    StringParser sp1 = new StringParser(parameters);
    ParsedLine line = new DefaultParsedLine("bob");

    assertThrows(ParserSyntaxException.class, () -> sp1.parse(null, line));
  }

  @Test
  void multipleOptions_1() throws EndOfLineException, ParserSyntaxException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("options", "option1|option2|option3");
    StringParser sp2 = new StringParser(parameters);
    ParsedLine line = new DefaultParsedLine("option1 option2 option3 bob");

    assertEquals("option1", sp2.parse(null, line));
    assertEquals("option2", sp2.parse(null, line));
    assertEquals("option3", sp2.parse(null, line));
    assertThrows(ParserSyntaxException.class, () -> sp2.parse(null, line));
  }

  @Test
  void completionEmptyInput_1() {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("options", "option1|option2|option3");
    StringParser p = new StringParser(parameters);
    ParsedLine line = new DefaultParsedLine("");
    List<CompletionCandidateGroup> result = new ArrayList<>();

    assertThrows(EndOfLineException.class, () -> p.complete(null, line, result));
  }

  @Test
  void completionEmptyInput_2() {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("options", "option1|option2|option3");
    StringParser p = new StringParser(parameters);
    ParsedLine line = new DefaultParsedLine(" ");
    List<CompletionCandidateGroup> result = new ArrayList<>();

    assertThrows(EndOfLineException.class, () -> p.complete(null, line, result));
  }

  @Test
  void completionSingleInput_1() throws EndOfLineException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("options", "option1|option2|option3");
    StringParser p = new StringParser(parameters);
    ParsedLine line = new DefaultParsedLine("b");
    List<CompletionCandidateGroup> result = new ArrayList<>();

    p.complete(null, line, result);
    assertEquals(
        0, result.stream().filter(g -> g.getMatchingCompletionCandidates().size() > 0).count());
  }

  @Test
  void completionSingleInput_2() throws EndOfLineException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("options", "option1|option2|option3");
    StringParser p = new StringParser(parameters);
    ParsedLine line = new DefaultParsedLine("o");
    List<CompletionCandidateGroup> result = new ArrayList<>();

    p.complete(null, line, result);
    assertEquals(
        1, result.stream().filter(g -> g.getMatchingCompletionCandidates().size() > 0).count());
    assertEquals(3, result.get(0).getMatchingCompletionCandidates().size());
  }

  @Test
  void completionSingleInput_3() throws EndOfLineException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("options", "option1|option2|option3");
    StringParser p = new StringParser(parameters);
    ParsedLine line = new DefaultParsedLine("option2");
    List<CompletionCandidateGroup> result = new ArrayList<>();

    p.complete(null, line, result);
    assertEquals(
        1, result.stream().filter(g -> g.getMatchingCompletionCandidates().size() > 0).count());
    assertEquals(1, result.get(0).getMatchingCompletionCandidates().size());
  }

  @Test
  void completionSingleInput_4() throws EndOfLineException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("options", "option1|option2|option3");
    StringParser p = new StringParser(parameters);
    ParsedLine line = new DefaultParsedLine("arg1");
    List<CompletionCandidateGroup> result = new ArrayList<>();
    line.next();

    assertThrows(EndOfLineException.class, () -> p.complete(null, line, result));
  }

  @Test
  void completionSingleInput_5() throws EndOfLineException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("options", "option1|option2|option3");
    StringParser p = new StringParser(parameters);
    ParsedLine line = new DefaultParsedLine("arg1 ");
    List<CompletionCandidateGroup> result = new ArrayList<>();
    line.next();

    p.complete(null, line, result);
    assertEquals(
        1, result.stream().filter(g -> g.getMatchingCompletionCandidates().size() > 0).count());
    assertEquals(3, result.get(0).getMatchingCompletionCandidates().size());
  }

}
