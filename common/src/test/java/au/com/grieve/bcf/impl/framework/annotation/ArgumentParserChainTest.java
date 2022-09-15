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

import au.com.grieve.bcf.CompletionCandidate;
import au.com.grieve.bcf.ParsedLine;
import au.com.grieve.bcf.Parser;
import au.com.grieve.bcf.exception.EndOfLineException;
import au.com.grieve.bcf.impl.line.DefaultParsedLine;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ArgumentParserChainTest {

    static class TestParser1 extends Parser<String> {

        public TestParser1(Map<String, String> parameters) {
            super(parameters);
        }

        @Override
        public void complete(ParsedLine line, List<CompletionCandidate> candidates) {

        }

        @Override
        public String parse(ParsedLine line) throws EndOfLineException {
            return line.next();
        }

    }

    static class TestParser2 extends Parser<Integer> {

        public TestParser2(Map<String, String> parameters) {
            super(parameters);
        }

        @Override
        public void complete(ParsedLine line, List<CompletionCandidate> candidates) {

        }

        @Override
        public Integer parse(ParsedLine line) throws EndOfLineException {
            return Integer.parseInt(line.next());
        }

    }

    @Test
    void noParsers_1() {
        final Map<String, Class<? extends Parser<?>>> parserClasses = new HashMap<>();

        // Should work
        assertEquals(0, new ArgumentParserChain(parserClasses, "").getParsers().size());
    }

    @Test
    void noParsers_2() {
        final Map<String, Class<? extends Parser<?>>> parserClasses = new HashMap<>();

        // No such parser
        assertThrows(RuntimeException.class, () -> new ArgumentParserChain(parserClasses, "first second third"));
    }
    
    @Test
    void noParsers_3() {
        final Map<String, Class<? extends Parser<?>>> parserClasses = new HashMap<>();

        // No such parser
        assertThrows(RuntimeException.class, () -> new ArgumentParserChain(parserClasses, "@string"));
    }

    Map<String, Class<? extends Parser<?>>> getParserClasses1() {
        final Map<String, Class<? extends Parser<?>>> parserClasses = new HashMap<>();
        parserClasses.put("literal", TestParser1.class);
        parserClasses.put("string", TestParser1.class);
        parserClasses.put("int", TestParser2.class);
        return parserClasses;
    }

    @Test
    void oneParser_1() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "");
        assertEquals(0, a.getParsers().size());
    }
    
    @Test
    void oneParser_2() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "literal");
        assertEquals(1, a.getParsers().size());
        assertEquals(TestParser1.class, a.getParsers().get(0).getClass());
    }
    
    @Test
    void oneParser_3() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "@string");
        assertEquals(1, a.getParsers().size());
        assertEquals(TestParser1.class, a.getParsers().get(0).getClass());
    }
    
    @Test
    void oneParser_4() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "@int");
        assertEquals(1, a.getParsers().size());
        assertEquals(TestParser2.class, a.getParsers().get(0).getClass());
    }

    @Test
    void twoParsers_1() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "literal literal");
        assertEquals(2, a.getParsers().size());
        assertEquals(TestParser1.class, a.getParsers().get(0).getClass());
        assertEquals(TestParser1.class, a.getParsers().get(1).getClass());
    }

    @Test
    void twoParsers_2() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "@string @string");
        assertEquals(2, a.getParsers().size());
        assertEquals(TestParser1.class, a.getParsers().get(0).getClass());
        assertEquals(TestParser1.class, a.getParsers().get(1).getClass());
    }

    @Test
    void twoParsers_3() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "@int @int");
        assertEquals(2, a.getParsers().size());
        assertEquals(TestParser2.class, a.getParsers().get(0).getClass());
        assertEquals(TestParser2.class, a.getParsers().get(1).getClass());
    }

    @Test
    void threeParsers_1() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "literal @string @int");
        assertEquals(3, a.getParsers().size());
        assertEquals(TestParser1.class, a.getParsers().get(0).getClass());
        assertEquals(TestParser1.class, a.getParsers().get(1).getClass());
        assertEquals(TestParser2.class, a.getParsers().get(2).getClass());
    }

    @Test
    void invalidParsers_1() {
        assertThrows(RuntimeException.class, () -> new ArgumentParserChain(getParserClasses1(), "@bob"));
    }

    @Test
    void invalidParsers_2() {
        assertThrows(RuntimeException.class, () -> new ArgumentParserChain(getParserClasses1(), "literal @bob"));
    }

    @Test
    void invalidParsers_3() {
        assertThrows(RuntimeException.class, () -> new ArgumentParserChain(getParserClasses1(), "@int @bob"));
    }

    @Test
    void invalidParsers_4() {
        assertThrows(RuntimeException.class, () -> new ArgumentParserChain(getParserClasses1(), "literal string @int @bob"));
    }

    @Test
    void literalParameters_1() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "literal");
        assertEquals(2, a.getParsers().get(0).getParameters().size());
        assertEquals("literal", a.getParsers().get(0).getParameters().get("options"));
        assertEquals("true", a.getParsers().get(0).getParameters().get("suppress"));
    }

    @Test
    void literalParameters_2() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "literal1|literal2|literal3");
        assertEquals(2, a.getParsers().get(0).getParameters().size());
        assertEquals("literal1|literal2|literal3", a.getParsers().get(0).getParameters().get("options"));
        assertEquals("true", a.getParsers().get(0).getParameters().get("suppress"));
    }

    @Test
    void multiParams_1() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "literal(p1=one,p2='two',p3='one two',p4=one two,suppress=false)");
        assertEquals(6, a.getParsers().get(0).getParameters().size());
        assertEquals("literal", a.getParsers().get(0).getParameters().get("options"));
        assertEquals("one", a.getParsers().get(0).getParameters().get("p1"));
        assertEquals("two", a.getParsers().get(0).getParameters().get("p2"));
        assertEquals("one two", a.getParsers().get(0).getParameters().get("p3"));
        assertEquals("one two", a.getParsers().get(0).getParameters().get("p4"));
        assertEquals("false", a.getParsers().get(0).getParameters().get("suppress"));
    }

    @Test
    void parseSuppress_1() throws EndOfLineException {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "@string");
        ParsedLine line = new DefaultParsedLine("bob");
        List<Object> result = new ArrayList<>();

        a.parse(line, result);
        System.err.println(result);
        assertEquals(1, result.size());
        assertEquals("bob", result.get(0));
    }

    @Test
    void parseSuppress_2() throws EndOfLineException {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "@string(suppress=true)");
        ParsedLine line = new DefaultParsedLine("bob");
        List<Object> result = new ArrayList<>();

        a.parse(line, result);
        assertEquals(0, result.size());
    }

    @Test
    void parseDefaultRequired_1() throws EndOfLineException {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "@string(default=alice)");
        ParsedLine line = new DefaultParsedLine("bob");
        List<Object> result = new ArrayList<>();

        a.parse(line, result);
        assertEquals(1, result.size());
        assertEquals("bob", result.get(0));
    }

    @Test
    void parseDefaultRequired_2() throws EndOfLineException {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "@string(default=alice)");
        ParsedLine line = new DefaultParsedLine("");
        List<Object> result = new ArrayList<>();

        a.parse(line, result);
        assertEquals(1, result.size());
        assertEquals("alice", result.get(0));
    }

    @Test
    void parseDefaultRequired_3() throws EndOfLineException {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "@string(required=false)");
        ParsedLine line = new DefaultParsedLine("bob");
        List<Object> result = new ArrayList<>();

        a.parse(line, result);
        assertEquals(1, result.size());
        assertEquals("bob", result.get(0));
    }

    @Test
    void parseDefaultRequired_4() throws EndOfLineException {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "@string(required=false)");
        ParsedLine line = new DefaultParsedLine("");
        List<Object> result = new ArrayList<>();

        a.parse(line, result);
        assertEquals(1, result.size());
        assertNull(result.get(0));
    }
}