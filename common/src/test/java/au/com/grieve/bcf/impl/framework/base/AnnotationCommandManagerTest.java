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

package au.com.grieve.bcf.impl.framework.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.com.grieve.bcf.CompletionCandidateGroup;
import au.com.grieve.bcf.ExecutionCandidate;
import au.com.grieve.bcf.framework.annotation.annotations.Arg;
import au.com.grieve.bcf.framework.annotation.annotations.Command;
import au.com.grieve.bcf.framework.annotation.annotations.Default;
import au.com.grieve.bcf.framework.annotation.annotations.Error;
import au.com.grieve.bcf.impl.framework.annotation.AnnotationCommand;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class AnnotationCommandManagerTest {

  @Nested
  class AnnotationCommandTests {
    @Test
    public void registerCommandNoCommand() {
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      assertThrows(RuntimeException.class, () -> manager.registerCommand(new ClassNoCommand()));
    }

    @Test
    public void registerCommandEmptyCommand() {
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      assertThrows(
          RuntimeException.class, () -> manager.registerCommand(new ClassWithEmptyCommand()));
    }

    @Test
    public void executeCommandSingle_1() {
      AnnotationCommand<Object> c1 = new ClassWithSingleCommand();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("", null);

      assertNull(e);
    }

    @Test
    public void executeCommandSingle_2() {
      AnnotationCommand<Object> c1 = new ClassWithSingleCommand();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("bob", null);

      assertNull(e);
    }

    @Test
    public void executeCommandSingle_3() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithSingleCommand();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("cmd1", null);

      assertEquals(c1.getClass().getMethod("d"), e.getMethod());
    }

    @Test
    public void executeCommandSingle_4() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithSingleCommand();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("cmd1 bob", null);

      assertEquals(c1.getClass().getMethod("d"), e.getMethod());
    }

    @Test
    public void executeCommandSingle_5() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithSingleCommand();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("cmd1 m_arg1 m_arg2", null);

      assertEquals(c1.getClass().getMethod("e"), e.getMethod());
    }

    @Test
    public void executeCommandSingle_6() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithSingleCommand();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("cmd1 m_arg1 m_arg2 m_arg3", null);

      assertEquals(c1.getClass().getMethod("m1"), e.getMethod());
    }

    @Test
    public void executeCommandSingle_7() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithSingleCommandSingleInput();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("cmd1 m_arg1 m_arg2 m_arg3", null);

      assertEquals(c1.getClass().getMethod("e"), e.getMethod());
    }

    @Test
    public void executeCommandSingle_8() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithSingleCommandSingleInput();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("cmd1 m_arg2 m_arg3", null);

      assertEquals(c1.getClass().getMethod("m1"), e.getMethod());
    }

    @Test
    public void executeCommandSingle_9() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithSingleCommandMultiInput();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("cmd1", null);

      assertEquals(c1.getClass().getMethod("m1"), e.getMethod());
    }

    @Test
    public void executeCommandSingleParamSingle_1() {
      AnnotationCommand<Object> c1 = new ClassWithSingleCommandSingleParam();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("", null);

      assertNull(e);
    }

    @Test
    public void executeCommandSingleParamSingle_2() {
      AnnotationCommand<Object> c1 = new ClassWithSingleCommandSingleParam();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("bob", null);

      assertNull(e);
    }

    @Test
    public void executeCommandSingleParamSingle_3() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithSingleCommandSingleParam();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("cmd1", null);

      assertEquals(c1.getClass().getMethod("e"), e.getMethod());
    }

    @Test
    public void executeCommandSingleParamSingle_4() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithSingleCommandSingleParam();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("cmd1 bob", null);

      assertEquals(c1.getClass().getMethod("e"), e.getMethod());
    }

    @Test
    public void executeCommandSingleParamSingle_5() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithSingleCommandSingleParam();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("cmd1 m_arg1 m_arg2", null);

      assertEquals(c1.getClass().getMethod("e"), e.getMethod());
    }

    @Test
    public void executeCommandSingleParamSingle_6() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithSingleCommandSingleParam();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("cmd1 m_arg1 m_arg2 m_arg3", null);

      assertEquals(c1.getClass().getMethod("e"), e.getMethod());
    }

    @Test
    public void executeCommandSingleParamSingle_7() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithSingleCommandSingleParam();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("cmd1 m_arg2 m_arg3", null);

      assertEquals(c1.getClass().getMethod("e"), e.getMethod());
    }

    @Test
    public void executeCommandSingleParamSingle_8() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithSingleCommandSingleParam();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("cmd1 c_arg1 m_arg1 m_arg2 m_arg3", null);

      assertEquals(c1.getClass().getMethod("m1"), e.getMethod());
    }

    @Test
    public void executeCommandSingleParamSingle_9() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithSingleCommandSingleParamSingleInput();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("cmd1 m_arg1 m_arg2 m_arg3", null);

      assertEquals(c1.getClass().getMethod("m1"), e.getMethod());
    }

    @Test
    public void executeCommandSingleParamSingle_10() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithSingleCommandSingleParamMultipleInput();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("cmd1 m_arg3", null);

      assertEquals(c1.getClass().getMethod("m1"), e.getMethod());
    }

    @Test
    public void executeCommandSingleParamMulti_1() {
      AnnotationCommand<Object> c1 = new ClassWithSingleCommandMultiParam();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("", null);

      assertNull(e);
    }

    @Test
    public void executeCommandSingleParamMulti_2() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithSingleCommandMultiParam();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e =
          manager.execute("cmd1 c_arg1 c_arg2 c_arg3 m_arg1 m_arg2 m_arg3", null);

      assertEquals(c1.getClass().getMethod("m1"), e.getMethod());
    }

    @Test
    public void executeCommandSingleParamMulti_3() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithSingleCommandMultiParamSingleInput();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("cmd1 c_arg2 c_arg3 m_arg1 m_arg2 m_arg3", null);

      assertEquals(c1.getClass().getMethod("m1"), e.getMethod());
    }

    @Test
    public void executeCommandSingleParamMulti_4() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithSingleCommandMultiParamMultiInput();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("cmd1 m_arg3", null);

      assertEquals(c1.getClass().getMethod("m1"), e.getMethod());
    }

    @Test
    public void executeCommandMulti_1() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithMultiCommand();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("cmd1 m_arg1 m_arg2 m_arg3", null);

      assertEquals(c1.getClass().getMethod("m1"), e.getMethod());
    }

    @Test
    public void executeCommandMulti_2() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithMultiCommand();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("cmd2 m_arg1 m_arg2 m_arg3", null);

      assertEquals(c1.getClass().getMethod("m1"), e.getMethod());
    }

    @Test
    public void executeCommandMulti_3() {
      AnnotationCommand<Object> c1 = new ClassWithMultiCommand();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("bob m_arg1 m_arg2 m_arg3", null);

      assertNull(e);
    }

    @Test
    public void executeCommandMultiParamSingle_1() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithMultiCommandSingleParam();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("cmd1 c_arg1 m_arg1 m_arg2 m_arg3", null);

      assertEquals(c1.getClass().getMethod("m1"), e.getMethod());
    }

    @Test
    public void executeCommandMultiParamSingle_2() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithMultiCommandSingleParam();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("cmd2 c_arg1 m_arg1 m_arg2 m_arg3", null);

      assertEquals(c1.getClass().getMethod("m1"), e.getMethod());
    }

    @Test
    public void executeCommandMultiParamSingle_3() {
      AnnotationCommand<Object> c1 = new ClassWithMultiCommandSingleParam();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e = manager.execute("bpb c_arg1 m_arg1 m_arg2 m_arg3", null);

      assertNull(e);
    }

    @Test
    public void executeCommandMultiParamMulti_1() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithMultiCommandMultiParam();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e =
          manager.execute("cmd1 c_arg1 c_arg2 c_arg3 m_arg1 m_arg2 m_arg3", null);

      assertEquals(c1.getClass().getMethod("m1"), e.getMethod());
    }

    @Test
    public void executeCommandMultiParamMulti_2() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithMultiCommandMultiParam();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      ExecutionCandidate e =
          manager.execute("cmd2 c_arg1 c_arg2 c_arg3 m_arg1 m_arg2 m_arg3", null);

      assertEquals(c1.getClass().getMethod("m1"), e.getMethod());
    }

    @Test
    public void registerOverlap_1() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithSingleCommand();
      AnnotationCommand<Object> c2 = new ClassWithSingleCommand2();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      manager.registerCommand(c2);

      ExecutionCandidate e = manager.execute("cmd1 m_arg1 m_arg2 m_arg3", null);

      assertEquals(c1.getClass().getMethod("m1"), e.getMethod());
      assertNotEquals(c2.getClass().getMethod("m1"), e.getMethod());
    }

    @Test
    public void registerOverlap_2() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithOverlappingName();
      AnnotationCommand<Object> c2 = new ClassWithOverlappingAlias();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      manager.registerCommand(c2);

      ExecutionCandidate e = manager.execute("cmd1 m_arg1 m_arg2 m_arg3", null);

      assertEquals(c1.getClass().getMethod("m1"), e.getMethod());
      assertNotEquals(c2.getClass().getMethod("m1"), e.getMethod());
    }

    @Test
    public void registerOverlap_3() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithOverlappingName();
      AnnotationCommand<Object> c2 = new ClassWithOverlappingAlias();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      manager.registerCommand(c2);

      ExecutionCandidate e = manager.execute("cmd5 m_arg1 m_arg2 m_arg3", null);

      assertEquals(c1.getClass().getMethod("m1"), e.getMethod());
      assertNotEquals(c2.getClass().getMethod("m1"), e.getMethod());
    }

    @Test
    public void registerOverlap_4() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ClassWithOverlappingName();
      AnnotationCommand<Object> c2 = new ClassWithOverlappingAlias();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      manager.registerCommand(c2);

      ExecutionCandidate e = manager.execute("cmd4 m_arg1 m_arg2 m_arg3", null);

      assertEquals(c2.getClass().getMethod("m1"), e.getMethod());
      assertNotEquals(c1.getClass().getMethod("m1"), e.getMethod());
    }

    @Test
    public void childCommand_1() throws NoSuchMethodException {
      au.com.grieve.bcf.Command<Object> c1 = new ParentCommand();
      AnnotationCommand<Object> c2 = new ChildCommand();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      manager.registerCommand(ParentCommand.class, c2);

      ExecutionCandidate e =
          manager.execute("cmd1 c_arg1 c_arg2 c_arg3 m_arg1 m_arg2 m_arg3", null);

      assertEquals(c1.getClass().getMethod("m1"), e.getMethod());
      assertNotEquals(c2.getClass().getMethod("m1"), e.getMethod());
    }

    @Test
    public void childCommand_2() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ParentCommand();
      AnnotationCommand<Object> c2 = new ChildCommand();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      manager.registerCommand(ParentCommand.class, c2);

      ExecutionCandidate e =
          manager.execute("cmd1 c_arg1 c_arg2 c_arg3 child m_arg1 m_arg2 m_arg3", null);

      assertEquals(c2.getClass().getMethod("m1"), e.getMethod());
      assertNotEquals(c1.getClass().getMethod("m1"), e.getMethod());
    }

    @Test
    public void childCommand_3() throws NoSuchMethodException {
      AnnotationCommand<Object> c1 = new ParentCommand();
      AnnotationCommand<Object> c2 = new ChildCommand();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      manager.registerCommand(ParentCommand.class, c2);

      ExecutionCandidate e = manager.execute("cmd2 m_arg1 m_arg2 m_arg3", null);

      assertEquals(c2.getClass().getMethod("m1"), e.getMethod());
      assertNotEquals(c1.getClass().getMethod("m1"), e.getMethod());
    }

    @Test
    public void completeSingleMultiCommand_1() {
      AnnotationCommand<Object> c1 = new ClassWithMultiCommand();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      List<CompletionCandidateGroup> groups = new ArrayList<>();

      manager.complete("", groups);

      assertEquals(1, groups.size());
      assertEquals(3, groups.get(0).getCompletionCandidates().size());
    }

    @Test
    public void completeSingleMultiCommand_2() {
      AnnotationCommand<Object> c1 = new ClassWithMultiCommand();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      List<CompletionCandidateGroup> groups = new ArrayList<>();

      manager.complete(" ", groups);

      assertEquals(1, groups.size());
      assertEquals(3, groups.get(0).getCompletionCandidates().size());
    }

    @Test
    public void completeSingleMultiCommand_3() {
      AnnotationCommand<Object> c1 = new ClassWithMultiCommand();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      List<CompletionCandidateGroup> groups = new ArrayList<>();

      manager.complete("c", groups);

      assertEquals(1, groups.size());
      assertEquals(3, groups.get(0).getCompletionCandidates().size());
    }

    @Test
    public void completeSingleMultiCommand_4() {
      AnnotationCommand<Object> c1 = new ClassWithMultiCommand();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      List<CompletionCandidateGroup> groups = new ArrayList<>();

      manager.complete("d", groups);

      assertEquals(0, groups.size());
    }

    @Test
    public void completeSingleMultiCommand_5() {
      AnnotationCommand<Object> c1 = new ClassWithMultiCommand();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      List<CompletionCandidateGroup> groups = new ArrayList<>();

      manager.complete("cmd2", groups);

      assertEquals(1, groups.size());
      assertEquals(1, groups.get(0).getCompletionCandidates().size());
    }

    @Test
    public void completeSingleMultiCommand_6() {
      AnnotationCommand<Object> c1 = new ClassWithMultiCommand();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      List<CompletionCandidateGroup> groups = new ArrayList<>();

      manager.complete("cmd2 ", groups);

      assertEquals(2, groups.size());
      assertTrue(
          groups.stream()
              .map(g -> g.getCompletionCandidates().size() == 1)
              .filter(b -> b)
              .findFirst()
              .orElse(false));
      assertTrue(
          groups.stream()
              .map(g -> g.getCompletionCandidates().size() == 2)
              .filter(b -> b)
              .findFirst()
              .orElse(false));
    }

    @Test
    public void completeSingleMultiCommand_7() {
      AnnotationCommand<Object> c1 = new ClassWithMultiCommand();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      List<CompletionCandidateGroup> groups = new ArrayList<>();

      manager.complete("cmd2 m_arg2", groups);

      assertEquals(2, groups.size());
    }

    @Test
    public void completeSingleMultiCommand_8() {
      AnnotationCommand<Object> c1 = new ClassWithMultiCommand();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      List<CompletionCandidateGroup> groups = new ArrayList<>();

      manager.complete("cmd2 m_arg1", groups);

      assertEquals(2, groups.size());
    }

    @Test
    public void completeSingleMultiCommandSingleParam_1() {
      AnnotationCommand<Object> c1 = new ClassWithMultiCommandSingleParam();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      List<CompletionCandidateGroup> groups = new ArrayList<>();

      manager.complete("cmd2 m_arg1", groups);

      assertEquals(1, groups.size());
      assertEquals("c_arg1", groups.get(0).getCompletionCandidates().get(0).getValue());
    }

    @Test
    public void completeSingleMultiCommandSingleParam_2() {
      AnnotationCommand<Object> c1 = new ClassWithMultiCommandSingleParam();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      List<CompletionCandidateGroup> groups = new ArrayList<>();

      manager.complete("cmd2 ", groups);

      assertEquals(1, groups.size());
      assertEquals("c_arg1", groups.get(0).getCompletionCandidates().get(0).getValue());
    }

    @Test
    public void completeSingleMultiCommandSingleParam_3() {
      AnnotationCommand<Object> c1 = new ClassWithMultiCommandSingleParam();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      List<CompletionCandidateGroup> groups = new ArrayList<>();

      manager.complete("cmd2 c_arg1 ", groups);

      assertEquals(2, groups.size());
      assertTrue(
          groups.stream()
              .map(g -> g.getCompletionCandidates().size() == 1)
              .filter(b -> b)
              .findFirst()
              .orElse(false));
      assertTrue(
          groups.stream()
              .map(g -> g.getCompletionCandidates().size() == 2)
              .filter(b -> b)
              .findFirst()
              .orElse(false));
    }

    @Test
    public void completeSingleMultiCommandSingleParamSingleInput_1() {
      AnnotationCommand<Object> c1 = new ClassWithMultiCommandSingleParamSingleInput();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      List<CompletionCandidateGroup> groups = new ArrayList<>();

      manager.complete("cmd2", groups);

      assertEquals(1, groups.size());
    }

    @Test
    public void completeSingleMultiCommandSingleParamSingleInput_2() {
      AnnotationCommand<Object> c1 = new ClassWithMultiCommandSingleParamSingleInput();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      List<CompletionCandidateGroup> groups = new ArrayList<>();

      manager.complete("cmd2 ", groups);

      assertEquals(2, groups.size());
      assertTrue(
          groups.stream()
              .map(g -> g.getCompletionCandidates().size() == 1)
              .filter(b -> b)
              .findFirst()
              .orElse(false));
      assertTrue(
          groups.stream()
              .map(g -> g.getCompletionCandidates().size() == 2)
              .filter(b -> b)
              .findFirst()
              .orElse(false));
    }

    @Test
    public void completeSingleMultiCommandSingleParamMultiInput_1() {
      AnnotationCommand<Object> c1 = new ClassWithMultiCommandSingleParamMultiInput();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      List<CompletionCandidateGroup> groups = new ArrayList<>();

      manager.complete("cmd2 ", groups);

      assertEquals(2, groups.size());
      assertTrue(
          groups.stream()
              .map(g -> g.getCompletionCandidates().get(0).getValue())
              .map(i -> i.equals("m_arg3"))
              .filter(b -> b)
              .findFirst()
              .orElse(false));
      assertTrue(
          groups.stream()
              .map(g -> g.getCompletionCandidates().get(0).getValue())
              .map(i -> i.equals("m_arg2"))
              .filter(b -> b)
              .findFirst()
              .orElse(false));
    }

    @Test
    public void completeMultipleCommands_1() {
      AnnotationCommand<Object> c1 = new ClassWithOverlappingName();
      AnnotationCommand<Object> c2 = new ClassWithOverlappingAlias();
      BaseCommandManager<Object> manager = new BaseCommandManager<>();
      manager.registerCommand(c1);
      manager.registerCommand(c2);
      List<CompletionCandidateGroup> groups = new ArrayList<>();

      manager.complete("", groups);

      assertEquals(2, groups.size());
      assertTrue(groups.stream().allMatch(g -> g.getCompletionCandidates().size() == 2));
    }

    class ClassNoCommand extends AnnotationCommand<Object> {}

    @Command("")
    class ClassWithEmptyCommand extends AnnotationCommand<Object> {}

    @Command("cmd1")
    class ClassWithSingleCommand extends AnnotationCommand<Object> {
      @Default
      public void d() {}

      @Error
      public void e() {}

      @Arg("m_arg1 m_arg2 m_arg3")
      public void m1() {}
    }

    @Command("cmd1")
    class ClassWithSingleCommand2 extends AnnotationCommand<Object> {
      @Default
      public void d() {}

      @Error
      public void e() {}

      @Arg("m_arg1 m_arg2 m_arg3")
      public void m1() {}
    }

    @Command("cmd1 c_arg1 c_arg2 c_arg3")
    class ParentCommand extends AnnotationCommand<Object> {
      @Default
      public void d() {}

      @Error
      public void e() {}

      @Arg("m_arg1 m_arg2 m_arg3")
      public void m1() {}
    }

    @Command(value = "cmd2 c_arg1 c_arg2 c_arg3", input = "c_arg1 c_arg2 c_arg3 child")
    @Arg("child")
    class ChildCommand extends AnnotationCommand<Object> {
      @Default
      public void d() {}

      @Error
      public void e() {}

      @Arg("m_arg1 m_arg2 m_arg3")
      public void m1() {}
    }

    @Command(value = "cmd1", input = "m_arg1")
    class ClassWithSingleCommandSingleInput extends AnnotationCommand<Object> {
      @Default
      public void d() {}

      @Error
      public void e() {}

      @Arg("m_arg1 m_arg2 m_arg3")
      public void m1() {}
    }

    @Command(value = "cmd1", input = "m_arg1 m_arg2 m_arg3")
    class ClassWithSingleCommandMultiInput extends AnnotationCommand<Object> {
      @Default
      public void d() {}

      @Error
      public void e() {}

      @Arg("m_arg1 m_arg2 m_arg3")
      public void m1() {}
    }

    @Command("cmd1 c_arg1")
    class ClassWithSingleCommandSingleParam extends AnnotationCommand<Object> {
      @Default
      public void d() {}

      @Error
      public void e() {}

      @Arg("m_arg1 m_arg2 m_arg3")
      public void m1() {}
    }

    @Command(value = "cmd1 c_arg1", input = "c_arg1")
    class ClassWithSingleCommandSingleParamSingleInput extends AnnotationCommand<Object> {
      @Default
      public void d() {}

      @Error
      public void e() {}

      @Arg("m_arg1 m_arg2 m_arg3")
      public void m1() {}
    }

    @Command(value = "cmd1 c_arg1", input = "c_arg1 m_arg1 m_arg2")
    class ClassWithSingleCommandSingleParamMultipleInput extends AnnotationCommand<Object> {
      @Default
      public void d() {}

      @Error
      public void e() {}

      @Arg("m_arg1 m_arg2 m_arg3")
      public void m1() {}
    }

    @Command("cmd1 c_arg1 c_arg2 c_arg3")
    class ClassWithSingleCommandMultiParam extends AnnotationCommand<Object> {
      @Default
      public void d() {}

      @Error
      public void e() {}

      @Arg("m_arg1 m_arg2 m_arg3")
      public void m1() {}
    }

    @Command(value = "cmd1 c_arg1 c_arg2 c_arg3", input = "c_arg1")
    class ClassWithSingleCommandMultiParamSingleInput extends AnnotationCommand<Object> {
      @Default
      public void d() {}

      @Error
      public void e() {}

      @Arg("m_arg1 m_arg2 m_arg3")
      public void m1() {}
    }

    @Command(value = "cmd1 c_arg1 c_arg2 c_arg3", input = "c_arg1 c_arg2 c_arg3 m_arg1 m_arg2")
    class ClassWithSingleCommandMultiParamMultiInput extends AnnotationCommand<Object> {
      @Default
      public void d() {}

      @Error
      public void e() {}

      @Arg("m_arg1 m_arg2 m_arg3")
      public void m1() {}
    }

    @Command("cmd1|cmd2|cmd3")
    class ClassWithMultiCommand extends AnnotationCommand<Object> {
      @Default
      public void d() {}

      @Error
      public void e() {}

      @Arg("m_arg1 m_arg2 m_arg3")
      public void m1() {}

      @Arg("m_arg2|m_arg1 m_arg3")
      public void m2() {}
    }

    @Command("cmd1|cmd2|cmd3 c_arg1")
    class ClassWithMultiCommandSingleParam extends AnnotationCommand<Object> {
      @Default
      public void d() {}

      @Error
      public void e() {}

      @Arg("m_arg1 m_arg2 m_arg3")
      public void m1() {}

      @Arg("m_arg2|m_arg1 m_arg3")
      public void m2() {}
    }

    @Command(value = "cmd1|cmd2|cmd3 c_arg1", input = "c_arg1")
    class ClassWithMultiCommandSingleParamSingleInput extends AnnotationCommand<Object> {
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
    class ClassWithMultiCommandSingleParamMultiInput extends AnnotationCommand<Object> {
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
    class ClassWithMultiCommandMultiParam extends AnnotationCommand<Object> {
      @Default
      public void d() {}

      @Error
      public void e() {}

      @Arg("m_arg1 m_arg2 m_arg3")
      public void m1() {}
    }

    @Command("cmd1|cmd4|cmd5")
    class ClassWithOverlappingName extends AnnotationCommand<Object> {
      @Default
      public void d() {}

      @Error
      public void e() {}

      @Arg("m_arg1 m_arg2 m_arg3")
      public void m1() {}
    }

    @Command("cmd4|cmd2|cmd5")
    class ClassWithOverlappingAlias extends AnnotationCommand<Object> {
      @Default
      public void d() {}

      @Error
      public void e() {}

      @Arg("m_arg1 m_arg2 m_arg3")
      public void m1() {}
    }
  }
}
