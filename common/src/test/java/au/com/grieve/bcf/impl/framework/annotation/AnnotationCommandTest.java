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

package au.com.grieve.bcf.impl.framework.annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.com.grieve.bcf.CompletionCandidateGroup;
import au.com.grieve.bcf.CompletionContext;
import au.com.grieve.bcf.ExecutionCandidate;
import au.com.grieve.bcf.ExecutionContext;
import au.com.grieve.bcf.framework.annotation.annotations.Arg;
import au.com.grieve.bcf.framework.annotation.annotations.Command;
import au.com.grieve.bcf.framework.annotation.annotations.Default;
import au.com.grieve.bcf.framework.annotation.annotations.Error;
import au.com.grieve.bcf.impl.framework.base.BaseCompletionContext;
import au.com.grieve.bcf.impl.framework.base.BaseExecutionContext;
import au.com.grieve.bcf.impl.line.DefaultParsedLine;
import au.com.grieve.bcf.impl.parser.IntegerParser;
import au.com.grieve.bcf.impl.parser.StringParser;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class AnnotationCommandTest {

  ExecutionContext defaultExecutionContext(String line) {
    ExecutionContext result = new BaseExecutionContext(new DefaultParsedLine(line));
    result.getParserClasses().put("literal", StringParser.class);
    result.getParserClasses().put("string", StringParser.class);
    result.getParserClasses().put("int", IntegerParser.class);
    return result;
  }

  CompletionContext defaultCompletionContext(String line) {
    CompletionContext result = new BaseCompletionContext(new DefaultParsedLine(line));
    result.getParserClasses().put("literal", StringParser.class);
    result.getParserClasses().put("string", StringParser.class);
    result.getParserClasses().put("int", IntegerParser.class);
    return result;
  }

  @Test
  void noDefaultDefined() {
    C1 c1 = new C1();
    ExecutionContext ctx = defaultExecutionContext("bob");
    assertNull(c1.execute(ctx));
  }

  @Test
  void noArgMatchOnClass_1() throws NoSuchMethodException {
    C2 c2 = new C2();
    ExecutionContext ctx = defaultExecutionContext("");
    ExecutionCandidate e = c2.execute(ctx);

    // Should be error method with a weight of 0
    assertEquals(c2.getClass().getMethod("d"), e.getMethod());
    assertEquals(0, e.getWeight());
  }

  @Test
  void noArgMatchOnClass_2() throws NoSuchMethodException {
    C2 c2 = new C2();
    ExecutionContext ctx = defaultExecutionContext("bob");
    ExecutionCandidate e = c2.execute(ctx);

    // Should be error method with a weight of 0
    assertEquals(c2.getClass().getMethod("e"), e.getMethod());
    assertEquals(1, e.getWeight());
  }

  @Test
  void someArgMatchOnClass_1() throws NoSuchMethodException {
    C2 c2 = new C2();
    ExecutionContext ctx = defaultExecutionContext("c_arg1 c_arg2");
    ExecutionCandidate e = c2.execute(ctx);

    // Should be error method with a weight of 2
    assertEquals(c2.getClass().getMethod("e"), e.getMethod());
    assertEquals(2, e.getWeight());
  }

  @Test
  void someArgMatchOnClass_2() throws NoSuchMethodException {
    C2 c2 = new C2();
    ExecutionContext ctx = defaultExecutionContext("c_arg1 c_arg2 bob");
    ExecutionCandidate e = c2.execute(ctx);

    // Should be error method with a weight of 2
    assertEquals(c2.getClass().getMethod("e"), e.getMethod());
    assertEquals(3, e.getWeight());
  }

  @Test
  void argMatchClassNotMethod_1() throws NoSuchMethodException {
    C2 c2 = new C2();
    ExecutionContext ctx = defaultExecutionContext("c_arg1 c_arg2 c_arg3");
    ExecutionCandidate e = c2.execute(ctx);

    // Should be default method with a weight of 3
    assertEquals(c2.getClass().getMethod("d"), e.getMethod());
    assertEquals(3, e.getWeight());
  }

  @Test
  void argMatchClassNotMethod_2() throws NoSuchMethodException {
    C2 c2 = new C2();
    ExecutionContext ctx = defaultExecutionContext("c_arg1 c_arg2 c_arg3 bob");
    ExecutionCandidate e = c2.execute(ctx);

    // Should be default method with a weight of 3
    assertEquals(c2.getClass().getMethod("e"), e.getMethod());
    assertEquals(4, e.getWeight());
  }

  @Test
  void argMatchClassSomeMethod_1() throws NoSuchMethodException {
    C2 c2 = new C2();
    ExecutionContext ctx = defaultExecutionContext("c_arg1 c_arg2 c_arg3 m_arg1 m_arg2");
    ExecutionCandidate e = c2.execute(ctx);

    // Should be error method with a weight of 5
    assertEquals(c2.getClass().getMethod("e"), e.getMethod());
    assertEquals(5, e.getWeight());
  }

  @Test
  void argMatchClassSomeMethod_2() throws NoSuchMethodException {
    C2 c2 = new C2();
    ExecutionContext ctx = defaultExecutionContext("c_arg1 c_arg2 c_arg3 m_arg1 m_arg2 bob");
    ExecutionCandidate e = c2.execute(ctx);

    // Should be error method with a weight of 5
    assertEquals(c2.getClass().getMethod("e"), e.getMethod());
    assertEquals(6, e.getWeight());
  }

  @Test
  void argMatchClassAndMethod_1() throws NoSuchMethodException {
    C2 c2 = new C2();
    ExecutionContext ctx = defaultExecutionContext("c_arg1 c_arg2 c_arg3 m_arg1 m_arg2 m_arg3");
    ExecutionCandidate e = c2.execute(ctx);

    // Success
    assertEquals(c2.getClass().getMethod("m1"), e.getMethod());
    assertEquals(7, e.getWeight());
  }

  @Test
  void argMatchClassAndMethod_2() throws NoSuchMethodException {
    C2 c2 = new C2();
    ExecutionContext ctx = defaultExecutionContext("c_arg1 c_arg2 c_arg3 m_arg1 m_arg2 m_arg3_m2");
    ExecutionCandidate e = c2.execute(ctx);

    // Success
    assertEquals(c2.getClass().getMethod("m2"), e.getMethod());
    assertEquals(7, e.getWeight());
  }

  @Test
  void noArgMatchOnMethod_1() throws NoSuchMethodException {
    C3 c3 = new C3();
    ExecutionContext ctx = defaultExecutionContext("");
    ExecutionCandidate e = c3.execute(ctx);

    // Should be default method with a weight of 0
    assertEquals(c3.getClass().getMethod("d"), e.getMethod());
    assertEquals(0, e.getWeight());
  }

  @Test
  void noArgMatchOnMethod_2() throws NoSuchMethodException {
    C3 c3 = new C3();
    ExecutionContext ctx = defaultExecutionContext("bob");
    ExecutionCandidate e = c3.execute(ctx);

    // Should be default method with a weight of 0
    assertEquals(c3.getClass().getMethod("e"), e.getMethod());
    assertEquals(1, e.getWeight());
  }

  @Test
  void someMatchOnMethod_1() throws NoSuchMethodException {
    C3 c3 = new C3();
    ExecutionContext ctx = defaultExecutionContext("m_arg1 m_arg2");
    ExecutionCandidate e = c3.execute(ctx);

    // Should be error method with a weight of 2
    assertEquals(c3.getClass().getMethod("e"), e.getMethod());
    assertEquals(2, e.getWeight());
  }

  @Test
  void someMatchOnMethod_2() throws NoSuchMethodException {
    C3 c3 = new C3();
    ExecutionContext ctx = defaultExecutionContext("m_arg1 m_arg2 bob");
    ExecutionCandidate e = c3.execute(ctx);

    // Should be error method with a weight of 2
    assertEquals(c3.getClass().getMethod("e"), e.getMethod());
    assertEquals(3, e.getWeight());
  }

  @Test
  void argMatchMethod_1() throws NoSuchMethodException {
    C3 c3 = new C3();
    ExecutionContext ctx = defaultExecutionContext("m_arg1 m_arg2 m_arg3");
    ExecutionCandidate e = c3.execute(ctx);

    // Success
    assertEquals(c3.getClass().getMethod("m1"), e.getMethod());
    assertEquals(4, e.getWeight());
  }

  @Test
  void argMatchMethod_2() throws NoSuchMethodException {
    C3 c3 = new C3();
    ExecutionContext ctx = defaultExecutionContext("m_arg1 m_arg2 m_arg3_m2");
    ExecutionCandidate e = c3.execute(ctx);

    // Success
    assertEquals(c3.getClass().getMethod("m2"), e.getMethod());
    assertEquals(4, e.getWeight());
  }

  // Child Class Tests
  @Test
  void noChildArgMatch_1() throws NoSuchMethodException {
    C2 c2 = new C2();
    Child1 child1 = new Child1();
    c2.addChild(child1);

    ExecutionContext ctx = defaultExecutionContext("c_arg1 c_arg2 c_arg3");
    ExecutionCandidate e = c2.execute(ctx);

    // Should be default method with a weight of 3 on parent
    assertEquals(c2.getClass().getMethod("d"), e.getMethod());
    assertEquals(3, e.getWeight());
  }

  @Test
  void noChildArgMatch_2() throws NoSuchMethodException {
    C2 c2 = new C2();
    Child1 child1 = new Child1();
    c2.addChild(child1);

    ExecutionContext ctx = defaultExecutionContext("c_arg1 c_arg2 c_arg3 bob");
    ExecutionCandidate e = c2.execute(ctx);

    // Should be default method with a weight of 3 on parent
    assertEquals(c2.getClass().getMethod("e"), e.getMethod());
    assertEquals(4, e.getWeight());
  }

  @Test
  void someChildArgMatch_1() throws NoSuchMethodException {
    C2 c2 = new C2();
    Child1 child1 = new Child1();
    c2.addChild(child1);

    ExecutionContext ctx = defaultExecutionContext("c_arg1 c_arg2 c_arg3 child_arg1 child_arg2");
    ExecutionCandidate e = c2.execute(ctx);

    // Should be error method with a weight of 5 on parent as child has no error
    assertEquals(c2.getClass().getMethod("e"), e.getMethod());
    assertEquals(5, e.getWeight());
  }

  @Test
  void someChildArgMatch_2() throws NoSuchMethodException {
    C2 c2 = new C2();
    Child1 child1 = new Child1();
    c2.addChild(child1);

    ExecutionContext ctx =
        defaultExecutionContext("c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 bob");
    ExecutionCandidate e = c2.execute(ctx);

    // Should be error method with a weight of 5 on parent as child has no error
    assertEquals(c2.getClass().getMethod("e"), e.getMethod());
    assertEquals(6, e.getWeight());
  }

  @Test
  void someChildArgMatch_3() throws NoSuchMethodException {
    C2 c2 = new C2();
    Child2 child2 = new Child2();
    c2.addChild(child2);

    ExecutionContext ctx = defaultExecutionContext("c_arg1 c_arg2 c_arg3 child_arg1 child_arg2");
    ExecutionCandidate e = c2.execute(ctx);

    // Should be error method with a weight of 5 on child
    assertEquals(child2.getClass().getMethod("e"), e.getMethod());
    assertEquals(5, e.getWeight());
  }

  // Test when there is no class args

  @Test
  void someChildArgMatch_4() throws NoSuchMethodException {
    C2 c2 = new C2();
    Child2 child2 = new Child2();
    c2.addChild(child2);

    ExecutionContext ctx =
        defaultExecutionContext("c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 bob");
    ExecutionCandidate e = c2.execute(ctx);

    // Should be error method with a weight of 5 on child
    assertEquals(child2.getClass().getMethod("e"), e.getMethod());
    assertEquals(6, e.getWeight());
  }

  @Test
  void childArgMatch_1() throws NoSuchMethodException {
    C2 c2 = new C2();
    Child1 child1 = new Child1();
    c2.addChild(child1);

    ExecutionContext ctx =
        defaultExecutionContext("c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 child_arg3");
    ExecutionCandidate e = c2.execute(ctx);

    // Should be default method with a weight of 6 on parent as child has no default
    assertEquals(c2.getClass().getMethod("d"), e.getMethod());
    assertEquals(6, e.getWeight());
  }

  @Test
  void childArgMatch_2() throws NoSuchMethodException {
    C2 c2 = new C2();
    Child2 child2 = new Child2();
    c2.addChild(child2);

    ExecutionContext ctx =
        defaultExecutionContext("c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 child_arg3");
    ExecutionCandidate e = c2.execute(ctx);

    // Should be default method with a weight of 6 on child
    assertEquals(child2.getClass().getMethod("d"), e.getMethod());
    assertEquals(6, e.getWeight());
  }

  @Test
  void childArgMatchSomeMethod_1() throws NoSuchMethodException {
    C2 c2 = new C2();
    Child2 child2 = new Child2();
    c2.addChild(child2);

    ExecutionContext ctx =
        defaultExecutionContext(
            "c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 child_arg3 m_arg1 m_arg2");
    ExecutionCandidate e = c2.execute(ctx);

    // Should be error method with a weight of 8 on child
    assertEquals(child2.getClass().getMethod("e"), e.getMethod());
    assertEquals(8, e.getWeight());
  }

  @Test
  void childArgMatchSomeMethod_2() throws NoSuchMethodException {
    C2 c2 = new C2();
    Child2 child2 = new Child2();
    c2.addChild(child2);

    ExecutionContext ctx =
        defaultExecutionContext(
            "c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 child_arg3 m_arg1 m_arg2 bob");
    ExecutionCandidate e = c2.execute(ctx);

    // Should be error method with a weight of 8 on child
    assertEquals(child2.getClass().getMethod("e"), e.getMethod());
    assertEquals(9, e.getWeight());
  }

  @Test
  void childArgMatchMethod_1() throws NoSuchMethodException {
    C2 c2 = new C2();
    Child2 child2 = new Child2();
    c2.addChild(child2);

    ExecutionContext ctx =
        defaultExecutionContext(
            "c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 child_arg3 m_arg1 m_arg2 m_arg3");
    ExecutionCandidate e = c2.execute(ctx);

    // Success
    assertEquals(child2.getClass().getMethod("m1"), e.getMethod());
    assertEquals(10, e.getWeight());
  }

  @Test
  void childArgMatchMethod_2() throws NoSuchMethodException {
    C2 c2 = new C2();
    Child2 child2 = new Child2();
    c2.addChild(child2);

    ExecutionContext ctx =
        defaultExecutionContext(
            "c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 child_arg3 m_arg1 m_arg2 m_arg3 bob");
    ExecutionCandidate e = c2.execute(ctx);

    // Error
    assertEquals(child2.getClass().getMethod("e"), e.getMethod());
    assertEquals(9, e.getWeight());
  }

  @Test
  void childArgMatchMethod_3() throws NoSuchMethodException {
    C2 c2 = new C2();
    Child2 child2 = new Child2();
    c2.addChild(child2);

    ExecutionContext ctx =
        defaultExecutionContext(
            "c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 child_arg3 m_arg1 m_arg2 m_arg3_m2");
    ExecutionCandidate e = c2.execute(ctx);

    // Success
    assertEquals(child2.getClass().getMethod("m2"), e.getMethod());
    assertEquals(10, e.getWeight());
  }

  @Test
  void extendedClass_1() throws NoSuchMethodException {
    C2Extended c2 = new C2Extended();
    ExecutionContext ctx = defaultExecutionContext("bob");
    ExecutionCandidate e = c2.execute(ctx);

    // Should be error method with a weight of 1
    assertEquals(c2.getClass().getMethod("e"), e.getMethod());
    assertEquals(1, e.getWeight());
  }

  @Test
  void extendedClass_2() throws NoSuchMethodException {
    C2Extended c2 = new C2Extended();
    ExecutionContext ctx = defaultExecutionContext("c_arg1 c_arg2 c_arg3");
    ExecutionCandidate e = c2.execute(ctx);

    // Should be default method with a weight of 3
    assertEquals(c2.getClass().getMethod("d"), e.getMethod());
    assertEquals(3, e.getWeight());
  }

  @Test
  void parameters_1() throws NoSuchMethodException {
    ParamClass c = new ParamClass();
    ExecutionContext ctx = defaultExecutionContext("c_arg1 arg1 arg2 23");
    ExecutionCandidate e = c.execute(ctx);

    // Should be m1
    assertEquals(
        c.getClass().getMethod("m1", String.class, String.class, Integer.class), e.getMethod());
    assertEquals("arg1", e.getParameters().get(0));
    assertEquals("arg2", e.getParameters().get(1));
    assertEquals(23, e.getParameters().get(2));
  }

  @Test
  void complete_1() {
    CompletionClass c = new CompletionClass();
    CompletionContext ctx = defaultCompletionContext("");
    List<CompletionCandidateGroup> groups = c.complete(ctx);

    assertEquals(0, groups.size());
  }

  @Test
  void complete_2() {
    CompletionClass c = new CompletionClass();
    CompletionContext ctx = defaultCompletionContext(" ");
    List<CompletionCandidateGroup> groups = c.complete(ctx);

    assertEquals(0, groups.size());
  }

  @Test
  void complete_3() {
    CompletionClass c = new CompletionClass();
    CompletionContext ctx = defaultCompletionContext("b");
    List<CompletionCandidateGroup> groups = c.complete(ctx);

    assertEquals(
        0, groups.stream().filter(g -> g.getMatchingCompletionCandidates().size() > 0).count());
  }

  @Test
  void complete_4() {
    CompletionClass c = new CompletionClass();
    CompletionContext ctx = defaultCompletionContext("f");
    List<CompletionCandidateGroup> groups = c.complete(ctx);

    assertEquals(1, groups.size());
    assertEquals(2, groups.get(0).getMatchingCompletionCandidates().size());
  }

  @Test
  void complete_5() {
    CompletionClass c = new CompletionClass();
    CompletionContext ctx = defaultCompletionContext("firs");
    List<CompletionCandidateGroup> groups = c.complete(ctx);

    assertEquals(1, groups.size());
    assertEquals(1, groups.get(0).getMatchingCompletionCandidates().size());
  }

  @Test
  void complete_6() {
    CompletionClass c = new CompletionClass();
    CompletionContext ctx = defaultCompletionContext("firs m");
    List<CompletionCandidateGroup> groups = c.complete(ctx);

    assertEquals(0, groups.size());
  }

  @Test
  void complete_7() {
    CompletionClass c = new CompletionClass();
    CompletionContext ctx = defaultCompletionContext("first m");
    List<CompletionCandidateGroup> groups = c.complete(ctx);

    // Should have 2 groups, one with 'mike', and another with 'mike,marta,millie' for the two
    // methods
    assertEquals(2, groups.size());
    assertTrue(
        groups.stream()
            .map(g -> g.getMatchingCompletionCandidates().size() == 1)
            .filter(b -> b)
            .findFirst()
            .orElse(false));
    assertTrue(
        groups.stream()
            .map(g -> g.getMatchingCompletionCandidates().size() == 3)
            .filter(b -> b)
            .findFirst()
            .orElse(false));
  }

  @Test
  void complete_8() {
    CompletionClass c = new CompletionClass();
    CompletionContext ctx = defaultCompletionContext("first marta");
    List<CompletionCandidateGroup> groups = c.complete(ctx);

    assertEquals(
        1, groups.stream().filter(g -> g.getMatchingCompletionCandidates().size() > 0).count());
    assertEquals(
        "marta",
        groups.stream()
            .filter(g -> g.getMatchingCompletionCandidates().size() > 0)
            .collect(Collectors.toList())
            .get(0)
            .getMatchingCompletionCandidates()
            .get(0)
            .getValue());
  }

  @Test
  void complete_9() {
    CompletionClass c = new CompletionClass();
    CompletionContext ctx = defaultCompletionContext("first marta ");
    List<CompletionCandidateGroup> groups = c.complete(ctx);

    assertEquals(1, groups.size());
    assertEquals("art", groups.get(0).getMatchingCompletionCandidates().get(0).getValue());
  }

  @Test
  void complete_10() {
    CompletionClass c = new CompletionClass();
    CompletionContext ctx = defaultCompletionContext("first mike");
    List<CompletionCandidateGroup> groups = c.complete(ctx);

    assertEquals(2, groups.size());
    assertEquals("mike", groups.get(0).getMatchingCompletionCandidates().get(0).getValue());
    assertEquals("mike", groups.get(1).getMatchingCompletionCandidates().get(0).getValue());
  }

  @Test
  void complete_11() {
    CompletionClass c = new CompletionClass();
    CompletionContext ctx = defaultCompletionContext("first mike ");
    List<CompletionCandidateGroup> groups = c.complete(ctx);

    assertEquals(2, groups.size());
    assertTrue(
        groups.stream()
            .map(g -> g.getMatchingCompletionCandidates().size() == 2)
            .filter(b -> b)
            .findFirst()
            .orElse(false));
    assertTrue(
        groups.stream()
            .map(g -> g.getMatchingCompletionCandidates().size() == 1)
            .filter(b -> b)
            .findFirst()
            .orElse(false));
  }

  @Test
  void complete_12() {
    CompletionClass c = new CompletionClass();
    CompletionContext ctx = defaultCompletionContext("first mike p");
    List<CompletionCandidateGroup> groups = c.complete(ctx);

    assertEquals(
        1, groups.stream().filter(g -> g.getMatchingCompletionCandidates().size() > 0).count());
    assertEquals(
        "plate",
        groups.stream()
            .map(CompletionCandidateGroup::getMatchingCompletionCandidates)
            .filter(g -> g.size() > 0)
            .map(g -> g.get(0).getValue())
            .findFirst()
            .orElse(null));
  }

  @Test
  void switchTest_1() {
    SwitchClass1 c = new SwitchClass1();
    ExecutionContext ctx = defaultExecutionContext("opt1 mike art");

    ExecutionCandidate e = c.execute(ctx);
    assertNull(e);
  }

  @Test
  void switchTest_2() throws NoSuchMethodException {
    SwitchClass1 c = new SwitchClass1();
    ExecutionContext ctx = defaultExecutionContext("-c_sw1 opt1 -m_sw2 mike art");

    ExecutionCandidate e = c.execute(ctx);
    assertEquals(c.getClass().getMethod("m2"), e.getMethod());
    assertEquals("opt1", e.getParameters().get(0));
    assertEquals("mike", e.getParameters().get(1));
  }

  @Test
  void switchTest_3() throws NoSuchMethodException {
    SwitchClass1 c = new SwitchClass1();
    ExecutionContext ctx = defaultExecutionContext("-c_sw1 opt1 art -m_sw2 mike");

    ExecutionCandidate e = c.execute(ctx);
    assertEquals(c.getClass().getMethod("m2"), e.getMethod());
    assertEquals("opt1", e.getParameters().get(0));
    assertEquals("mike", e.getParameters().get(1));
  }

  @Test
  void switchTest_4() throws NoSuchMethodException {
    SwitchClass1 c = new SwitchClass1();
    ExecutionContext ctx = defaultExecutionContext("-m_sw2 mike art -c_sw1 opt1");

    ExecutionCandidate e = c.execute(ctx);
    assertEquals(c.getClass().getMethod("m2"), e.getMethod());
    assertEquals("opt1", e.getParameters().get(0));
    assertEquals("mike", e.getParameters().get(1));
  }

  @Test
  void switchTest_5() throws NoSuchMethodException {
    SwitchClass1 c = new SwitchClass1();
    ExecutionContext ctx = defaultExecutionContext("art -c_sw1 opt1 -m_sw2 mike");

    ExecutionCandidate e = c.execute(ctx);
    assertEquals(c.getClass().getMethod("m2"), e.getMethod());
    assertEquals("opt1", e.getParameters().get(0));
    assertEquals("mike", e.getParameters().get(1));
  }

  @Test
  void switchTest_6() throws NoSuchMethodException {
    SwitchClass1 c = new SwitchClass1();
    ExecutionContext ctx = defaultExecutionContext("art -m_sw2 mike -c_sw1 opt1");

    ExecutionCandidate e = c.execute(ctx);
    assertEquals(c.getClass().getMethod("m2"), e.getMethod());
    assertEquals("opt1", e.getParameters().get(0));
    assertEquals("mike", e.getParameters().get(1));
  }

  @Test
  void switchTest_7() {
    SwitchClass1 c = new SwitchClass1();
    ExecutionContext ctx = defaultExecutionContext("-c_sw1 opt1 -m_sw1 bob art");

    ExecutionCandidate e = c.execute(ctx);
    assertNull(e);
  }

  @Test
  void switchTest_8() throws NoSuchMethodException {
    SwitchClass1 c = new SwitchClass1();
    ExecutionContext ctx = defaultExecutionContext("-c_sw1 opt1 -m_sw1 milly art");

    ExecutionCandidate e = c.execute(ctx);
    assertEquals(c.getClass().getMethod("m1"), e.getMethod());
    assertEquals("opt1", e.getParameters().get(0));
    assertEquals("milly", e.getParameters().get(1));
  }

  @Test
  void switchTest_9() throws NoSuchMethodException {
    SwitchClass1 c = new SwitchClass1();
    ExecutionContext ctx = defaultExecutionContext("art -c_sw1 opt1 -m_sw1 milly");

    ExecutionCandidate e = c.execute(ctx);
    assertEquals(c.getClass().getMethod("m1"), e.getMethod());
    assertEquals("opt1", e.getParameters().get(0));
    assertEquals("milly", e.getParameters().get(1));
  }

  @Test
  void switchTest_10() throws NoSuchMethodException {
    SwitchClass1 c = new SwitchClass1();
    ExecutionContext ctx = defaultExecutionContext("art -m_sw1 zoe -c_sw1 opt2");

    ExecutionCandidate e = c.execute(ctx);
    assertEquals(c.getClass().getMethod("m3"), e.getMethod());
    assertEquals("opt2", e.getParameters().get(0));
    assertEquals("zoe", e.getParameters().get(1));
  }

  @Test
  void switchTest_11() throws NoSuchMethodException {
    SwitchClass1 c = new SwitchClass1();
    ExecutionContext ctx = defaultExecutionContext("-c_sw1 opt1 -m_sw3 mike art plate");

    ExecutionCandidate e = c.execute(ctx);
    assertEquals(c.getClass().getMethod("m4"), e.getMethod());
    assertEquals("opt1", e.getParameters().get(0));
    assertEquals("mike", e.getParameters().get(1));
  }

  @Test
  void switchTest_12() throws NoSuchMethodException {
    SwitchClass1 c = new SwitchClass1();
    ExecutionContext ctx = defaultExecutionContext("-c_sw1 opt1 -m_sw3 mike art");

    ExecutionCandidate e = c.execute(ctx);
    assertEquals(c.getClass().getMethod("m6"), e.getMethod());
    assertEquals("opt1", e.getParameters().get(0));
    assertEquals("mike", e.getParameters().get(1));
  }

  @Test
  void switchTest_13() throws NoSuchMethodException {
    SwitchClass1 c = new SwitchClass1();
    ExecutionContext ctx = defaultExecutionContext("art angel -m_sw3 mike -c_sw1 opt1");

    ExecutionCandidate e = c.execute(ctx);
    assertEquals(c.getClass().getMethod("m5"), e.getMethod());
    assertEquals("opt1", e.getParameters().get(0));
    assertEquals("mike", e.getParameters().get(1));
  }

  @Test
  void switchWithDefault_1() throws NoSuchMethodException {
    SwitchClassWithDefaults1 c = new SwitchClassWithDefaults1();
    ExecutionContext ctx = defaultExecutionContext("art -c_sw1 opt1");

    ExecutionCandidate e = c.execute(ctx);
    assertEquals(c.getClass().getMethod("m1"), e.getMethod());
    assertEquals("opt1", e.getParameters().get(0));
    assertEquals("marta", e.getParameters().get(1));
  }

  @Test
  void switchWithDefault_2() throws NoSuchMethodException {
    SwitchClassWithDefaults1 c = new SwitchClassWithDefaults1();
    ExecutionContext ctx = defaultExecutionContext("art -c_sw1 opt1 -m_sw1 milly");

    ExecutionCandidate e = c.execute(ctx);
    assertEquals(c.getClass().getMethod("m2"), e.getMethod());
    assertEquals("opt1", e.getParameters().get(0));
    assertEquals("milly", e.getParameters().get(1));
  }

  @Test
  void complexSwitch_1() {
    ComplexSwitchClass1 c = new ComplexSwitchClass1();
    ExecutionContext ctx = defaultExecutionContext("list 4 bob");

    ExecutionCandidate e = c.execute(ctx);
    assertNull(e);
  }

  @Test
  void classWithCommand_1() {
    AnnotationCommand c = new ClassWithCommand();
    assertNotNull(c.getCommandData());
    assertEquals("cmd1", c.getCommandData().getName());
    assertEquals(2, c.getCommandData().getAliases().length);
    assertEquals("input", c.getCommandData().getInput());
    assertNotNull(c.getCommandData().getParserChain());
  }

  @Arg("c_arg1 c_arg2 c_arg3")
  static class C1 extends AnnotationCommand {}

  @Arg("c_arg1 c_arg2 c_arg3")
  static class C2 extends AnnotationCommand {

    @Default
    public void d() {}

    @Error
    public void e() {}

    @Arg("m_arg1 m_arg2 m_arg3")
    public void m1() {}

    @Arg("m_arg1 m_arg2 m_arg3_m2")
    public void m2() {}
  }

  static class C2Extended extends C2 {}

  static class C3 extends AnnotationCommand {

    @Default
    public void d() {}

    @Error
    public void e() {}

    @Arg("m_arg1 m_arg2 m_arg3")
    public void m1() {}

    @Arg("m_arg1 m_arg2 m_arg3_m2")
    public void m2() {}
  }

  @Arg("child_arg1 child_arg2 child_arg3")
  static class Child1 extends AnnotationCommand {}

  @Arg("child_arg1 child_arg2 child_arg3")
  static class Child2 extends AnnotationCommand {
    @Default
    public void d() {}

    @Error
    public void e() {}

    @Arg("m_arg1 m_arg2 m_arg3")
    public void m1() {}

    @Arg("m_arg1 m_arg2 m_arg3_m2")
    public void m2() {}
  }

  @Arg("c_arg1 @string")
  static class ParamClass extends AnnotationCommand {
    @Arg("@string @int")
    public void m1(String p1, String p2, Integer p3) {}
  }

  @Arg("first|fire|second")
  static class CompletionClass extends AnnotationCommand {
    @Arg("mike|milly|marta art")
    public void m1() {}

    @Arg("alice|bob|mike plate|yeet")
    public void m2() {}
  }

  @Arg("opt1|opt2|opt3(switch=c_sw1, suppress=false)")
  static class SwitchClass1 extends AnnotationCommand {
    @Arg("mike|milly|marta(switch=m_sw1, suppress=false) art")
    public void m1() {}

    @Arg("mike|milly(switch=m_sw2, suppress=false) art")
    public void m2() {}

    @Arg("sue|zoe(switch=m_sw1, suppress=false) art")
    public void m3() {}

    @Arg("mike|milly(switch=m_sw3, suppress=false) art plate")
    public void m4() {}

    @Arg("mike|zoe(switch=m_sw3, suppress=false) art angel")
    public void m5() {}

    @Arg("mike|zoe(switch=m_sw3, suppress=false) art")
    public void m6() {}
  }

  @Arg("opt1|opt2|opt3(switch=c_sw1, suppress=false)")
  static class SwitchClassWithDefaults1 extends AnnotationCommand {
    @Arg("mike|marta(switch=m_sw1, suppress=false, default=marta) art")
    public void m1() {}

    @Arg("mike|milly(switch=m_sw1, suppress=false) art")
    public void m2() {}
  }

  static class ComplexSwitchClass1 extends AnnotationCommand {
    @Arg(
        "list(description=List Process) @int(min=1,max=6,switch=page,default=1,description=Page Number) @string(description=Process Name)")
    public void onList(Integer pageNo, String processName) {}
  }

  @Command(
      value = "cmd1|cmd2|cmd3 c_arg1 c_arg2 c_arg3",
      description = "description",
      input = "input")
  static class ClassWithCommand extends AnnotationCommand {}
}
