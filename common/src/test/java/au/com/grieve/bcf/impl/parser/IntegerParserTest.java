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

class IntegerParserTest {

  @Test
  void parseSimple_1() {
    IntegerParser integerParser = new IntegerParser(new HashMap<>());
    ParsedLine line = new DefaultParsedLine("");

    assertThrows(EndOfLineException.class, () -> integerParser.parse(null, line));
  }

  @Test
  void parseSimple_2() {
    IntegerParser integerParser = new IntegerParser(new HashMap<>());
    ParsedLine line = new DefaultParsedLine("a");

    assertThrows(ParserSyntaxException.class, () -> integerParser.parse(null, line));
  }

  @Test
  void parseSimple_3() throws EndOfLineException, ParserSyntaxException {
    IntegerParser integerParser = new IntegerParser(new HashMap<>());
    ParsedLine line = new DefaultParsedLine("1");

    assertEquals(1, integerParser.parse(null, line));
  }

  @Test
  void parseSimple_4() throws EndOfLineException, ParserSyntaxException {
    IntegerParser integerParser = new IntegerParser(new HashMap<>());
    ParsedLine line = new DefaultParsedLine("1 a");

    assertEquals(1, integerParser.parse(null, line));
  }

  @Test
  void parseSimple_5() {
    IntegerParser integerParser = new IntegerParser(new HashMap<>());
    ParsedLine line = new DefaultParsedLine("a 1");

    assertThrows(ParserSyntaxException.class, () -> integerParser.parse(null, line));
  }

  @Test
  void optionsMin_1() {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("min", "13");
    IntegerParser integerParser = new IntegerParser(parameters);
    ParsedLine line = new DefaultParsedLine("5");

    assertThrows(ParserSyntaxException.class, () -> integerParser.parse(null, line));
  }

  @Test
  void optionsMin_2() throws EndOfLineException, ParserSyntaxException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("min", "13");
    IntegerParser integerParser = new IntegerParser(parameters);
    ParsedLine line = new DefaultParsedLine("100");

    assertEquals(100, integerParser.parse(null, line));
  }

  @Test
  void optionsMin_3() throws EndOfLineException, ParserSyntaxException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("min", "13");
    IntegerParser integerParser = new IntegerParser(parameters);
    ParsedLine line = new DefaultParsedLine("13");

    assertEquals(13, integerParser.parse(null, line));
  }

  @Test
  void optionsMax_1() {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("max", "13");
    IntegerParser integerParser = new IntegerParser(parameters);
    ParsedLine line = new DefaultParsedLine("100");

    assertThrows(ParserSyntaxException.class, () -> integerParser.parse(null, line));
  }

  @Test
  void optionsMax_2() throws EndOfLineException, ParserSyntaxException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("max", "13");
    IntegerParser integerParser = new IntegerParser(parameters);
    ParsedLine line = new DefaultParsedLine("5");

    assertEquals(5, integerParser.parse(null, line));
  }

  @Test
  void optionsMax_3() throws EndOfLineException, ParserSyntaxException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("max", "13");
    IntegerParser integerParser = new IntegerParser(parameters);
    ParsedLine line = new DefaultParsedLine("13");

    assertEquals(13, integerParser.parse(null, line));
  }

  @Test
  void completionMinMax_1() {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("min", "5");
    parameters.put("max", "13");
    ParsedLine line = new DefaultParsedLine("");
    List<CompletionCandidateGroup> groups = new ArrayList<>();
    IntegerParser integerParser = new IntegerParser(parameters);

    assertThrows(EndOfLineException.class, () -> integerParser.complete(null, line, groups));
    assertEquals(0, groups.size());
    //    assertEquals(9, groups.get(0).getMatchingCompletionCandidates().size());
  }

  @Test
  void completionMinMax_2() throws EndOfLineException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("min", "5");
    parameters.put("max", "13");
    ParsedLine line = new DefaultParsedLine("1");
    List<CompletionCandidateGroup> groups = new ArrayList<>();
    IntegerParser integerParser = new IntegerParser(parameters);

    integerParser.complete(null, line, groups);
    assertEquals(1, groups.size());
    assertEquals(4, groups.get(0).getMatchingCompletionCandidates().size());
  }

  @Test
  void completionMinMax_3() throws EndOfLineException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("min", "13");
    parameters.put("max", "5");
    ParsedLine line = new DefaultParsedLine("1");
    List<CompletionCandidateGroup> groups = new ArrayList<>();
    IntegerParser integerParser = new IntegerParser(parameters);

    integerParser.complete(null, line, groups);
    assertEquals(0, groups.size());
  }

}
