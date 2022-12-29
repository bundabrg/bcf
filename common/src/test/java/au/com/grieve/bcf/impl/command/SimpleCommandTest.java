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

package au.com.grieve.bcf.impl.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.com.grieve.bcf.CompletionCandidateGroup;
import au.com.grieve.bcf.ParsedLine;
import au.com.grieve.bcf.Parser;
import au.com.grieve.bcf.ParserTreeResult;
import au.com.grieve.bcf.StringParserClassRegister;
import au.com.grieve.bcf.annotation.Arg;
import au.com.grieve.bcf.impl.error.UnexpectedInputError;
import au.com.grieve.bcf.impl.parser.IntegerParser;
import au.com.grieve.bcf.impl.parser.StringParser;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

class SimpleCommandTest {

  StringParserClassRegister<Void> register =
      (name, parameters) -> {
        Map<String, Class<? extends Parser<?, ?>>> parserClassMap = new HashMap<>();
        parserClassMap.put("literal", StringParser.class);
        parserClassMap.put("string", StringParser.class);
        parserClassMap.put("int", IntegerParser.class);

        Class<? extends Parser<?, ?>> parserClass = parserClassMap.get(name);
        if (parserClass == null) {
          throw new RuntimeException("Unknown parser: " + name);
        }

        try {
          //noinspection unchecked
          return (Parser<Void, ?>) parserClass.getConstructor(Map.class).newInstance(parameters);
        } catch (InstantiationException
            | NoSuchMethodException
            | InvocationTargetException
            | IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      };

  @Test
  void test_1() {
    TestSimpleClass c1 = new TestSimpleClass("cmd1");
    ParserTreeResult<Void> result = c1.buildCommand().getRoot().parse("c_arg1 c_arg2 c_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertEquals(0, result.getErrors().size());
    assertEquals(0, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(1, c1.execute_count);
    assertEquals(1, c1.complete_count);
  }

  @Test
  void child_1() {
    TestParentAnnotationCommand parent = new TestParentAnnotationCommand();
    TestSimpleClass c1 = new TestSimpleClass("cmd1");
    parent.then(c1.buildCommand().getRoot());

    ParserTreeResult<Void> result =
        parent.buildCommand(register).getRoot().parse("c_arg1 c_arg2 c_arg3 bob sue", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof UnexpectedInputError));
    assertEquals(0, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(1, c1.execute_count);
    assertEquals(1, c1.complete_count);
  }

  static class TestSimpleClass extends SimpleCommand<Void> {
    int execute_count = 0;
    int complete_count = 0;

    public TestSimpleClass(String name) {
      super(name);
    }

    @Override
    public void execute(Void unused, @NotNull ParsedLine line) {
      execute_count++;
    }

    @Override
    public void complete(
        Void unused, @NotNull ParsedLine line, List<CompletionCandidateGroup> completions) {
      complete_count++;
    }
  }

  @Arg("c_arg1 c_arg2 c_arg3")
  static class TestParentAnnotationCommand extends AnnotationCommand<Void> {}
}
