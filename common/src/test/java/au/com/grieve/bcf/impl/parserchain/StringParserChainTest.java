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

package au.com.grieve.bcf.impl.parserchain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.com.grieve.bcf.CompletionCandidateGroup;
import au.com.grieve.bcf.CompletionContext;
import au.com.grieve.bcf.ExecutionContext;
import au.com.grieve.bcf.ParsedLine;
import au.com.grieve.bcf.Result;
import au.com.grieve.bcf.exception.EndOfLineException;
import au.com.grieve.bcf.impl.framework.base.BaseCompletionContext;
import au.com.grieve.bcf.impl.framework.base.BaseExecutionContext;
import au.com.grieve.bcf.impl.line.DefaultParsedLine;
import au.com.grieve.bcf.impl.parser.IntegerParser;
import au.com.grieve.bcf.impl.parser.StringParser;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class StringParserChainTest {

  ExecutionContext<Object> defaultExecutionContext() {
    ExecutionContext<Object> result = new BaseExecutionContext<>();
    result.getParserClasses().put("literal", StringParser.class);
    result.getParserClasses().put("string", StringParser.class);
    result.getParserClasses().put("int", IntegerParser.class);
    return result;
  }

  CompletionContext<Object> defaultCompletionContext() {
    CompletionContext<Object> result = new BaseCompletionContext<>();
    result.getParserClasses().put("literal", StringParser.class);
    result.getParserClasses().put("string", StringParser.class);
    result.getParserClasses().put("int", IntegerParser.class);
    return result;
  }

  @Test
  void oneParser_1() {
    StringParserChain<Object> a = new StringParserChain<>("");
    assertEquals(0, a.getParserConfigs().size());
  }

  @Test
  void oneParser_2() {
    StringParserChain<Object> a = new StringParserChain<>("literal");
    assertEquals(1, a.getParserConfigs().size());
    assertEquals("literal", a.getParserConfigs().get(0).getName());
  }

  @Test
  void oneParser_3() {
    StringParserChain<Object> a = new StringParserChain<>("@string");
    assertEquals(1, a.getParserConfigs().size());
    assertEquals("string", a.getParserConfigs().get(0).getName());
  }

  @Test
  void twoParsers_1() {
    StringParserChain<Object> a = new StringParserChain<>("literal literal");
    assertEquals(2, a.getParserConfigs().size());
    assertEquals("literal", a.getParserConfigs().get(0).getName());
    assertEquals("literal", a.getParserConfigs().get(1).getName());
  }

  @Test
  void twoParsers_2() {
    StringParserChain<Object> a = new StringParserChain<>("@string @string");
    assertEquals(2, a.getParserConfigs().size());
    assertEquals("string", a.getParserConfigs().get(0).getName());
    assertEquals("string", a.getParserConfigs().get(1).getName());
  }

  @Test
  void literalParameters_1() {
    StringParserChain<Object> a = new StringParserChain<>("literal");
    assertEquals(2, a.getParserConfigs().get(0).getParameters().size());
    assertEquals("literal", a.getParserConfigs().get(0).getParameters().get("options"));
    assertEquals("true", a.getParserConfigs().get(0).getParameters().get("suppress"));
  }

  @Test
  void literalParameters_2() {
    StringParserChain<Object> a = new StringParserChain<>("literal1|literal2|literal3");
    assertEquals(2, a.getParserConfigs().get(0).getParameters().size());
    assertEquals(
        "literal1|literal2|literal3", a.getParserConfigs().get(0).getParameters().get("options"));
    assertEquals("true", a.getParserConfigs().get(0).getParameters().get("suppress"));
  }

  @Test
  void multiParams_1() {
    StringParserChain<Object> a =
        new StringParserChain<>("literal(p1=one,p2='two',p3='one two',p4=one two,suppress=false)");
    assertEquals(6, a.getParserConfigs().get(0).getParameters().size());
    assertEquals("literal", a.getParserConfigs().get(0).getParameters().get("options"));
    assertEquals("one", a.getParserConfigs().get(0).getParameters().get("p1"));
    assertEquals("two", a.getParserConfigs().get(0).getParameters().get("p2"));
    assertEquals("one two", a.getParserConfigs().get(0).getParameters().get("p3"));
    assertEquals("one two", a.getParserConfigs().get(0).getParameters().get("p4"));
    assertEquals("false", a.getParserConfigs().get(0).getParameters().get("suppress"));
  }

  @Test
  void parseSuppress_1() throws EndOfLineException {
    StringParserChain<Object> a = new StringParserChain<>("@string");
    ParsedLine line = new DefaultParsedLine("bob");
    ExecutionContext<Object> context = defaultExecutionContext();
    List<Result> result = new ArrayList<>();

    a.parse(line, result, context);
    assertEquals(1, result.size());
    assertEquals("bob", result.get(0).getValue());
    assertFalse(result.get(0).isSuppressed());
  }

  @Test
  void parseSuppress_2() throws EndOfLineException {
    StringParserChain<Object> a = new StringParserChain<>("@string(suppress=true)");
    ExecutionContext<Object> context = defaultExecutionContext();
    ParsedLine line = new DefaultParsedLine("bob");
    List<Result> result = new ArrayList<>();

    a.parse(line, result, context);
    assertEquals(1, result.size());
    assertEquals("bob", result.get(0).getValue());
    assertTrue(result.get(0).isSuppressed());
  }

  @Test
  void parseDefaultRequired_1() throws EndOfLineException {
    StringParserChain<Object> a = new StringParserChain<>("@string(default=alice)");
    ExecutionContext<Object> context = defaultExecutionContext();
    ParsedLine line = new DefaultParsedLine("bob");
    List<Result> result = new ArrayList<>();

    a.parse(line, result, context);
    assertEquals(1, result.size());
    assertEquals("bob", result.get(0).getValue());
  }

  @Test
  void parseDefaultRequired_2() throws EndOfLineException {
    StringParserChain<Object> a = new StringParserChain<>("@string(default=alice)");
    ExecutionContext<Object> context = defaultExecutionContext();
    ParsedLine line = new DefaultParsedLine("");
    List<Result> result = new ArrayList<>();

    a.parse(line, result, context);
    assertEquals(1, result.size());
    assertEquals("alice", result.get(0).getValue());
  }

  @Test
  void parseDefaultRequired_3() throws EndOfLineException {
    StringParserChain<Object> a = new StringParserChain<>("@string(required=false)");
    ExecutionContext<Object> context = defaultExecutionContext();
    ParsedLine line = new DefaultParsedLine("bob");
    List<Result> result = new ArrayList<>();

    a.parse(line, result, context);
    assertEquals(1, result.size());
    assertEquals("bob", result.get(0).getValue());
  }

  @Test
  void parseDefaultRequired_4() throws EndOfLineException {
    StringParserChain<Object> a = new StringParserChain<>("@string(required=false)");
    ExecutionContext<Object> context = defaultExecutionContext();
    ParsedLine line = new DefaultParsedLine("");
    List<Result> result = new ArrayList<>();

    a.parse(line, result, context);
    assertEquals(1, result.size());
    assertNull(result.get(0).getValue());
  }

  @Test
  void parseDefaultRequired_5() throws EndOfLineException {
    StringParserChain<Object> a = new StringParserChain<>("@int(default=5)");
    ExecutionContext<Object> context = defaultExecutionContext();
    ParsedLine line = new DefaultParsedLine("");
    List<Result> result = new ArrayList<>();

    a.parse(line, result, context);
    assertEquals(1, result.size());
    assertEquals(5, result.get(0).getValue());
  }

  @Test
  void complete_1() {
    StringParserChain<Object> a = new StringParserChain<>("literal1");
    CompletionContext<Object> context = defaultCompletionContext();
    ParsedLine line = new DefaultParsedLine("");
    List<CompletionCandidateGroup> result = new ArrayList<>();

    assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
  }

  @Test
  void complete_2() {
    StringParserChain<Object> a = new StringParserChain<>("literal1");
    CompletionContext<Object> context = defaultCompletionContext();
    ParsedLine line = new DefaultParsedLine(" ");
    List<CompletionCandidateGroup> result = new ArrayList<>();

    assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
  }

  @Test
  void complete_3() {
    StringParserChain<Object> a = new StringParserChain<>("literal1");
    CompletionContext<Object> context = defaultCompletionContext();
    ParsedLine line = new DefaultParsedLine("b");
    List<CompletionCandidateGroup> result = new ArrayList<>();

    assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
    assertEquals(
        0, result.stream().filter(g -> g.getMatchingCompletionCandidates().size() > 0).count());
  }

  @Test
  void complete_4() {
    StringParserChain<Object> a = new StringParserChain<>("literal1");
    CompletionContext<Object> context = defaultCompletionContext();
    ParsedLine line = new DefaultParsedLine("l");
    List<CompletionCandidateGroup> result = new ArrayList<>();

    assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
    assertEquals(
        1, result.stream().filter(g -> g.getMatchingCompletionCandidates().size() > 0).count());
    assertEquals(1, result.get(0).getMatchingCompletionCandidates().size());
  }

  @Test
  void complete_5() {
    StringParserChain<Object> a = new StringParserChain<>("literal1");
    CompletionContext<Object> context = defaultCompletionContext();
    ParsedLine line = new DefaultParsedLine("literal1");
    List<CompletionCandidateGroup> result = new ArrayList<>();

    assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
    assertEquals(
        1, result.stream().filter(g -> g.getMatchingCompletionCandidates().size() > 0).count());
    assertEquals(1, result.get(0).getMatchingCompletionCandidates().size());
  }

  @Test
  void complete_6() {
    StringParserChain<Object> a = new StringParserChain<>("literal1|literal2|literal3");
    CompletionContext<Object> context = defaultCompletionContext();
    ParsedLine line = new DefaultParsedLine("literal");
    List<CompletionCandidateGroup> result = new ArrayList<>();

    assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
    assertEquals(
        1, result.stream().filter(g -> g.getMatchingCompletionCandidates().size() > 0).count());
    assertEquals(3, result.get(0).getMatchingCompletionCandidates().size());
  }

  @Test
  void complete_7() {
    StringParserChain<Object> a = new StringParserChain<>("literal1|literal2|literal3");
    CompletionContext<Object> context = defaultCompletionContext();
    ParsedLine line = new DefaultParsedLine("literal1");
    List<CompletionCandidateGroup> result = new ArrayList<>();

    assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
    assertEquals(
        1, result.stream().filter(g -> g.getMatchingCompletionCandidates().size() > 0).count());
    assertEquals(1, result.get(0).getMatchingCompletionCandidates().size());
  }

  @Test
  void complete_8() {
    StringParserChain<Object> a =
        new StringParserChain<>("literal1|literal2|literal3 mike|milly|mark");
    CompletionContext<Object> context = defaultCompletionContext();
    ParsedLine line = new DefaultParsedLine("l");
    List<CompletionCandidateGroup> result = new ArrayList<>();

    assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
    assertEquals(
        1, result.stream().filter(g -> g.getMatchingCompletionCandidates().size() > 0).count());
    assertEquals(3, result.get(0).getMatchingCompletionCandidates().size());
  }

  @Test
  void complete_9() {
    StringParserChain<Object> a =
        new StringParserChain<>("literal1|literal2|literal mike|milly|mark");
    CompletionContext<Object> context = defaultCompletionContext();
    ParsedLine line = new DefaultParsedLine("literal");
    List<CompletionCandidateGroup> result = new ArrayList<>();

    assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
    assertEquals(
        1, result.stream().filter(g -> g.getMatchingCompletionCandidates().size() > 0).count());
    assertEquals(3, result.get(0).getMatchingCompletionCandidates().size());
  }

  @Test
  void complete_10() {
    StringParserChain<Object> a =
        new StringParserChain<>("literal1|literal2|literal mike|milly|mark");
    CompletionContext<Object> context = defaultCompletionContext();
    ParsedLine line = new DefaultParsedLine("literal1");
    List<CompletionCandidateGroup> result = new ArrayList<>();

    assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
    assertEquals(
        1, result.stream().filter(g -> g.getMatchingCompletionCandidates().size() > 0).count());
    assertEquals(1, result.get(0).getMatchingCompletionCandidates().size());
  }

  @Test
  void complete_11() {
    StringParserChain<Object> a =
        new StringParserChain<>("literal1|literal2|literal mike|milly|mark");
    CompletionContext<Object> context = defaultCompletionContext();
    ParsedLine line = new DefaultParsedLine("literal ");
    List<CompletionCandidateGroup> result = new ArrayList<>();

    assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
    assertEquals(
        1, result.stream().filter(g -> g.getMatchingCompletionCandidates().size() > 0).count());
    assertEquals(3, result.get(0).getMatchingCompletionCandidates().size());
  }

  @Test
  void complete_12() {
    StringParserChain<Object> a =
        new StringParserChain<>("literal1|literal2|literal mike|milly|mark");
    CompletionContext<Object> context = defaultCompletionContext();
    ParsedLine line = new DefaultParsedLine("literal ma");
    List<CompletionCandidateGroup> result = new ArrayList<>();

    assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
    assertEquals(
        1, result.stream().filter(g -> g.getMatchingCompletionCandidates().size() > 0).count());
    assertEquals(1, result.get(0).getMatchingCompletionCandidates().size());
  }

  @Test
  void complete_13() {
    StringParserChain<Object> a =
        new StringParserChain<>("literal1|literal2|literal mike|milly|mark");
    CompletionContext<Object> context = defaultCompletionContext();
    ParsedLine line = new DefaultParsedLine("literal mark");
    List<CompletionCandidateGroup> result = new ArrayList<>();

    assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
    assertEquals(
        1, result.stream().filter(g -> g.getMatchingCompletionCandidates().size() > 0).count());
    assertEquals(1, result.get(0).getMatchingCompletionCandidates().size());
  }

  @Test
  void complete_14() {
    StringParserChain<Object> a =
        new StringParserChain<>("literal1|literal2|literal mike|milly|mark");
    CompletionContext<Object> context = defaultCompletionContext();
    ParsedLine line = new DefaultParsedLine("literal markb");
    List<CompletionCandidateGroup> result = new ArrayList<>();

    assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
    assertEquals(
        0, result.stream().filter(g -> g.getMatchingCompletionCandidates().size() > 0).count());
  }

  @Test
  void complete_15() throws EndOfLineException {
    StringParserChain<Object> a =
        new StringParserChain<>("literal1|literal2|literal mike|milly|mark");
    CompletionContext<Object> context = defaultCompletionContext();
    ParsedLine line = new DefaultParsedLine("literal mark b");
    List<CompletionCandidateGroup> result = new ArrayList<>();

    a.complete(line, result, context);
    assertEquals(
        0, result.stream().filter(g -> g.getMatchingCompletionCandidates().size() > 0).count());
  }

  @Test
  void complete_16() {
    StringParserChain<Object> a =
        new StringParserChain<>("literal1|literal2|literal mike|milly|mark");
    CompletionContext<Object> context = defaultCompletionContext();
    ParsedLine line = new DefaultParsedLine("l m");
    List<CompletionCandidateGroup> result = new ArrayList<>();

    assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
    assertEquals(
        0, result.stream().filter(g -> g.getMatchingCompletionCandidates().size() > 0).count());
  }

  @Test
  void parseSwitch_1() {
    StringParserChain<Object> a = new StringParserChain<>("@string(switch=sw1) @string @string");
    ExecutionContext<Object> context = defaultExecutionContext();
    List<Result> result = new ArrayList<>();
    ParsedLine line = new DefaultParsedLine("alice");

    assertThrows(EndOfLineException.class, () -> a.parse(line, result, context));
    assertEquals(2, result.size());
  }

  @Test
  void parseSwitch_2() {
    StringParserChain<Object> a = new StringParserChain<>("@string(switch=sw1) @string @string");
    ExecutionContext<Object> context = defaultExecutionContext();
    List<Result> result = new ArrayList<>();
    ParsedLine line = new DefaultParsedLine("-sw1 bob");

    assertThrows(EndOfLineException.class, () -> a.parse(line, result, context));
    assertEquals(1, result.size());
    assertEquals("bob", result.get(0).getValue());
  }

  @Test
  void parseSwitch_3() throws EndOfLineException {
    StringParserChain<Object> a = new StringParserChain<>("@string(switch=sw1) @string @string");
    ExecutionContext<Object> context = defaultExecutionContext();
    List<Result> result = new ArrayList<>();
    ParsedLine line = new DefaultParsedLine("alice amy -sw1 bob");

    a.parse(line, result, context);
    assertEquals(3, result.size());
    assertEquals("bob", result.get(0).getValue());
    assertEquals("alice", result.get(1).getValue());
    assertEquals("amy", result.get(2).getValue());
  }

  @Test
  void parseSwitch_4() throws EndOfLineException {
    StringParserChain<Object> a = new StringParserChain<>("@string(switch=sw1) @string @string");
    ExecutionContext<Object> context = defaultExecutionContext();
    List<Result> result = new ArrayList<>();
    ParsedLine line = new DefaultParsedLine("alice -sw1 bob amy");

    a.parse(line, result, context);
    assertEquals(3, result.size());
    assertEquals("bob", result.get(0).getValue());
    assertEquals("alice", result.get(1).getValue());
    assertEquals("amy", result.get(2).getValue());
  }

  @Test
  void parseSwitch_5() throws EndOfLineException {
    StringParserChain<Object> a = new StringParserChain<>("@string(switch=sw1) @string @string");
    ExecutionContext<Object> context = defaultExecutionContext();
    List<Result> result = new ArrayList<>();
    ParsedLine line = new DefaultParsedLine("-sw1 bob alice amy");

    a.parse(line, result, context);
    assertEquals(3, result.size());
    assertEquals("bob", result.get(0).getValue());
    assertEquals("alice", result.get(1).getValue());
    assertEquals("amy", result.get(2).getValue());
  }

  @Test
  void parseSwitch_6() throws EndOfLineException {
    StringParserChain<Object> a = new StringParserChain<>("@string @string(switch=sw1) @string");
    ExecutionContext<Object> context = defaultExecutionContext();
    List<Result> result = new ArrayList<>();
    ParsedLine line = new DefaultParsedLine("-sw1 bob alice amy");

    // The switch is invalid, so it'll end up in the other parameters
    a.parse(line, result, context);
    assertEquals(3, result.size());
    assertEquals("-sw1", result.get(0).getValue());
    assertThrows(IllegalArgumentException.class, () -> result.get(1).getValue());
    assertEquals("bob", result.get(2).getValue());
  }

  @Test
  void parseSwitch_7() throws EndOfLineException {
    StringParserChain<Object> a = new StringParserChain<>("@string @string(switch=sw1) @string");
    ExecutionContext<Object> context = defaultExecutionContext();
    List<Result> result = new ArrayList<>();
    ParsedLine line = new DefaultParsedLine("alice -sw1 bob amy");

    a.parse(line, result, context);
    assertEquals(3, result.size());
    assertEquals("alice", result.get(0).getValue());
    assertEquals("bob", result.get(1).getValue());
    assertEquals("amy", result.get(2).getValue());
  }

  @Test
  void parseSwitch_8() throws EndOfLineException {
    StringParserChain<Object> a = new StringParserChain<>("@string @string(switch=sw1) @string");
    ExecutionContext<Object> context = defaultExecutionContext();
    List<Result> result = new ArrayList<>();
    ParsedLine line = new DefaultParsedLine("alice amy -sw1 bob");

    a.parse(line, result, context);
    assertEquals(3, result.size());
    assertEquals("alice", result.get(0).getValue());
    assertEquals("bob", result.get(1).getValue());
    assertEquals("amy", result.get(2).getValue());
  }

  @Test
  void parseSwitch_9() throws EndOfLineException {
    StringParserChain<Object> a = new StringParserChain<>("@string @string @string(switch=sw1)");
    ExecutionContext<Object> context = defaultExecutionContext();
    List<Result> result = new ArrayList<>();
    ParsedLine line = new DefaultParsedLine("-sw1 bob alice amy");

    // The switch is invalid, so it'll end up in the other parameters
    a.parse(line, result, context);
    assertEquals(3, result.size());
    assertEquals("-sw1", result.get(0).getValue());
    assertEquals("bob", result.get(1).getValue());
    assertThrows(IllegalArgumentException.class, () -> result.get(2).getValue());
  }

  @Test
  void parseSwitch_10() throws EndOfLineException {
    StringParserChain<Object> a = new StringParserChain<>("@string @string @string(switch=sw1)");
    ExecutionContext<Object> context = defaultExecutionContext();
    List<Result> result = new ArrayList<>();
    ParsedLine line = new DefaultParsedLine("alice -sw1 bob amy");

    // The switch is invalid, so it'll end up in the other parameters
    a.parse(line, result, context);
    assertEquals(3, result.size());
    assertEquals("alice", result.get(0).getValue());
    assertEquals("-sw1", result.get(1).getValue());
    assertThrows(IllegalArgumentException.class, () -> result.get(2).getValue());
  }

  @Test
  void parseSwitch_11() throws EndOfLineException {
    StringParserChain<Object> a = new StringParserChain<>("@string @string @string(switch=sw1)");
    ExecutionContext<Object> context = defaultExecutionContext();
    List<Result> result = new ArrayList<>();
    ParsedLine line = new DefaultParsedLine("alice amy -sw1 bob");

    a.parse(line, result, context);
    assertEquals(3, result.size());
    assertEquals("alice", result.get(0).getValue());
    assertEquals("amy", result.get(1).getValue());
    assertEquals("bob", result.get(2).getValue());
  }

  @Test
  void parseSwitch_12() throws EndOfLineException {
    StringParserChain<Object> a = new StringParserChain<>("@string(switch=sw1) @string @string");
    ExecutionContext<Object> context = defaultExecutionContext();
    List<Result> result = new ArrayList<>();
    ParsedLine line = new DefaultParsedLine("alice amy bob");

    a.parse(line, result, context);
    assertThrows(IllegalArgumentException.class, () -> result.get(0).getValue());
  }

  @Test
  void parseSwitchDefault_1() throws EndOfLineException {
    StringParserChain<Object> a =
        new StringParserChain<>("@string(switch=sw1,default=zoe) @string @string");
    ExecutionContext<Object> context = defaultExecutionContext();
    List<Result> result = new ArrayList<>();
    ParsedLine line = new DefaultParsedLine("alice amy");

    a.parse(line, result, context);
    assertEquals(3, result.size());
    assertEquals("zoe", result.get(0).getValue());
    assertEquals("alice", result.get(1).getValue());
    assertEquals("amy", result.get(2).getValue());
  }

  @Test
  void parseSwitchDefault_2() throws EndOfLineException {
    StringParserChain<Object> a =
        new StringParserChain<>("@string(switch=sw1,default=zoe) @string @string");
    ExecutionContext<Object> context = defaultExecutionContext();
    List<Result> result = new ArrayList<>();
    ParsedLine line = new DefaultParsedLine("alice amy -sw1 bob");

    a.parse(line, result, context);
    assertEquals(3, result.size());
    assertEquals("bob", result.get(0).getValue());
    assertEquals("alice", result.get(1).getValue());
    assertEquals("amy", result.get(2).getValue());
  }
}
