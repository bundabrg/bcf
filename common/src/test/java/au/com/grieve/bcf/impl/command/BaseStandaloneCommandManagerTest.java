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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.com.grieve.bcf.ErrorContext;
import au.com.grieve.bcf.ParserTree;
import au.com.grieve.bcf.ParserTreeHandler;
import au.com.grieve.bcf.ParserTreeResult;
import au.com.grieve.bcf.annotation.Arg;
import au.com.grieve.bcf.annotation.Command;
import au.com.grieve.bcf.annotation.Default;
import au.com.grieve.bcf.annotation.Error;
import au.com.grieve.bcf.impl.error.InputExpectedError;
import au.com.grieve.bcf.impl.error.InvalidOptionError;
import au.com.grieve.bcf.impl.error.UnexpectedInputError;
import au.com.grieve.bcf.impl.error.UnknownCommandError;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class BaseStandaloneCommandManagerTest {

  Consumer<List<ParserTree<Void>>> debugWalker =
      n -> {
        System.err.printf("%" + n.size() + "s", "");
        System.err.println(n.get(n.size() - 1));
      };

  @Test
  public void registerCommandNoCommand() {
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    assertThrows(RuntimeException.class, () -> manager.registerCommand(new ClassNoCommand()));
  }

  @Test
  public void registerCommandEmptyCommand() {
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    assertThrows(
        RuntimeException.class, () -> manager.registerCommand(new ClassWithEmptyCommand()));
  }

  @Test
  public void executeCommandSingle_1() {
    ClassWithSingleCommand c1 = new ClassWithSingleCommand();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("", null);

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
    assertEquals(1, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(0, c1.m1_count);
  }

  @Test
  public void executeCommandSingle_2() {
    ClassWithSingleCommand c1 = new ClassWithSingleCommand();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("bob", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof UnknownCommandError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(1, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(0, c1.m1_count);
  }

  @Test
  public void executeCommandSingle_3() {
    ClassWithSingleCommand c1 = new ClassWithSingleCommand();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("cmd1", null);

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
    assertEquals(0, manager.e_count);
    assertEquals(1, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(0, c1.m1_count);
  }

  @Test
  public void executeCommandSingle_4() {
    ClassWithSingleCommand c1 = new ClassWithSingleCommand();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("cmd1 bob", null);

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
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(1, c1.e_count);
    assertEquals(0, c1.m1_count);
  }

  @Test
  public void executeCommandSingle_5() {
    ClassWithSingleCommand c1 = new ClassWithSingleCommand();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("cmd1 m_arg1 m_arg2", null);

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
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(1, c1.e_count);
    assertEquals(0, c1.m1_count);
  }

  @Test
  public void executeCommandSingle_6() {
    ClassWithSingleCommand c1 = new ClassWithSingleCommand();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("cmd1 m_arg1 m_arg2 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertEquals(0, result.getErrors().size());
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(1, c1.m1_count);
  }

  @Test
  public void executeCommandSingle_6b() {
    ClassWithSingleCommand c1 = new ClassWithSingleCommand();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("cmd1 m_arg1 m_arg2 m_arg3 bob", null);

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
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(1, c1.e_count);
    assertEquals(0, c1.m1_count);
  }

  @Test
  public void executeCommandSingle_7() {
    ClassWithSingleCommandSingleInput c1 = new ClassWithSingleCommandSingleInput();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("cmd1 m_arg1 m_arg2 m_arg3", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(0, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(1, c1.e_count);
    assertEquals(0, c1.m1_count);
  }

  @Test
  public void executeCommandSingle_8() {
    ClassWithSingleCommandSingleInput c1 = new ClassWithSingleCommandSingleInput();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("cmd1 m_arg2 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertEquals(0, result.getErrors().size());
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(1, c1.m1_count);
  }

  @Test
  public void executeCommandSingle_9() {
    ClassWithSingleCommandMultiInput c1 = new ClassWithSingleCommandMultiInput();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("cmd1", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertEquals(0, result.getErrors().size());
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(1, c1.m1_count);
  }

  @Test
  public void executeCommandSingleParamSingle_1() {
    ClassWithSingleCommandSingleParam c1 = new ClassWithSingleCommandSingleParam();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("", null);

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
    assertEquals(1, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(0, c1.m1_count);
  }

  @Test
  public void executeCommandSingleParamSingle_2() {
    ClassWithSingleCommandSingleParam c1 = new ClassWithSingleCommandSingleParam();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("bob", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof UnknownCommandError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(1, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(0, c1.m1_count);
  }

  @Test
  public void executeCommandSingleParamSingle_3() {
    ClassWithSingleCommandSingleParam c1 = new ClassWithSingleCommandSingleParam();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("cmd1", null);

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
    assertEquals(1, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(0, c1.m1_count);
  }

  @Test
  public void executeCommandSingleParamSingle_4() {
    ClassWithSingleCommandSingleParam c1 = new ClassWithSingleCommandSingleParam();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("cmd1 bob", null);

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
    assertEquals(1, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(0, c1.m1_count);
  }

  @Test
  public void executeCommandSingleParamSingle_5() {
    ClassWithSingleCommandSingleParam c1 = new ClassWithSingleCommandSingleParam();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("cmd1 m_arg1 m_arg2", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(0, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(1, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(0, c1.m1_count);
  }

  @Test
  public void executeCommandSingleParamSingle_6() {
    ClassWithSingleCommandSingleParam c1 = new ClassWithSingleCommandSingleParam();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("cmd1 m_arg1 m_arg2 m_arg3", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(0, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(1, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(0, c1.m1_count);
  }

  @Test
  public void executeCommandSingleParamSingle_7() {
    ClassWithSingleCommandSingleParam c1 = new ClassWithSingleCommandSingleParam();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("cmd1 m_arg2 m_arg3", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(0, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(1, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(0, c1.m1_count);
  }

  @Test
  public void executeCommandSingleParamSingle_8() {
    ClassWithSingleCommandSingleParam c1 = new ClassWithSingleCommandSingleParam();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("cmd1 c_arg1 m_arg1 m_arg2 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertEquals(0, result.getErrors().size());
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(1, c1.m1_count);
  }

  @Test
  public void executeCommandSingleParamSingle_9() {
    ClassWithSingleCommandSingleParamSingleInput c1 =
        new ClassWithSingleCommandSingleParamSingleInput();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("cmd1 m_arg1 m_arg2 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertEquals(0, result.getErrors().size());
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(1, c1.m1_count);
  }

  @Test
  public void executeCommandSingleParamSingle_10() {
    ClassWithSingleCommandSingleParamMultipleInput c1 =
        new ClassWithSingleCommandSingleParamMultipleInput();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("cmd1 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertEquals(0, result.getErrors().size());
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(1, c1.m1_count);
  }

  @Test
  public void executeCommandSingleParamMulti_1() {
    ClassWithSingleCommandMultiParam c1 = new ClassWithSingleCommandMultiParam();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("", null);

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
    assertEquals(1, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(0, c1.m1_count);
  }

  @Test
  public void executeCommandSingleParamMulti_2() {
    ClassWithSingleCommandMultiParam c1 = new ClassWithSingleCommandMultiParam();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result =
        manager.parse("cmd1 c_arg1 c_arg2 c_arg3 m_arg1 m_arg2 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertEquals(0, result.getErrors().size());
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(1, c1.m1_count);
  }

  @Test
  public void executeCommandSingleParamMulti_3() {
    ClassWithSingleCommandMultiParamSingleInput c1 =
        new ClassWithSingleCommandMultiParamSingleInput();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("cmd1 c_arg2 c_arg3 m_arg1 m_arg2 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertEquals(0, result.getErrors().size());
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(1, c1.m1_count);
  }

  @Test
  public void executeCommandSingleParamMulti_4() {
    ClassWithSingleCommandMultiParamMultiInput c1 =
        new ClassWithSingleCommandMultiParamMultiInput();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("cmd1 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertEquals(0, result.getErrors().size());
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(1, c1.m1_count);
  }

  @Test
  public void executeCommandMulti_1() {
    ClassWithMultiCommand c1 = new ClassWithMultiCommand();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("cmd1 m_arg1 m_arg2 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(1, c1.m1_count);
    assertEquals(0, c1.m2_count);
  }

  @Test
  public void executeCommandMulti_2() {
    ClassWithMultiCommand c1 = new ClassWithMultiCommand();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("cmd2 m_arg1 m_arg2 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(1, c1.m1_count);
    assertEquals(0, c1.m2_count);
  }

  @Test
  public void executeCommandMulti_3() {
    ClassWithMultiCommand c1 = new ClassWithMultiCommand();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("bob m_arg1 m_arg2 m_arg3", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof UnknownCommandError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(1, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(0, c1.m1_count);
    assertEquals(0, c1.m2_count);
  }

  @Test
  public void executeCommandMultiParamSingle_1() {
    ClassWithMultiCommandSingleParam c1 = new ClassWithMultiCommandSingleParam();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("cmd1 c_arg1 m_arg1 m_arg2 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(1, c1.m1_count);
    assertEquals(0, c1.m2_count);
  }

  @Test
  public void executeCommandMultiParamSingle_2() {
    ClassWithMultiCommandSingleParam c1 = new ClassWithMultiCommandSingleParam();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("cmd2 c_arg1 m_arg1 m_arg2 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(1, c1.m1_count);
    assertEquals(0, c1.m2_count);
  }

  @Test
  public void executeCommandMultiParamMulti_1() {
    ClassWithMultiCommandMultiParam c1 = new ClassWithMultiCommandMultiParam();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result =
        manager.parse("cmd1 c_arg1 c_arg2 c_arg3 m_arg1 m_arg2 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertEquals(0, result.getErrors().size());
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(1, c1.m1_count);
  }

  @Test
  public void executeCommandMultiParamMulti_2() {
    ClassWithMultiCommandMultiParam c1 = new ClassWithMultiCommandMultiParam();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result =
        manager.parse("cmd2 c_arg1 c_arg2 c_arg3 m_arg1 m_arg2 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertEquals(0, result.getErrors().size());
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(1, c1.m1_count);
  }

  @Test
  public void registerOverlap_1() {
    ClassWithSingleCommand c1 = new ClassWithSingleCommand();
    ClassWithSingleCommand2 c2 = new ClassWithSingleCommand2();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    manager.registerCommand(c2);
    ParserTreeResult<Void> result = manager.parse("cmd1 m_arg1 m_arg2 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertEquals(0, result.getErrors().size());
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(1, c1.m1_count);
    assertEquals(0, c2.d_count);
    assertEquals(0, c2.e_count);
    assertEquals(0, c2.m1_count);
  }

  @Test
  public void registerOverlap_2() {
    ClassWithOverlappingName c1 = new ClassWithOverlappingName();
    ClassWithOverlappingAlias c2 = new ClassWithOverlappingAlias();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    manager.registerCommand(c2);
    ParserTreeResult<Void> result = manager.parse("cmd1 m_arg1 m_arg2 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertEquals(0, result.getErrors().size());
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(1, c1.m1_count);
    assertEquals(0, c2.d_count);
    assertEquals(0, c2.e_count);
    assertEquals(0, c2.m1_count);
  }

  @Test
  public void registerOverlap_3() {
    ClassWithOverlappingName c1 = new ClassWithOverlappingName();
    ClassWithOverlappingAlias c2 = new ClassWithOverlappingAlias();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    manager.registerCommand(c2);
    ParserTreeResult<Void> result = manager.parse("cmd5 m_arg1 m_arg2 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertEquals(0, result.getErrors().size());
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(1, c1.m1_count);
    assertEquals(0, c2.d_count);
    assertEquals(0, c2.e_count);
    assertEquals(0, c2.m1_count);
  }

  @Test
  public void registerOverlap_4() {
    ClassWithOverlappingName c1 = new ClassWithOverlappingName();
    ClassWithOverlappingAlias c2 = new ClassWithOverlappingAlias();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    manager.registerCommand(c2);
    ParserTreeResult<Void> result = manager.parse("cmd4 m_arg1 m_arg2 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertEquals(0, result.getErrors().size());
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(0, c1.m1_count);
    assertEquals(0, c2.d_count);
    assertEquals(0, c2.e_count);
    assertEquals(1, c2.m1_count);
  }

  @Test
  public void childCommand_1() {
    ParentCommand c1 = new ParentCommand();
    ChildCommand c2 = new ChildCommand();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    manager.registerCommand(ParentCommand.class, c2);
    ParserTreeResult<Void> result =
        manager.parse("cmd1 c_arg1 c_arg2 c_arg3 m_arg1 m_arg2 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(1, c1.m1_count);
    assertEquals(0, c2.d_count);
    assertEquals(0, c2.e_count);
    assertEquals(0, c2.m1_count);
  }

  @Test
  public void childCommand_2() {
    ParentCommand c1 = new ParentCommand();
    ChildCommand c2 = new ChildCommand();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    manager.registerCommand(ParentCommand.class, c2);
    ParserTreeResult<Void> result =
        manager.parse("cmd1 c_arg1 c_arg2 c_arg3 child m_arg1 m_arg2 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(0, c1.m1_count);
    assertEquals(0, c2.d_count);
    assertEquals(0, c2.e_count);
    assertEquals(1, c2.m1_count);
  }

  @Test
  public void childCommand_3() {
    ParentCommand c1 = new ParentCommand();
    ChildCommand c2 = new ChildCommand();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    manager.registerCommand(ParentCommand.class, c2);
    ParserTreeResult<Void> result = manager.parse("cmd2 m_arg1 m_arg2 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertEquals(0, result.getErrors().size());
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(0, c1.m1_count);
    assertEquals(0, c2.d_count);
    assertEquals(0, c2.e_count);
    assertEquals(1, c2.m1_count);
  }

  @Test
  public void unregisterCommand_1() {
    ClassWithMultiCommand c1 = new ClassWithMultiCommand();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("cmd1 m_arg1 m_arg2 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(1, c1.m1_count);
    assertEquals(0, c1.m2_count);

    manager.unregisterCommand(c1);
    result = manager.parse("cmd1 m_arg1 m_arg2 m_arg3", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof UnknownCommandError));
    assertEquals(0, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(1, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(1, c1.m1_count); // From the previous run
    assertEquals(0, c1.m2_count);
  }

  @Test
  public void unregisterCommand_2() {
    ClassWithMultiCommand c1 = new ClassWithMultiCommand();
    TestBaseStandaloneCommandManager manager = new TestBaseStandaloneCommandManager();
    manager.registerCommand(c1);
    ParserTreeResult<Void> result = manager.parse("cmd1 m_arg1 m_arg2 m_arg3", null);

    assertNotNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof InvalidOptionError));
    assertEquals(1, result.getCompletions().size());
    assertEquals(
        1,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(0, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(1, c1.m1_count);
    assertEquals(0, c1.m2_count);

    manager.unregisterCommand(ClassWithMultiCommand.class);
    result = manager.parse("cmd1 m_arg1 m_arg2 m_arg3", null);

    assertNull(result.getExecuteCandidate());
    assertNotNull(result.getErrorCandidate());
    assertNull(result.getCompleteCandidate());
    assertTrue(result.getErrors().stream().anyMatch(e -> e instanceof UnknownCommandError));
    assertEquals(0, result.getCompletions().size());
    assertEquals(
        0,
        result.getCompletions().stream()
            .mapToLong(g -> g.getMatchingCompletionCandidates().size())
            .sum());

    result.execute();
    assertEquals(1, manager.e_count);
    assertEquals(0, c1.d_count);
    assertEquals(0, c1.e_count);
    assertEquals(1, c1.m1_count); // From the previous run
    assertEquals(0, c1.m2_count);
  }

  static class TestBaseStandaloneCommandManager extends BaseStandaloneCommandManager<Void> {
    int e_count = 0;

    @Override
    protected ParserTreeHandler<ErrorContext<Void>> getBaseErrorHandler() {
      return context -> e_count++;
    }
  }

  static class ClassNoCommand extends AnnotationCommand<Void> {}

  @Command("")
  static class ClassWithEmptyCommand extends AnnotationCommand<Void> {}

  @Command("cmd1")
  static class ClassWithSingleCommand extends AnnotationCommand<Void> {
    int d_count = 0;
    int e_count = 0;
    int m1_count = 0;

    @Default
    public void d() {
      d_count++;
    }

    @Error
    public void e(ErrorContext<Void> ctx) {
      e_count++;
    }

    @Arg("m_arg1 m_arg2 m_arg3")
    public void m1() {
      m1_count++;
    }
  }

  @Command("cmd1")
  static class ClassWithSingleCommand2 extends AnnotationCommand<Void> {
    int d_count = 0;
    int e_count = 0;
    int m1_count = 0;

    @Default
    public void d() {
      d_count++;
    }

    @Error
    public void e(ErrorContext<Void> ctx) {
      e_count++;
    }

    @Arg("m_arg1 m_arg2 m_arg3")
    public void m1() {
      m1_count++;
    }
  }

  @Command("cmd1 c_arg1 c_arg2 c_arg3")
  static class ParentCommand extends AnnotationCommand<Void> {
    int d_count = 0;
    int e_count = 0;
    int m1_count = 0;

    @Default
    public void d() {
      d_count++;
    }

    @Error
    public void e(ErrorContext<Void> ctx) {
      e_count++;
    }

    @Arg("m_arg1 m_arg2 m_arg3")
    public void m1() {
      m1_count++;
    }
  }

  @Command(value = "cmd2 c_arg1 c_arg2 c_arg3", input = "c_arg1 c_arg2 c_arg3 child")
  @Arg("child")
  static class ChildCommand extends AnnotationCommand<Void> {
    int d_count = 0;
    int e_count = 0;
    int m1_count = 0;

    @Default
    public void d() {
      d_count++;
    }

    @Error
    public void e(ErrorContext<Void> ctx) {
      e_count++;
    }

    @Arg("m_arg1 m_arg2 m_arg3")
    public void m1() {
      m1_count++;
    }
  }

  @Command(value = "cmd1", input = "m_arg1")
  static class ClassWithSingleCommandSingleInput extends AnnotationCommand<Void> {
    int d_count = 0;
    int e_count = 0;
    int m1_count = 0;

    @Default
    public void d() {
      d_count++;
    }

    @Error
    public void e(ErrorContext<Void> ctx) {
      e_count++;
    }

    @Arg("m_arg1 m_arg2 m_arg3")
    public void m1() {
      m1_count++;
    }
  }

  @Command(value = "cmd1", input = "m_arg1 m_arg2 m_arg3")
  static class ClassWithSingleCommandMultiInput extends AnnotationCommand<Void> {
    int d_count = 0;
    int e_count = 0;
    int m1_count = 0;

    @Default
    public void d() {
      d_count++;
    }

    @Error
    public void e(ErrorContext<Void> ctx) {
      e_count++;
    }

    @Arg("m_arg1 m_arg2 m_arg3")
    public void m1() {
      m1_count++;
    }
  }

  @Command("cmd1 c_arg1")
  static class ClassWithSingleCommandSingleParam extends AnnotationCommand<Void> {
    int d_count = 0;
    int e_count = 0;
    int m1_count = 0;

    @Default
    public void d() {
      d_count++;
    }

    @Error
    public void e(ErrorContext<Void> ctx) {
      e_count++;
    }

    @Arg("m_arg1 m_arg2 m_arg3")
    public void m1() {
      m1_count++;
    }
  }

  @Command(value = "cmd1 c_arg1", input = "c_arg1")
  static class ClassWithSingleCommandSingleParamSingleInput extends AnnotationCommand<Void> {
    int d_count = 0;
    int e_count = 0;
    int m1_count = 0;

    @Default
    public void d() {
      d_count++;
    }

    @Error
    public void e(ErrorContext<Void> ctx) {
      e_count++;
    }

    @Arg("m_arg1 m_arg2 m_arg3")
    public void m1() {
      m1_count++;
    }
  }

  @Command(value = "cmd1 c_arg1", input = "c_arg1 m_arg1 m_arg2")
  static class ClassWithSingleCommandSingleParamMultipleInput extends AnnotationCommand<Void> {
    int d_count = 0;
    int e_count = 0;
    int m1_count = 0;

    @Default
    public void d() {
      d_count++;
    }

    @Error
    public void e(ErrorContext<Void> ctx) {
      e_count++;
    }

    @Arg("m_arg1 m_arg2 m_arg3")
    public void m1() {
      m1_count++;
    }
  }

  @Command("cmd1 c_arg1 c_arg2 c_arg3")
  static class ClassWithSingleCommandMultiParam extends AnnotationCommand<Void> {
    int d_count = 0;
    int e_count = 0;
    int m1_count = 0;

    @Default
    public void d() {
      d_count++;
    }

    @Error
    public void e(ErrorContext<Void> ctx) {
      e_count++;
    }

    @Arg("m_arg1 m_arg2 m_arg3")
    public void m1() {
      m1_count++;
    }
  }

  @Command(value = "cmd1 c_arg1 c_arg2 c_arg3", input = "c_arg1")
  static class ClassWithSingleCommandMultiParamSingleInput extends AnnotationCommand<Void> {
    int d_count = 0;
    int e_count = 0;
    int m1_count = 0;

    @Default
    public void d() {
      d_count++;
    }

    @Error
    public void e(ErrorContext<Void> ctx) {
      e_count++;
    }

    @Arg("m_arg1 m_arg2 m_arg3")
    public void m1() {
      m1_count++;
    }
  }

  @Command(value = "cmd1 c_arg1 c_arg2 c_arg3", input = "c_arg1 c_arg2 c_arg3 m_arg1 m_arg2")
  static class ClassWithSingleCommandMultiParamMultiInput extends AnnotationCommand<Void> {
    int d_count = 0;
    int e_count = 0;
    int m1_count = 0;

    @Default
    public void d() {
      d_count++;
    }

    @Error
    public void e(ErrorContext<Void> ctx) {
      e_count++;
    }

    @Arg("m_arg1 m_arg2 m_arg3")
    public void m1() {
      m1_count++;
    }
  }

  @Command("cmd1|cmd2|cmd3")
  static class ClassWithMultiCommand extends AnnotationCommand<Void> {
    int d_count = 0;
    int e_count = 0;
    int m1_count = 0;
    int m2_count = 0;

    @Default
    public void d() {
      d_count++;
    }

    @Error
    public void e(ErrorContext<Void> ctx) {
      e_count++;
    }

    @Arg("m_arg1 m_arg2 m_arg3")
    public void m1() {
      m1_count++;
    }

    @Arg("m_arg2|m_arg1 m_arg3")
    public void m2() {
      m2_count++;
    }
  }

  @Command("cmd1|cmd2|cmd3 c_arg1")
  static class ClassWithMultiCommandSingleParam extends AnnotationCommand<Void> {
    int d_count = 0;
    int e_count = 0;
    int m1_count = 0;
    int m2_count = 0;

    @Default
    public void d() {
      d_count++;
    }

    @Error
    public void e(ErrorContext<Void> ctx) {
      e_count++;
    }

    @Arg("m_arg1 m_arg2 m_arg3")
    public void m1() {
      m1_count++;
    }

    @Arg("m_arg2|m_arg1 m_arg3")
    public void m2() {
      m2_count++;
    }
  }

  @Command(value = "cmd1|cmd2|cmd3 c_arg1", input = "c_arg1")
  static class ClassWithMultiCommandSingleParamSingleInput extends AnnotationCommand<Void> {
    @Default
    public void d() {}

    @Error
    public void e() {}

    @Arg("m_arg1 m_arg2 m_arg3")
    public void m1() {}

    @Arg("m_arg2|m_arg1 m_arg3")
    public void m2() {}
  }

  @Command(value = "cmd1|cmd2|cmd3 c_arg1", input = "c_arg1 m_arg1")
  static class ClassWithMultiCommandSingleParamMultiInput extends AnnotationCommand<Void> {
    @Default
    public void d() {}

    @Error
    public void e() {}

    @Arg("m_arg1 m_arg2 m_arg3")
    public void m1() {}

    @Arg("m_arg2|m_arg1 m_arg3")
    public void m2() {}
  }

  @Command("cmd1|cmd2|cmd3 c_arg1 c_arg2 c_arg3")
  static class ClassWithMultiCommandMultiParam extends AnnotationCommand<Void> {
    int d_count = 0;
    int e_count = 0;
    int m1_count = 0;

    @Default
    public void d() {
      d_count++;
    }

    @Error
    public void e(ErrorContext<Void> ctx) {
      e_count++;
    }

    @Arg("m_arg1 m_arg2 m_arg3")
    public void m1() {
      m1_count++;
    }
  }

  @Command("cmd1|cmd4|cmd5")
  static class ClassWithOverlappingName extends AnnotationCommand<Void> {
    int d_count = 0;
    int e_count = 0;
    int m1_count = 0;

    @Default
    public void d() {
      d_count++;
    }

    @Error
    public void e(ErrorContext<Void> ctx) {
      e_count++;
    }

    @Arg("m_arg1 m_arg2 m_arg3")
    public void m1() {
      m1_count++;
    }
  }

  @Command("cmd4|cmd2|cmd5")
  static class ClassWithOverlappingAlias extends AnnotationCommand<Void> {
    int d_count = 0;
    int e_count = 0;
    int m1_count = 0;

    @Default
    public void d() {
      d_count++;
    }

    @Error
    public void e(ErrorContext<Void> ctx) {
      e_count++;
    }

    @Arg("m_arg1 m_arg2 m_arg3")
    public void m1() {
      m1_count++;
    }
  }
}
