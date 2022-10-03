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

import au.com.grieve.bcf.CommandError;
import au.com.grieve.bcf.ErrorContext;
import au.com.grieve.bcf.Parser;
import au.com.grieve.bcf.ParserTreeResult;
import au.com.grieve.bcf.StringParserClassRegister;
import au.com.grieve.bcf.annotation.Arg;
import au.com.grieve.bcf.annotation.Default;
import au.com.grieve.bcf.annotation.Error;
import au.com.grieve.bcf.impl.error.InputExpectedError;
import au.com.grieve.bcf.impl.error.InvalidOptionError;
import au.com.grieve.bcf.impl.error.UnexpectedInputError;
import au.com.grieve.bcf.impl.parser.IntegerParser;
import au.com.grieve.bcf.impl.parser.StringParser;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class AnnotationCommandTest {
  //  Consumer<List<ParserTree<Void>>> debugWalker =
  //      n -> {
  //        System.err.printf("%" + n.size() + "s", "");
  //        System.err.println(n.get(n.size() - 1));
  //      };

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
  void noDefaultDefined() {
    C1 c1 = new C1();
    ParserTreeResult<Void> result = c1.buildCommand(register).getRoot().parse("bob", null);

    assertNotNull(result);
    assertNull(result.getExecuteCandidate());
    assertTrue(
        result.getErrors().stream()
            .map(CommandError::getClass)
            .anyMatch(e -> e == InvalidOptionError.class));
  }

  @Test
  void noArgMatchOnClass_1() {
    C2 c2 = new C2();
    ParserTreeResult<Void> result = c2.buildCommand(register).getRoot().parse("", null);

    assertNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InputExpectedError));
    assertEquals(0, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());
  }

  @Test
  void noArgMatchOnClass_2() {
    C2 c2 = new C2();
    ParserTreeResult<Void> result = c2.buildCommand(register).getRoot().parse("bob", null);

    assertNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());
  }

  @Test
  void someArgMatchOnClass_1() {
    C2 c2 = new C2();
    ParserTreeResult<Void> result =
        c2.buildCommand(register).getRoot().parse("c_arg1 c_arg2", null);

    assertNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InputExpectedError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());
  }

  @Test
  void someArgMatchOnClass_2() {
    C2 c2 = new C2();
    ParserTreeResult<Void> result =
        c2.buildCommand(register).getRoot().parse("c_arg1 c_arg2 ", null);

    assertNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());
  }

  @Test
  void someArgMatchOnClass_3() {
    C2 c2 = new C2();
    ParserTreeResult<Void> result =
        c2.buildCommand(register).getRoot().parse("c_arg1 c_arg2 bob", null);

    assertNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());
  }

  @Test
  void argMatchClassNotMethod_1() {
    C2 c2 = new C2();
    ParserTreeResult<Void> result =
        c2.buildCommand(register).getRoot().parse("c_arg1 c_arg2 c_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InputExpectedError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(1, c2.d_count);
    assertEquals(0, c2.e_count);
    assertEquals(0, c2.m1_count);
    assertEquals(0, c2.m2_count);
  }

  @Test
  void argMatchClassNotMethod_2() {
    C2 c2 = new C2();
    ParserTreeResult<Void> result =
        c2.buildCommand(register).getRoot().parse("c_arg1 c_arg2 c_arg3 ", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(2, result.getCompletions().size());
    assertEquals(
        2,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c2.d_count);
    assertEquals(1, c2.e_count);
    assertEquals(0, c2.m1_count);
    assertEquals(0, c2.m2_count);
  }

  @Test
  void argMatchClassNotMethod_3() {
    C2 c2 = new C2();
    ParserTreeResult<Void> result =
        c2.buildCommand(register).getRoot().parse("c_arg1 c_arg2 c_arg3 bob", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(2, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c2.d_count);
    assertEquals(1, c2.e_count);
    assertEquals(0, c2.m1_count);
    assertEquals(0, c2.m2_count);
  }

  @Test
  void argMatchClassSomeMethod_1() {
    C2 c2 = new C2();
    ParserTreeResult<Void> result =
        c2.buildCommand(register).getRoot().parse("c_arg1 c_arg2 c_arg3 m_arg1 m_arg2", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InputExpectedError));
    assertEquals(2, result.getCompletions().size());
    assertEquals(
        2,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c2.d_count);
    assertEquals(1, c2.e_count);
    assertEquals(0, c2.m1_count);
    assertEquals(0, c2.m2_count);
  }

  @Test
  void argMatchClassSomeMethod_2() {
    C2 c2 = new C2();
    ParserTreeResult<Void> result =
        c2.buildCommand(register).getRoot().parse("c_arg1 c_arg2 c_arg3 m_arg1 m_arg2 ", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(2, result.getCompletions().size());
    assertEquals(
        2,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c2.d_count);
    assertEquals(1, c2.e_count);
    assertEquals(0, c2.m1_count);
    assertEquals(0, c2.m2_count);
  }

  @Test
  void argMatchClassSomeMethod_3() {
    C2 c2 = new C2();
    ParserTreeResult<Void> result =
        c2.buildCommand(register).getRoot().parse("c_arg1 c_arg2 c_arg3 m_arg1 m_arg2 bob", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(2, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c2.d_count);
    assertEquals(1, c2.e_count);
    assertEquals(0, c2.m1_count);
    assertEquals(0, c2.m2_count);
  }

  @Test
  void argMatchClassAndMethod_1() {
    C2 c2 = new C2();
    ParserTreeResult<Void> result =
        c2.buildCommand(register)
            .getRoot()
            .parse("c_arg1 c_arg2 c_arg3 m_arg1 m_arg2 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(2, result.getCompletions().size());
    assertEquals(
        2,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c2.d_count);
    assertEquals(0, c2.e_count);
    assertEquals(1, c2.m1_count);
    assertEquals(0, c2.m2_count);
  }

  @Test
  void argMatchClassAndMethod_2() {
    C2 c2 = new C2();
    ParserTreeResult<Void> result =
        c2.buildCommand(register)
            .getRoot()
            .parse("c_arg1 c_arg2 c_arg3 m_arg1 m_arg2 m_arg3_m2", null);

    assertNotNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(2, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c2.d_count);
    assertEquals(0, c2.e_count);
    assertEquals(0, c2.m1_count);
    assertEquals(1, c2.m2_count);
  }

  @Test
  void argMatchClassAndMethod_3() {
    C2 c2 = new C2();
    ParserTreeResult<Void> result =
        c2.buildCommand(register)
            .getRoot()
            .parse("c_arg1 c_arg2 c_arg3 m_arg1 m_arg2 m_arg3 bob", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof UnexpectedInputError));
    assertEquals(0, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c2.d_count);
    assertEquals(1, c2.e_count);
    assertEquals(0, c2.m1_count);
    assertEquals(0, c2.m2_count);
  }

  @Test
  void noArgMatchOnMethod_1() {
    C3 c3 = new C3();
    ParserTreeResult<Void> result = c3.buildCommand(register).getRoot().parse("", null);

    assertNotNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InputExpectedError));
    assertEquals(0, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(1, c3.d_count);
    assertEquals(0, c3.e_count);
    assertEquals(0, c3.m1_count);
    assertEquals(0, c3.m2_count);
  }

  @Test
  void noArgMatchOnMethod_2() {
    C3 c3 = new C3();
    ParserTreeResult<Void> result = c3.buildCommand(register).getRoot().parse("bob", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(2, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());
  }

  @Test
  void someMatchOnMethod_1() {
    C3 c3 = new C3();
    ParserTreeResult<Void> result =
        c3.buildCommand(register).getRoot().parse("m_arg1 m_arg2", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InputExpectedError));
    assertEquals(2, result.getCompletions().size());
    assertEquals(
        2,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c3.d_count);
    assertEquals(1, c3.e_count);
    assertEquals(0, c3.m1_count);
    assertEquals(0, c3.m2_count);
  }

  @Test
  void someMatchOnMethod_2() {
    C3 c3 = new C3();
    ParserTreeResult<Void> result =
        c3.buildCommand(register).getRoot().parse("m_arg1 m_arg2 bob", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(2, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c3.d_count);
    assertEquals(1, c3.e_count);
    assertEquals(0, c3.m1_count);
    assertEquals(0, c3.m2_count);
  }

  @Test
  void argMatchMethod_1() {
    C3 c3 = new C3();
    ParserTreeResult<Void> result =
        c3.buildCommand(register).getRoot().parse("m_arg1 m_arg2 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(2, result.getCompletions().size());
    assertEquals(
        2,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c3.d_count);
    assertEquals(0, c3.e_count);
    assertEquals(1, c3.m1_count);
    assertEquals(0, c3.m2_count);
  }

  @Test
  void argMatchMethod_2() {
    C3 c3 = new C3();
    ParserTreeResult<Void> result =
        c3.buildCommand(register).getRoot().parse("m_arg1 m_arg2 m_arg3_m2", null);

    assertNotNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(2, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c3.d_count);
    assertEquals(0, c3.e_count);
    assertEquals(0, c3.m1_count);
    assertEquals(1, c3.m2_count);
  }

  // Child Class Tests
  @Test
  void noChildArgMatch_1() {
    C2 c2 = new C2();
    Child1 child1 = new Child1();
    c2.then(child1.buildCommand(register).getRoot());

    ParserTreeResult<Void> result =
        c2.buildCommand(register).getRoot().parse("c_arg1 c_arg2 c_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InputExpectedError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(1, c2.d_count);
    assertEquals(0, c2.e_count);
    assertEquals(0, c2.m1_count);
    assertEquals(0, c2.m2_count);
  }

  @Test
  void noChildArgMatch_2() {
    C2 c2 = new C2();
    Child1 child1 = new Child1();
    c2.then(child1.buildCommand(register).getRoot());

    ParserTreeResult<Void> result =
        c2.buildCommand(register).getRoot().parse("c_arg1 c_arg2 c_arg3 ", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(3, result.getCompletions().size());
    assertEquals(
        3,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c2.d_count);
    assertEquals(1, c2.e_count);
    assertEquals(0, c2.m1_count);
    assertEquals(0, c2.m2_count);
  }

  @Test
  void noChildArgMatch_3() {
    C2 c2 = new C2();
    Child1 child1 = new Child1();
    c2.then(child1.buildCommand(register).getRoot());

    ParserTreeResult<Void> result =
        c2.buildCommand(register).getRoot().parse("c_arg1 c_arg2 c_arg3 bob", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(3, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c2.d_count);
    assertEquals(1, c2.e_count);
    assertEquals(0, c2.m1_count);
    assertEquals(0, c2.m2_count);
  }

  @Test
  void someChildArgMatch_1() {
    C2 c2 = new C2();
    Child1 child1 = new Child1();
    c2.then(child1.buildCommand(register).getRoot());

    ParserTreeResult<Void> result =
        c2.buildCommand(register)
            .getRoot()
            .parse("c_arg1 c_arg2 c_arg3 child_arg1 child_arg2", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InputExpectedError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c2.d_count);
    assertEquals(1, c2.e_count);
    assertEquals(0, c2.m1_count);
    assertEquals(0, c2.m2_count);
  }

  @Test
  void someChildArgMatch_2() {
    C2 c2 = new C2();
    Child1 child1 = new Child1();
    c2.then(child1.buildCommand(register).getRoot());

    ParserTreeResult<Void> result =
        c2.buildCommand(register)
            .getRoot()
            .parse("c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 bob", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c2.d_count);
    assertEquals(1, c2.e_count);
    assertEquals(0, c2.m1_count);
    assertEquals(0, c2.m2_count);
  }

  @Test
  void someChildArgMatch_3() {
    C2 c2 = new C2();
    Child2 child2 = new Child2();
    c2.then(child2.buildCommand(register).getRoot());

    ParserTreeResult<Void> result =
        c2.buildCommand(register)
            .getRoot()
            .parse("c_arg1 c_arg2 c_arg3 child_arg1 child_arg2", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InputExpectedError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c2.d_count);
    assertEquals(1, c2.e_count);
    assertEquals(0, c2.m1_count);
    assertEquals(0, c2.m2_count);
    assertEquals(0, child2.d_count);
    assertEquals(0, child2.e_count);
    assertEquals(0, child2.m1_count);
    assertEquals(0, child2.m2_count);
  }

  @Test
  void someChildArgMatch_4() {
    C2 c2 = new C2();
    Child2 child2 = new Child2();
    c2.then(child2.buildCommand(register).getRoot());

    ParserTreeResult<Void> result =
        c2.buildCommand(register)
            .getRoot()
            .parse("c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 bob", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c2.d_count);
    assertEquals(1, c2.e_count);
    assertEquals(0, c2.m1_count);
    assertEquals(0, c2.m2_count);
    assertEquals(0, child2.d_count);
    assertEquals(0, child2.e_count);
    assertEquals(0, child2.m1_count);
    assertEquals(0, child2.m2_count);
  }

  @Test
  void childArgMatch_1() {
    C2 c2 = new C2();
    Child1 child1 = new Child1();
    c2.then(child1.buildCommand(register).getRoot());

    ParserTreeResult<Void> result =
        c2.buildCommand(register)
            .getRoot()
            .parse("c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 child_arg3", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InputExpectedError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c2.d_count);
    assertEquals(1, c2.e_count);
    assertEquals(0, c2.m1_count);
    assertEquals(0, c2.m2_count);
  }

  @Test
  void childArgMatch_2() {
    C2 c2 = new C2();
    Child2 child2 = new Child2();
    c2.then(child2.buildCommand(register).getRoot());

    ParserTreeResult<Void> result =
        c2.buildCommand(register)
            .getRoot()
            .parse("c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 child_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InputExpectedError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c2.d_count);
    assertEquals(0, c2.e_count);
    assertEquals(0, c2.m1_count);
    assertEquals(0, c2.m2_count);
    assertEquals(1, child2.d_count);
    assertEquals(0, child2.e_count);
    assertEquals(0, child2.m1_count);
    assertEquals(0, child2.m2_count);
  }

  @Test
  void childArgMatchSomeMethod_1() {
    C2 c2 = new C2();
    Child2 child2 = new Child2();
    c2.then(child2.buildCommand(register).getRoot());

    ParserTreeResult<Void> result =
        c2.buildCommand(register)
            .getRoot()
            .parse("c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 child_arg3 m_arg1 m_arg2", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InputExpectedError));
    assertEquals(2, result.getCompletions().size());
    assertEquals(
        2,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c2.d_count);
    assertEquals(0, c2.e_count);
    assertEquals(0, c2.m1_count);
    assertEquals(0, c2.m2_count);
    assertEquals(0, child2.d_count);
    assertEquals(1, child2.e_count);
    assertEquals(0, child2.m1_count);
    assertEquals(0, child2.m2_count);
  }

  @Test
  void childArgMatchSomeMethod_2() {
    C2 c2 = new C2();
    Child2 child2 = new Child2();
    c2.then(child2.buildCommand(register).getRoot());

    ParserTreeResult<Void> result =
        c2.buildCommand(register)
            .getRoot()
            .parse("c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 child_arg3 m_arg1 m_arg2 bob", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(2, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c2.d_count);
    assertEquals(0, c2.e_count);
    assertEquals(0, c2.m1_count);
    assertEquals(0, c2.m2_count);
    assertEquals(0, child2.d_count);
    assertEquals(1, child2.e_count);
    assertEquals(0, child2.m1_count);
    assertEquals(0, child2.m2_count);
  }

  @Test
  void childArgMatchMethod_1() {
    C2 c2 = new C2();
    Child2 child2 = new Child2();
    c2.then(child2.buildCommand(register).getRoot());

    ParserTreeResult<Void> result =
        c2.buildCommand(register)
            .getRoot()
            .parse(
                "c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 child_arg3 m_arg1 m_arg2 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(2, result.getCompletions().size());
    assertEquals(
        2,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c2.d_count);
    assertEquals(0, c2.e_count);
    assertEquals(0, c2.m1_count);
    assertEquals(0, c2.m2_count);
    assertEquals(0, child2.d_count);
    assertEquals(0, child2.e_count);
    assertEquals(1, child2.m1_count);
    assertEquals(0, child2.m2_count);
  }

  @Test
  void childArgMatchMethod_2() {
    C2 c2 = new C2();
    Child2 child2 = new Child2();
    c2.then(child2.buildCommand(register).getRoot());

    ParserTreeResult<Void> result =
        c2.buildCommand(register)
            .getRoot()
            .parse(
                "c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 child_arg3 m_arg1 m_arg2 m_arg3 bob",
                null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof UnexpectedInputError));
    assertEquals(0, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c2.d_count);
    assertEquals(0, c2.e_count);
    assertEquals(0, c2.m1_count);
    assertEquals(0, c2.m2_count);
    assertEquals(0, child2.d_count);
    assertEquals(1, child2.e_count);
    assertEquals(0, child2.m1_count);
    assertEquals(0, child2.m2_count);
  }

  @Test
  void childArgMatchMethod_3() {
    C2 c2 = new C2();
    Child2 child2 = new Child2();
    c2.then(child2.buildCommand(register).getRoot());

    ParserTreeResult<Void> result =
        c2.buildCommand(register)
            .getRoot()
            .parse(
                "c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 child_arg3 m_arg1 m_arg2 m_arg3_m2",
                null);

    assertNotNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(2, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c2.d_count);
    assertEquals(0, c2.e_count);
    assertEquals(0, c2.m1_count);
    assertEquals(0, c2.m2_count);
    assertEquals(0, child2.d_count);
    assertEquals(0, child2.e_count);
    assertEquals(0, child2.m1_count);
    assertEquals(1, child2.m2_count);
  }

  @Test
  void extendedClass_1() {
    C2Extended c2 = new C2Extended();

    ParserTreeResult<Void> result = c2.buildCommand(register).getRoot().parse("bob", null);

    assertNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());
  }

  @Test
  void extendedClass_2() {
    C2 c2 = new C2();
    ParserTreeResult<Void> result =
        c2.buildCommand(register).getRoot().parse("c_arg1 c_arg2 c_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InputExpectedError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(1, c2.d_count);
    assertEquals(0, c2.e_count);
    assertEquals(0, c2.m1_count);
    assertEquals(0, c2.m2_count);
  }

  @Test
  void parameters_1() {
    ParamClass c = new ParamClass();
    ParserTreeResult<Void> result =
        c.buildCommand(register).getRoot().parse("c_arg1 arg1 arg2 23", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertEquals(0, result.getErrors().size());
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(1, c.m1_count);
    assertEquals("arg1", result.getExecuteCandidate().getResults().get(0));
    assertEquals("arg2", result.getExecuteCandidate().getResults().get(1));
    assertEquals(23, result.getExecuteCandidate().getResults().get(2));
  }

  @Test
  void complete_1() {
    CompletionClass c = new CompletionClass();
    ParserTreeResult<Void> result = c.buildCommand(register).getRoot().parse("", null);

    assertNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InputExpectedError));
    assertEquals(0, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());
  }

  @Test
  void complete_2() {
    CompletionClass c = new CompletionClass();
    ParserTreeResult<Void> result = c.buildCommand(register).getRoot().parse(" ", null);

    assertNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InputExpectedError));
    assertEquals(0, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());
  }

  @Test
  void complete_3() {
    CompletionClass c = new CompletionClass();
    ParserTreeResult<Void> result = c.buildCommand(register).getRoot().parse("b", null);

    assertNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());
  }

  @Test
  void complete_4() {
    CompletionClass c = new CompletionClass();
    ParserTreeResult<Void> result = c.buildCommand(register).getRoot().parse("f", null);

    assertNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        2,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());
  }

  @Test
  void complete_5() {
    CompletionClass c = new CompletionClass();
    ParserTreeResult<Void> result = c.buildCommand(register).getRoot().parse("firs", null);

    assertNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());
  }

  @Test
  void complete_6() {
    CompletionClass c = new CompletionClass();
    ParserTreeResult<Void> result = c.buildCommand(register).getRoot().parse("firs m", null);

    assertNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(0, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());
  }

  @Test
  void complete_7() {
    CompletionClass c = new CompletionClass();
    ParserTreeResult<Void> result = c.buildCommand(register).getRoot().parse("first m", null);

    assertNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(2, result.getCompletions().size());
    assertEquals(
        4,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());
  }

  @Test
  void complete_8() {
    CompletionClass c = new CompletionClass();
    ParserTreeResult<Void> result = c.buildCommand(register).getRoot().parse("first marta", null);

    assertNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InputExpectedError));
    assertEquals(2, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());
  }

  @Test
  void complete_9() {
    CompletionClass c = new CompletionClass();
    ParserTreeResult<Void> result = c.buildCommand(register).getRoot().parse("first marta ", null);

    assertNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());
  }

  @Test
  void complete_10() {
    CompletionClass c = new CompletionClass();
    ParserTreeResult<Void> result = c.buildCommand(register).getRoot().parse("first mike", null);

    assertNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InputExpectedError));
    assertEquals(2, result.getCompletions().size());
    assertEquals(
        2,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());
  }

  @Test
  void complete_11() {
    CompletionClass c = new CompletionClass();
    ParserTreeResult<Void> result = c.buildCommand(register).getRoot().parse("first mike ", null);

    assertNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(2, result.getCompletions().size());
    assertEquals(
        3,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());
  }

  @Test
  void complete_12() {
    CompletionClass c = new CompletionClass();
    ParserTreeResult<Void> result = c.buildCommand(register).getRoot().parse("first mike p", null);

    assertNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(2, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());
  }

  @Test
  void switchTest_1() {
    SwitchClass1 c1 = new SwitchClass1();

    ParserTreeResult<Void> result =
        c1.buildCommand(register).getRoot().parse("opt1 mike art", null);

    assertNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(0, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());
  }

  @Test
  void switchTest_2() {
    SwitchClass1 c1 = new SwitchClass1();

    ParserTreeResult<Void> result =
        c1.buildCommand(register).getRoot().parse("-c_sw1 opt1 -m_sw2 mike art", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c1.m1_count);
    assertEquals(1, c1.m2_count);
    assertEquals(0, c1.m3_count);
    assertEquals(0, c1.m4_count);
    assertEquals(0, c1.m5_count);
    assertEquals(0, c1.m6_count);
    assertNull(c1.m1_arg1);
    assertEquals("opt1", c1.m2_arg1);
    assertNull(c1.m3_arg1);
    assertNull(c1.m4_arg1);
    assertNull(c1.m5_arg1);
    assertNull(c1.m6_arg1);
    assertNull(c1.m1_arg2);
    assertEquals("mike", c1.m2_arg2);
    assertNull(c1.m3_arg2);
    assertNull(c1.m4_arg2);
    assertNull(c1.m5_arg2);
    assertNull(c1.m6_arg2);
  }

  @Test
  void switchTest_3() {
    SwitchClass1 c1 = new SwitchClass1();

    ParserTreeResult<Void> result =
        c1.buildCommand(register).getRoot().parse("-c_sw1 opt1 art -m_sw2 mike", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c1.m1_count);
    assertEquals(1, c1.m2_count);
    assertEquals(0, c1.m3_count);
    assertEquals(0, c1.m4_count);
    assertEquals(0, c1.m5_count);
    assertEquals(0, c1.m6_count);
    assertNull(c1.m1_arg1);
    assertEquals("opt1", c1.m2_arg1);
    assertNull(c1.m3_arg1);
    assertNull(c1.m4_arg1);
    assertNull(c1.m5_arg1);
    assertNull(c1.m6_arg1);
    assertNull(c1.m1_arg2);
    assertEquals("mike", c1.m2_arg2);
    assertNull(c1.m3_arg2);
    assertNull(c1.m4_arg2);
    assertNull(c1.m5_arg2);
    assertNull(c1.m6_arg2);
  }

  @Test
  void switchTest_4() {
    SwitchClass1 c1 = new SwitchClass1();

    ParserTreeResult<Void> result =
        c1.buildCommand(register).getRoot().parse("-m_sw2 mike art -c_sw1 opt1", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c1.m1_count);
    assertEquals(1, c1.m2_count);
    assertEquals(0, c1.m3_count);
    assertEquals(0, c1.m4_count);
    assertEquals(0, c1.m5_count);
    assertEquals(0, c1.m6_count);
    assertNull(c1.m1_arg1);
    assertEquals("opt1", c1.m2_arg1);
    assertNull(c1.m3_arg1);
    assertNull(c1.m4_arg1);
    assertNull(c1.m5_arg1);
    assertNull(c1.m6_arg1);
    assertNull(c1.m1_arg2);
    assertEquals("mike", c1.m2_arg2);
    assertNull(c1.m3_arg2);
    assertNull(c1.m4_arg2);
    assertNull(c1.m5_arg2);
    assertNull(c1.m6_arg2);
  }

  @Test
  void switchTest_5() {
    SwitchClass1 c1 = new SwitchClass1();

    ParserTreeResult<Void> result =
        c1.buildCommand(register).getRoot().parse("art -c_sw1 opt1 -m_sw2 mike", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c1.m1_count);
    assertEquals(1, c1.m2_count);
    assertEquals(0, c1.m3_count);
    assertEquals(0, c1.m4_count);
    assertEquals(0, c1.m5_count);
    assertEquals(0, c1.m6_count);
    assertNull(c1.m1_arg1);
    assertEquals("opt1", c1.m2_arg1);
    assertNull(c1.m3_arg1);
    assertNull(c1.m4_arg1);
    assertNull(c1.m5_arg1);
    assertNull(c1.m6_arg1);
    assertNull(c1.m1_arg2);
    assertEquals("mike", c1.m2_arg2);
    assertNull(c1.m3_arg2);
    assertNull(c1.m4_arg2);
    assertNull(c1.m5_arg2);
    assertNull(c1.m6_arg2);
  }

  @Test
  void switchTest_6() {
    SwitchClass1 c1 = new SwitchClass1();

    ParserTreeResult<Void> result =
        c1.buildCommand(register).getRoot().parse("art -m_sw2 mike -c_sw1 opt1", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c1.m1_count);
    assertEquals(1, c1.m2_count);
    assertEquals(0, c1.m3_count);
    assertEquals(0, c1.m4_count);
    assertEquals(0, c1.m5_count);
    assertEquals(0, c1.m6_count);
    assertNull(c1.m1_arg1);
    assertEquals("opt1", c1.m2_arg1);
    assertNull(c1.m3_arg1);
    assertNull(c1.m4_arg1);
    assertNull(c1.m5_arg1);
    assertNull(c1.m6_arg1);
    assertNull(c1.m1_arg2);
    assertEquals("mike", c1.m2_arg2);
    assertNull(c1.m3_arg2);
    assertNull(c1.m4_arg2);
    assertNull(c1.m5_arg2);
    assertNull(c1.m6_arg2);
  }

  @Test
  void switchTest_7() {
    SwitchClass1 c1 = new SwitchClass1();

    ParserTreeResult<Void> result =
        c1.buildCommand(register).getRoot().parse("-c_sw1 opt1 -m_sw1 bob art", null);

    assertNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(0, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());
  }

  @Test
  void switchTest_8() {
    SwitchClass1 c1 = new SwitchClass1();

    ParserTreeResult<Void> result =
        c1.buildCommand(register).getRoot().parse("-c_sw1 opt1 -m_sw1 milly art", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(1, c1.m1_count);
    assertEquals(0, c1.m2_count);
    assertEquals(0, c1.m3_count);
    assertEquals(0, c1.m4_count);
    assertEquals(0, c1.m5_count);
    assertEquals(0, c1.m6_count);
    assertEquals("opt1", c1.m1_arg1);
    assertNull(c1.m2_arg1);
    assertNull(c1.m3_arg1);
    assertNull(c1.m4_arg1);
    assertNull(c1.m5_arg1);
    assertNull(c1.m6_arg1);
    assertEquals("milly", c1.m1_arg2);
    assertNull(c1.m2_arg2);
    assertNull(c1.m3_arg2);
    assertNull(c1.m4_arg2);
    assertNull(c1.m5_arg2);
    assertNull(c1.m6_arg2);
  }

  @Test
  void switchTest_9() {
    SwitchClass1 c1 = new SwitchClass1();

    ParserTreeResult<Void> result =
        c1.buildCommand(register).getRoot().parse("art -c_sw1 opt1 -m_sw1 milly", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(2, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(1, c1.m1_count);
    assertEquals(0, c1.m2_count);
    assertEquals(0, c1.m3_count);
    assertEquals(0, c1.m4_count);
    assertEquals(0, c1.m5_count);
    assertEquals(0, c1.m6_count);
    assertEquals("opt1", c1.m1_arg1);
    assertNull(c1.m2_arg1);
    assertNull(c1.m3_arg1);
    assertNull(c1.m4_arg1);
    assertNull(c1.m5_arg1);
    assertNull(c1.m6_arg1);
    assertEquals("milly", c1.m1_arg2);
    assertNull(c1.m2_arg2);
    assertNull(c1.m3_arg2);
    assertNull(c1.m4_arg2);
    assertNull(c1.m5_arg2);
    assertNull(c1.m6_arg2);
  }

  @Test
  void switchTest_10() {
    SwitchClass1 c1 = new SwitchClass1();

    ParserTreeResult<Void> result =
        c1.buildCommand(register).getRoot().parse("art -m_sw1 zoe -c_sw1 opt2", null);


    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c1.m1_count);
    assertEquals(0, c1.m2_count);
    assertEquals(1, c1.m3_count);
    assertEquals(0, c1.m4_count);
    assertEquals(0, c1.m5_count);
    assertEquals(0, c1.m6_count);
    assertNull(c1.m1_arg1);
    assertNull(c1.m2_arg1);
    assertEquals("opt2", c1.m3_arg1);
    assertNull(c1.m4_arg1);
    assertNull(c1.m5_arg1);
    assertNull(c1.m6_arg1);
    assertNull(c1.m1_arg2);
    assertNull(c1.m2_arg2);
    assertEquals("zoe", c1.m3_arg2);
    assertNull(c1.m4_arg2);
    assertNull(c1.m5_arg2);
    assertNull(c1.m6_arg2);
  }

  @Test
  void switchTest_11() {
    SwitchClass1 c1 = new SwitchClass1();

    ParserTreeResult<Void> result =
        c1.buildCommand(register).getRoot().parse("-c_sw1 opt1 -m_sw3 mike art plate", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(2, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c1.m1_count);
    assertEquals(0, c1.m2_count);
    assertEquals(0, c1.m3_count);
    assertEquals(1, c1.m4_count);
    assertEquals(0, c1.m5_count);
    assertEquals(0, c1.m6_count);
    assertNull(c1.m1_arg1);
    assertNull(c1.m2_arg1);
    assertNull(c1.m3_arg1);
    assertEquals("opt1", c1.m4_arg1);
    assertNull(c1.m5_arg1);
    assertNull(c1.m6_arg1);
    assertNull(c1.m1_arg2);
    assertNull(c1.m2_arg2);
    assertNull(c1.m3_arg2);
    assertEquals("mike", c1.m4_arg2);
    assertNull(c1.m5_arg2);
    assertNull(c1.m6_arg2);
  }

  @Test
  void switchTest_12() {
    SwitchClass1 c1 = new SwitchClass1();

    ParserTreeResult<Void> result =
        c1.buildCommand(register).getRoot().parse("-c_sw1 opt1 -m_sw3 mike art", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InputExpectedError));
    assertEquals(3, result.getCompletions().size());
    assertEquals(
        3,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c1.m1_count);
    assertEquals(0, c1.m2_count);
    assertEquals(0, c1.m3_count);
    assertEquals(0, c1.m4_count);
    assertEquals(0, c1.m5_count);
    assertEquals(1, c1.m6_count);
    assertNull(c1.m1_arg1);
    assertNull(c1.m2_arg1);
    assertNull(c1.m3_arg1);
    assertNull(c1.m4_arg1);
    assertNull(c1.m5_arg1);
    assertEquals("opt1", c1.m6_arg1);
    assertNull(c1.m1_arg2);
    assertNull(c1.m2_arg2);
    assertNull(c1.m3_arg2);
    assertNull(c1.m4_arg2);
    assertNull(c1.m5_arg2);
    assertEquals("mike", c1.m6_arg2);
  }

  @Test
  void switchTest_13() {
    SwitchClass1 c1 = new SwitchClass1();

    ParserTreeResult<Void> result =
        c1.buildCommand(register).getRoot().parse("art angel -m_sw3 mike -c_sw1 opt1", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c1.m1_count);
    assertEquals(0, c1.m2_count);
    assertEquals(0, c1.m3_count);
    assertEquals(0, c1.m4_count);
    assertEquals(1, c1.m5_count);
    assertEquals(0, c1.m6_count);
    assertNull(c1.m1_arg1);
    assertNull(c1.m2_arg1);
    assertNull(c1.m3_arg1);
    assertNull(c1.m4_arg1);
    assertEquals("opt1", c1.m5_arg1);
    assertNull(c1.m6_arg1);
    assertNull(c1.m1_arg2);
    assertNull(c1.m2_arg2);
    assertNull(c1.m3_arg2);
    assertNull(c1.m4_arg2);
    assertEquals("mike", c1.m5_arg2);
    assertNull(c1.m6_arg2);
  }

  @Test
  void switchWithDefault_1() {
    SwitchClassWithDefaults1 c1 = new SwitchClassWithDefaults1();

    ParserTreeResult<Void> result =
        c1.buildCommand(register).getRoot().parse("art -c_sw1 opt1", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InputExpectedError));
    assertEquals(2, result.getCompletions().size());
    assertEquals(
        2,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(1, c1.m1_count);
    assertEquals(0, c1.m2_count);
    assertEquals("opt1", c1.m1_arg1);
    assertNull(c1.m2_arg1);
    assertEquals("marta", c1.m1_arg2);
    assertNull(c1.m2_arg2);
  }

  @Test
  void switchWithDefault_2() {
    SwitchClassWithDefaults1 c1 = new SwitchClassWithDefaults1();

    ParserTreeResult<Void> result =
        c1.buildCommand(register).getRoot().parse("art -c_sw1 opt1 -m_sw1 milly", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(2, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, c1.m1_count);
    assertEquals(1, c1.m2_count);
    assertNull(c1.m1_arg1);
    assertEquals("opt1", c1.m2_arg1);
    assertNull(c1.m1_arg2);
    assertEquals("milly", c1.m2_arg2);
  }

  @Arg("c_arg1 c_arg2 c_arg3")
  static class C1 extends AnnotationCommand<Void> {}

  @Arg("c_arg1 c_arg2 c_arg3")
  static class C2 extends AnnotationCommand<Void> {
    int d_count = 0;
    int e_count = 0;
    int m1_count = 0;
    int m2_count = 0;

    @Default
    public void d() {
      d_count++;
    }

    @Error
    public void e(ErrorContext<Void> context) {
      e_count++;
    }

    @Arg("m_arg1 m_arg2 m_arg3")
    public void m1() {
      m1_count++;
    }

    @Arg("m_arg1 m_arg2 m_arg3_m2")
    public void m2() {
      m2_count++;
    }
  }

  static class C2Extended extends C2 {}

  static class C3 extends AnnotationCommand<Void> {
    int d_count = 0;
    int e_count = 0;
    int m1_count = 0;
    int m2_count = 0;

    @Default
    public void d() {
      d_count++;
    }

    @Error
    public void e(ErrorContext<Void> context) {
      e_count++;
    }

    @Arg("m_arg1 m_arg2 m_arg3")
    public void m1() {
      m1_count++;
    }

    @Arg("m_arg1 m_arg2 m_arg3_m2")
    public void m2() {
      m2_count++;
    }
  }

  @Arg("child_arg1 child_arg2 child_arg3")
  static class Child1 extends AnnotationCommand<Void> {}

  @Arg("child_arg1 child_arg2 child_arg3")
  static class Child2 extends AnnotationCommand<Void> {
    int d_count = 0;
    int e_count = 0;
    int m1_count = 0;
    int m2_count = 0;

    @Default
    public void d() {
      d_count++;
    }

    @Error
    public void e(ErrorContext<Void> context) {
      e_count++;
    }

    @Arg("m_arg1 m_arg2 m_arg3")
    public void m1() {
      m1_count++;
    }

    @Arg("m_arg1 m_arg2 m_arg3_m2")
    public void m2() {
      m2_count++;
    }
  }

  @Arg("c_arg1 @string")
  static class ParamClass extends AnnotationCommand<Void> {
    int m1_count = 0;

    @Arg("@string @int")
    public void m1(String p1, String p2, Integer p3) {
      m1_count++;
    }
  }

  @Arg("first|fire|second")
  static class CompletionClass extends AnnotationCommand<Void> {
    @Arg("mike|milly|marta art")
    public void m1() {}

    @Arg("alice|bob|mike plate|yeet")
    public void m2() {}
  }

  @Arg("opt1|opt2|opt3(switch=c_sw1, suppress=false)")
  static class SwitchClass1 extends AnnotationCommand<Void> {
    int m1_count = 0;
    int m2_count = 0;
    int m3_count = 0;
    int m4_count = 0;
    int m5_count = 0;
    int m6_count = 0;
    String m1_arg1 = null;
    String m2_arg1 = null;
    String m3_arg1 = null;
    String m4_arg1 = null;
    String m5_arg1 = null;
    String m6_arg1 = null;
    String m1_arg2 = null;
    String m2_arg2 = null;
    String m3_arg2 = null;
    String m4_arg2 = null;
    String m5_arg2 = null;
    String m6_arg2 = null;

    @Arg("mike|milly|marta(switch=m_sw1, suppress=false) art")
    public void m1(String arg1, String arg2) {
      m1_count++;
      m1_arg1 = arg1;
      m1_arg2 = arg2;
    }

    @Arg("mike|milly(switch=m_sw2, suppress=false) art")
    public void m2(String arg1, String arg2) {
      m2_count++;
      m2_arg1 = arg1;
      m2_arg2 = arg2;
    }

    @Arg("sue|zoe(switch=m_sw1, suppress=false) art")
    public void m3(String arg1, String arg2) {
      m3_count++;
      m3_arg1 = arg1;
      m3_arg2 = arg2;
    }

    @Arg("mike|milly(switch=m_sw3, suppress=false) art plate")
    public void m4(String arg1, String arg2) {
      m4_count++;
      m4_arg1 = arg1;
      m4_arg2 = arg2;
    }

    @Arg("mike|zoe(switch=m_sw3, suppress=false) art angel")
    public void m5(String arg1, String arg2) {
      m5_count++;
      m5_arg1 = arg1;
      m5_arg2 = arg2;
    }

    @Arg("mike|zoe(switch=m_sw3, suppress=false) art")
    public void m6(String arg1, String arg2) {
      m6_count++;
      m6_arg1 = arg1;
      m6_arg2 = arg2;
    }
  }

  @Arg("opt1|opt2|opt3(switch=c_sw1, suppress=false)")
  static class SwitchClassWithDefaults1 extends AnnotationCommand<Void> {
    int m1_count = 0;
    int m2_count = 0;
    String m1_arg1 = null;
    String m2_arg1 = null;
    String m1_arg2 = null;
    String m2_arg2 = null;

    @Arg("mike|marta(switch=m_sw1, suppress=false, default=marta) art")
    public void m1(String arg1, String arg2) {
      m1_count++;
      m1_arg1 = arg1;
      m1_arg2 = arg2;
    }

    @Arg("mike|milly(switch=m_sw1, suppress=false) art")
    public void m2(String arg1, String arg2) {
      m2_count++;
      m2_arg1 = arg1;
      m2_arg2 = arg2;
    }
  }

}
