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

import au.com.grieve.bcf.ExecutionCandidate;
import au.com.grieve.bcf.ParsedLine;
import au.com.grieve.bcf.framework.annotation.annotations.Arg;
import au.com.grieve.bcf.framework.annotation.annotations.Default;
import au.com.grieve.bcf.framework.annotation.annotations.Error;
import au.com.grieve.bcf.impl.line.DefaultParsedLine;
import au.com.grieve.bcf.impl.parser.IntegerParser;
import au.com.grieve.bcf.impl.parser.StringParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AnnotationCommandTest {



    @Arg("c_arg1 c_arg2 c_arg3")
    static class C1 extends AnnotationCommand {

    }

    @Arg("c_arg1 c_arg2 c_arg3")
    static class C2 extends AnnotationCommand {

        @Default
        public void d() {

        }

        @Error
        public void e() {

        }

        @Arg("m_arg1 m_arg2 m_arg3")
        public void m1() {

        }

        @Arg("m_arg1 m_arg2 m_arg3_m2")
        public void m2() {

        }
    }

    static class C2Extended extends C2 {

    }

    static class C3 extends AnnotationCommand {

        @Default
        public void d() {

        }

        @Error
        public void e() {

        }

        @Arg("m_arg1 m_arg2 m_arg3")
        public void m1() {

        }

        @Arg("m_arg1 m_arg2 m_arg3_m2")
        public void m2() {

        }
    }

    @Arg("child_arg1 child_arg2 child_arg3")
    static class Child1 extends AnnotationCommand {

    }

    @Arg("child_arg1 child_arg2 child_arg3")
    static class Child2 extends AnnotationCommand {
        @Default
        public void d() {

        }

        @Error
        public void e() {

        }

        @Arg("m_arg1 m_arg2 m_arg3")
        public void m1() {

        }

        @Arg("m_arg1 m_arg2 m_arg3_m2")
        public void m2() {

        }
    }

    @Arg("c_arg1 @string")
    static class ParamClass extends AnnotationCommand {
        @Arg("@string @int")
        public void m1(String p1, String p2, Integer p3) {

        }
    }

    @Test
    void noDefaultDefined() {
        C1 c1 = new C1();
        ParsedLine line = new DefaultParsedLine("bob");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        assertNull(c1.execute(line, ctx));
    }

    @Test
    void noArgMatchOnClass_1() throws NoSuchMethodException {
        C2 c2 = new C2();
        ParsedLine line = new DefaultParsedLine("");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c2.execute(line, ctx);

        // Should be error method with a weight of 0
        assertEquals(c2.getClass().getMethod("e"), e.getMethod());
        assertEquals(0, e.getWeight());
    }

    @Test
    void noArgMatchOnClass_2() throws NoSuchMethodException {
        C2 c2 = new C2();
        ParsedLine line = new DefaultParsedLine("bob");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c2.execute(line, ctx);

        // Should be error method with a weight of 0
        assertEquals(c2.getClass().getMethod("e"), e.getMethod());
        assertEquals(0, e.getWeight());
    }

    @Test
    void someArgMatchOnClass_1() throws NoSuchMethodException {
        C2 c2 = new C2();
        ParsedLine line = new DefaultParsedLine("c_arg1 c_arg2");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c2.execute(line, ctx);

        // Should be error method with a weight of 2
        assertEquals(c2.getClass().getMethod("e"), e.getMethod());
        assertEquals(2, e.getWeight());
    }

    @Test
    void someArgMatchOnClass_2() throws NoSuchMethodException {
        C2 c2 = new C2();
        ParsedLine line = new DefaultParsedLine("c_arg1 c_arg2 bob");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c2.execute(line, ctx);

        // Should be error method with a weight of 2
        assertEquals(c2.getClass().getMethod("e"), e.getMethod());
        assertEquals(2, e.getWeight());
    }

    @Test
    void argMatchClassNotMethod_1() throws NoSuchMethodException {
        C2 c2 = new C2();
        ParsedLine line = new DefaultParsedLine("c_arg1 c_arg2 c_arg3");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c2.execute(line, ctx);

        // Should be default method with a weight of 3
        assertEquals(c2.getClass().getMethod("d"), e.getMethod());
        assertEquals(3, e.getWeight());
    }

    @Test
    void argMatchClassNotMethod_2() throws NoSuchMethodException {
        C2 c2 = new C2();
        ParsedLine line = new DefaultParsedLine("c_arg1 c_arg2 c_arg3 bob");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c2.execute(line, ctx);

        // Should be default method with a weight of 3
        assertEquals(c2.getClass().getMethod("d"), e.getMethod());
        assertEquals(3, e.getWeight());
    }

    @Test
    void argMatchClassSomeMethod_1() throws NoSuchMethodException {
        C2 c2 = new C2();
        ParsedLine line = new DefaultParsedLine("c_arg1 c_arg2 c_arg3 m_arg1 m_arg2");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c2.execute(line, ctx);

        // Should be error method with a weight of 5
        assertEquals(c2.getClass().getMethod("e"), e.getMethod());
        assertEquals(5, e.getWeight());
    }

    @Test
    void argMatchClassSomeMethod_2() throws NoSuchMethodException {
        C2 c2 = new C2();
        ParsedLine line = new DefaultParsedLine("c_arg1 c_arg2 c_arg3 m_arg1 m_arg2 bob");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c2.execute(line, ctx);

        // Should be error method with a weight of 5
        assertEquals(c2.getClass().getMethod("e"), e.getMethod());
        assertEquals(5, e.getWeight());
    }

    @Test
    void argMatchClassAndMethod_1() throws NoSuchMethodException {
        C2 c2 = new C2();
        ParsedLine line = new DefaultParsedLine("c_arg1 c_arg2 c_arg3 m_arg1 m_arg2 m_arg3");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c2.execute(line, ctx);

        // Success
        assertEquals(c2.getClass().getMethod("m1"), e.getMethod());
        assertEquals(6, e.getWeight());
    }

    @Test
    void argMatchClassAndMethod_2() throws NoSuchMethodException {
        C2 c2 = new C2();
        ParsedLine line = new DefaultParsedLine("c_arg1 c_arg2 c_arg3 m_arg1 m_arg2 m_arg3_m2");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c2.execute(line, ctx);

        // Success
        assertEquals(c2.getClass().getMethod("m2"), e.getMethod());
        assertEquals(6, e.getWeight());
    }

    // Test when there is no class args

    @Test
    void noArgMatchOnMethod_1() throws NoSuchMethodException {
        C3 c3 = new C3();
        ParsedLine line = new DefaultParsedLine("");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c3.execute(line, ctx);

        // Should be default method with a weight of 0
        assertEquals(c3.getClass().getMethod("d"), e.getMethod());
        assertEquals(0, e.getWeight());
    }

    @Test
    void noArgMatchOnMethod_2() throws NoSuchMethodException {
        C3 c3 = new C3();
        ParsedLine line = new DefaultParsedLine("bob");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c3.execute(line, ctx);

        // Should be default method with a weight of 0
        assertEquals(c3.getClass().getMethod("d"), e.getMethod());
        assertEquals(0, e.getWeight());
    }

    @Test
    void someMatchOnMethod_1() throws NoSuchMethodException {
        C3 c3 = new C3();
        ParsedLine line = new DefaultParsedLine("m_arg1 m_arg2");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c3.execute(line, ctx);

        // Should be error method with a weight of 2
        assertEquals(c3.getClass().getMethod("e"), e.getMethod());
        assertEquals(2, e.getWeight());
    }

    @Test
    void someMatchOnMethod_2() throws NoSuchMethodException {
        C3 c3 = new C3();
        ParsedLine line = new DefaultParsedLine("m_arg1 m_arg2 bob");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c3.execute(line, ctx);

        // Should be error method with a weight of 2
        assertEquals(c3.getClass().getMethod("e"), e.getMethod());
        assertEquals(2, e.getWeight());
    }

    @Test
    void argMatchMethod_1() throws NoSuchMethodException {
        C3 c3 = new C3();
        ParsedLine line = new DefaultParsedLine("m_arg1 m_arg2 m_arg3");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c3.execute(line, ctx);

        // Success
        assertEquals(c3.getClass().getMethod("m1"), e.getMethod());
        assertEquals(3, e.getWeight());
    }

    @Test
    void argMatchMethod_2() throws NoSuchMethodException {
        C3 c3 = new C3();
        ParsedLine line = new DefaultParsedLine("m_arg1 m_arg2 m_arg3_m2");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c3.execute(line, ctx);

        // Success
        assertEquals(c3.getClass().getMethod("m2"), e.getMethod());
        assertEquals(3, e.getWeight());
    }

    // Child Class Tests
    @Test
    void noChildArgMatch_1() throws NoSuchMethodException {
        C2 c2 = new C2();
        Child1 child1 = new Child1();
        c2.addChild(child1);

        ParsedLine line = new DefaultParsedLine("c_arg1 c_arg2 c_arg3");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c2.execute(line, ctx);

        // Should be default method with a weight of 3 on parent
        assertEquals(c2.getClass().getMethod("d"), e.getMethod());
        assertEquals(3, e.getWeight());
    }

    @Test
    void noChildArgMatch_2() throws NoSuchMethodException {
        C2 c2 = new C2();
        Child1 child1 = new Child1();
        c2.addChild(child1);

        ParsedLine line = new DefaultParsedLine("c_arg1 c_arg2 c_arg3 bob");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c2.execute(line, ctx);

        // Should be default method with a weight of 3 on parent
        assertEquals(c2.getClass().getMethod("d"), e.getMethod());
        assertEquals(3, e.getWeight());
    }

    @Test
    void someChildArgMatch_1() throws NoSuchMethodException {
        C2 c2 = new C2();
        Child1 child1 = new Child1();
        c2.addChild(child1);

        ParsedLine line = new DefaultParsedLine("c_arg1 c_arg2 c_arg3 child_arg1 child_arg2");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c2.execute(line, ctx);

        // Should be error method with a weight of 5 on parent as child has no error
        assertEquals(c2.getClass().getMethod("e"), e.getMethod());
        assertEquals(5, e.getWeight());
    }

    @Test
    void someChildArgMatch_2() throws NoSuchMethodException {
        C2 c2 = new C2();
        Child1 child1 = new Child1();
        c2.addChild(child1);

        ParsedLine line = new DefaultParsedLine("c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 bob");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c2.execute(line, ctx);

        // Should be error method with a weight of 5 on parent as child has no error
        assertEquals(c2.getClass().getMethod("e"), e.getMethod());
        assertEquals(5, e.getWeight());
    }

    @Test
    void someChildArgMatch_3() throws NoSuchMethodException {
        C2 c2 = new C2();
        Child2 child2 = new Child2();
        c2.addChild(child2);

        ParsedLine line = new DefaultParsedLine("c_arg1 c_arg2 c_arg3 child_arg1 child_arg2");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c2.execute(line, ctx);

        // Should be error method with a weight of 5 on child
        assertEquals(child2.getClass().getMethod("e"), e.getMethod());
        assertEquals(5, e.getWeight());
    }

    @Test
    void someChildArgMatch_4() throws NoSuchMethodException {
        C2 c2 = new C2();
        Child2 child2 = new Child2();
        c2.addChild(child2);

        ParsedLine line = new DefaultParsedLine("c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 bob");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c2.execute(line, ctx);

        // Should be error method with a weight of 5 on child
        assertEquals(child2.getClass().getMethod("e"), e.getMethod());
        assertEquals(5, e.getWeight());
    }

    @Test
    void childArgMatch_1() throws NoSuchMethodException {
        C2 c2 = new C2();
        Child1 child1 = new Child1();
        c2.addChild(child1);

        ParsedLine line = new DefaultParsedLine("c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 child_arg3");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c2.execute(line, ctx);

        // Should be default method with a weight of 6 on parent as child has no default
        assertEquals(c2.getClass().getMethod("d"), e.getMethod());
        assertEquals(6, e.getWeight());
    }

    @Test
    void childArgMatch_2() throws NoSuchMethodException {
        C2 c2 = new C2();
        Child2 child2 = new Child2();
        c2.addChild(child2);

        ParsedLine line = new DefaultParsedLine("c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 child_arg3");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c2.execute(line, ctx);

        // Should be default method with a weight of 6 on child
        assertEquals(child2.getClass().getMethod("d"), e.getMethod());
        assertEquals(6, e.getWeight());
    }

    @Test
    void childArgMatchSomeMethod_1() throws NoSuchMethodException {
        C2 c2 = new C2();
        Child2 child2 = new Child2();
        c2.addChild(child2);

        ParsedLine line = new DefaultParsedLine("c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 child_arg3 m_arg1 m_arg2");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c2.execute(line, ctx);

        // Should be error method with a weight of 8 on child
        assertEquals(child2.getClass().getMethod("e"), e.getMethod());
        assertEquals(8, e.getWeight());
    }

    @Test
    void childArgMatchSomeMethod_2() throws NoSuchMethodException {
        C2 c2 = new C2();
        Child2 child2 = new Child2();
        c2.addChild(child2);

        ParsedLine line = new DefaultParsedLine("c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 child_arg3 m_arg1 m_arg2 bob");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c2.execute(line, ctx);

        // Should be error method with a weight of 8 on child
        assertEquals(child2.getClass().getMethod("e"), e.getMethod());
        assertEquals(8, e.getWeight());
    }

    @Test
    void childArgMatchMethod_1() throws NoSuchMethodException {
        C2 c2 = new C2();
        Child2 child2 = new Child2();
        c2.addChild(child2);

        ParsedLine line = new DefaultParsedLine("c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 child_arg3 m_arg1 m_arg2 m_arg3");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c2.execute(line, ctx);

        // Success
        assertEquals(child2.getClass().getMethod("m1"), e.getMethod());
        assertEquals(9, e.getWeight());
    }

    @Test
    void childArgMatchMethod_2() throws NoSuchMethodException {
        C2 c2 = new C2();
        Child2 child2 = new Child2();
        c2.addChild(child2);

        ParsedLine line = new DefaultParsedLine("c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 child_arg3 m_arg1 m_arg2 m_arg3 bob");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c2.execute(line, ctx);

        // Success
        assertEquals(child2.getClass().getMethod("m1"), e.getMethod());
        assertEquals(9, e.getWeight());
    }

    @Test
    void childArgMatchMethod_3() throws NoSuchMethodException {
        C2 c2 = new C2();
        Child2 child2 = new Child2();
        c2.addChild(child2);

        ParsedLine line = new DefaultParsedLine("c_arg1 c_arg2 c_arg3 child_arg1 child_arg2 child_arg3 m_arg1 m_arg2 m_arg3_m2");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c2.execute(line, ctx);

        // Success
        assertEquals(child2.getClass().getMethod("m2"), e.getMethod());
        assertEquals(9, e.getWeight());
    }

    @Test
    void extendedClass_1() throws NoSuchMethodException {
        C2Extended c2 = new C2Extended();
        ParsedLine line = new DefaultParsedLine("bob");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c2.execute(line, ctx);

        // Should be error method with a weight of 0
        assertEquals(c2.getClass().getMethod("e"), e.getMethod());
        assertEquals(0, e.getWeight());
    }

    @Test
    void extendedClass_2() throws NoSuchMethodException {
        C2Extended c2 = new C2Extended();
        ParsedLine line = new DefaultParsedLine("c_arg1 c_arg2 c_arg3");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ExecutionCandidate e = c2.execute(line, ctx);

        // Should be default method with a weight of 3
        assertEquals(c2.getClass().getMethod("d"), e.getMethod());
        assertEquals(3, e.getWeight());
    }

    @Test
    void parameters_1() throws NoSuchMethodException {
        ParamClass c = new ParamClass();
        ParsedLine line = new DefaultParsedLine("c_arg1 arg1 arg2 23");
        AnnotationContext ctx = AnnotationContext.builder().build();
        ctx.getParserClasses().put("literal", StringParser.class);
        ctx.getParserClasses().put("string", StringParser.class);
        ctx.getParserClasses().put("int", IntegerParser.class);
        ExecutionCandidate e = c.execute(line, ctx);

        // Should be m1
        assertEquals(c.getClass().getMethod("m1", String.class, String.class, Integer.class), e.getMethod());
        assertEquals("arg1", e.getParameters().get(0));
        assertEquals("arg2", e.getParameters().get(1));
        assertEquals(23, e.getParameters().get(2));
    }


}