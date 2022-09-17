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

import au.com.grieve.bcf.*;
import au.com.grieve.bcf.exception.EndOfLineException;
import au.com.grieve.bcf.impl.line.DefaultParsedLine;
import au.com.grieve.bcf.impl.parser.IntegerParser;
import au.com.grieve.bcf.impl.parser.StringParser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ArgumentParserChainTest {

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
        parserClasses.put("literal", StringParser.class);
        parserClasses.put("string", StringParser.class);
        parserClasses.put("int", IntegerParser.class);
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
        assertEquals(StringParser.class, a.getParsers().get(0).getClass());
    }
    
    @Test
    void oneParser_3() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "@string");
        assertEquals(1, a.getParsers().size());
        assertEquals(StringParser.class, a.getParsers().get(0).getClass());
    }
    
    @Test
    void oneParser_4() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "@int");
        assertEquals(1, a.getParsers().size());
        assertEquals(IntegerParser.class, a.getParsers().get(0).getClass());
    }

    @Test
    void twoParsers_1() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "literal literal");
        assertEquals(2, a.getParsers().size());
        assertEquals(StringParser.class, a.getParsers().get(0).getClass());
        assertEquals(StringParser.class, a.getParsers().get(1).getClass());
    }

    @Test
    void twoParsers_2() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "@string @string");
        assertEquals(2, a.getParsers().size());
        assertEquals(StringParser.class, a.getParsers().get(0).getClass());
        assertEquals(StringParser.class, a.getParsers().get(1).getClass());
    }

    @Test
    void twoParsers_3() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "@int @int");
        assertEquals(2, a.getParsers().size());
        assertEquals(IntegerParser.class, a.getParsers().get(0).getClass());
        assertEquals(IntegerParser.class, a.getParsers().get(1).getClass());
    }

    @Test
    void threeParsers_1() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "literal @string @int");
        assertEquals(3, a.getParsers().size());
        assertEquals(StringParser.class, a.getParsers().get(0).getClass());
        assertEquals(StringParser.class, a.getParsers().get(1).getClass());
        assertEquals(IntegerParser.class, a.getParsers().get(2).getClass());
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
        Context context = new AnnotationContext();
        List<Result> result = new ArrayList<>();

        a.parse(line, result, context);
        assertEquals(1, result.size());
        assertEquals("bob", result.get(0).getValue());
    }

    @Test
    void parseSuppress_2() throws EndOfLineException {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "@string(suppress=true)");
        Context context = new AnnotationContext();
        ParsedLine line = new DefaultParsedLine("bob");
        List<Result> result = new ArrayList<>();

        a.parse(line, result, context);
        assertEquals(0, result.size());
    }

    @Test
    void parseDefaultRequired_1() throws EndOfLineException {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "@string(default=alice)");
        Context context = new AnnotationContext();
        ParsedLine line = new DefaultParsedLine("bob");
        List<Result> result = new ArrayList<>();

        a.parse(line, result,context);
        assertEquals(1, result.size());
        assertEquals("bob", result.get(0).getValue());
    }

    @Test
    void parseDefaultRequired_2() throws EndOfLineException {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "@string(default=alice)");
        Context context = new AnnotationContext();
        ParsedLine line = new DefaultParsedLine("");
        List<Result> result = new ArrayList<>();

        a.parse(line, result, context);
        assertEquals(1, result.size());
        assertEquals("alice", result.get(0).getValue());
    }

    @Test
    void parseDefaultRequired_3() throws EndOfLineException {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "@string(required=false)");
        Context context = new AnnotationContext();
        ParsedLine line = new DefaultParsedLine("bob");
        List<Result> result = new ArrayList<>();

        a.parse(line, result, context);
        assertEquals(1, result.size());
        assertEquals("bob", result.get(0).getValue());
    }

    @Test
    void parseDefaultRequired_4() throws EndOfLineException {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "@string(required=false)");
        Context context = new AnnotationContext();
        ParsedLine line = new DefaultParsedLine("");
        List<Result> result = new ArrayList<>();

        a.parse(line, result, context);
        assertEquals(1, result.size());
        assertNull(result.get(0).getValue());
    }

    @Test
    void parseDefaultRequired_5() throws EndOfLineException {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "@int(default=5)");
        Context context = new AnnotationContext();
        ParsedLine line = new DefaultParsedLine("");
        List<Result> result = new ArrayList<>();

        a.parse(line, result, context);
        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getValue());
    }

    @Test
    void complete_1() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "literal1");
        Context context = new AnnotationContext();
        ParsedLine line = new DefaultParsedLine("");
        List<CompletionCandidateGroup> result = new ArrayList<>();

        assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
    }

    @Test
    void complete_2() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "literal1");
        Context context = new AnnotationContext();
        ParsedLine line = new DefaultParsedLine(" ");
        List<CompletionCandidateGroup> result = new ArrayList<>();

        assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
    }

    @Test
    void complete_3() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "literal1");
        Context context = new AnnotationContext();
        ParsedLine line = new DefaultParsedLine("b");
        List<CompletionCandidateGroup> result = new ArrayList<>();

        assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
        assertEquals(0, result.size());
    }

    @Test
    void complete_4() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "literal1");
        Context context = new AnnotationContext();
        ParsedLine line = new DefaultParsedLine("l");
        List<CompletionCandidateGroup> result = new ArrayList<>();

        assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getCompletionCandidates().size());
    }

    @Test
    void complete_5() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "literal1");
        Context context = new AnnotationContext();
        ParsedLine line = new DefaultParsedLine("literal1");
        List<CompletionCandidateGroup> result = new ArrayList<>();

        assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getCompletionCandidates().size());
    }

    @Test
    void complete_6() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "literal1|literal2|literal3");
        Context context = new AnnotationContext();
        ParsedLine line = new DefaultParsedLine("literal");
        List<CompletionCandidateGroup> result = new ArrayList<>();

        assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
        assertEquals(1, result.size());
        assertEquals(3, result.get(0).getCompletionCandidates().size());
    }

    @Test
    void complete_7() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "literal1|literal2|literal3");
        Context context = new AnnotationContext();
        ParsedLine line = new DefaultParsedLine("literal1");
        List<CompletionCandidateGroup> result = new ArrayList<>();

        assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getCompletionCandidates().size());
    }

    @Test
    void complete_8() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "literal1|literal2|literal3 mike|milly|mark");
        Context context = new AnnotationContext();
        ParsedLine line = new DefaultParsedLine("l");
        List<CompletionCandidateGroup> result = new ArrayList<>();

        assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
        assertEquals(1, result.size());
        assertEquals(3, result.get(0).getCompletionCandidates().size());
    }

    @Test
    void complete_9() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "literal1|literal2|literal mike|milly|mark");
        Context context = new AnnotationContext();
        ParsedLine line = new DefaultParsedLine("literal");
        List<CompletionCandidateGroup> result = new ArrayList<>();

        assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
        assertEquals(1, result.size());
        assertEquals(3, result.get(0).getCompletionCandidates().size());
    }

    @Test
    void complete_10() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "literal1|literal2|literal mike|milly|mark");
        Context context = new AnnotationContext();
        ParsedLine line = new DefaultParsedLine("literal1");
        List<CompletionCandidateGroup> result = new ArrayList<>();

        assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getCompletionCandidates().size());
    }

    @Test
    void complete_11() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "literal1|literal2|literal mike|milly|mark");
        Context context = new AnnotationContext();
        ParsedLine line = new DefaultParsedLine("literal ");
        List<CompletionCandidateGroup> result = new ArrayList<>();

        assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
        assertEquals(1, result.size());
        assertEquals(3, result.get(0).getCompletionCandidates().size());
    }

    @Test
    void complete_12() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "literal1|literal2|literal mike|milly|mark");
        Context context = new AnnotationContext();
        ParsedLine line = new DefaultParsedLine("literal ma");
        List<CompletionCandidateGroup> result = new ArrayList<>();

        assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getCompletionCandidates().size());
    }

    @Test
    void complete_13() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "literal1|literal2|literal mike|milly|mark");
        Context context = new AnnotationContext();
        ParsedLine line = new DefaultParsedLine("literal mark");
        List<CompletionCandidateGroup> result = new ArrayList<>();

        assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getCompletionCandidates().size());
    }

    @Test
    void complete_14() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "literal1|literal2|literal mike|milly|mark");
        Context context = new AnnotationContext();
        ParsedLine line = new DefaultParsedLine("literal markb");
        List<CompletionCandidateGroup> result = new ArrayList<>();

        assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
        assertEquals(0, result.size());
    }

    @Test
    void complete_15() throws EndOfLineException {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "literal1|literal2|literal mike|milly|mark");
        Context context = new AnnotationContext();
        ParsedLine line = new DefaultParsedLine("literal mark b");
        List<CompletionCandidateGroup> result = new ArrayList<>();

        a.complete(line, result, context);
        assertEquals(0, result.size());
    }

    @Test
    void complete_16() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "literal1|literal2|literal mike|milly|mark");
        Context context = new AnnotationContext();
        ParsedLine line = new DefaultParsedLine("l m");
        List<CompletionCandidateGroup> result = new ArrayList<>();

        assertThrows(EndOfLineException.class, () -> a.complete(line, result, context));
        assertEquals(0, result.size());
    }

    @Test
    void parseSwitch_1() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "@string(switch=sw1) @string @string");
        Context context = new AnnotationContext();
        List<Result> result = new ArrayList<>();
        ParsedLine line = new DefaultParsedLine("alice");

        assertThrows(EndOfLineException.class, () -> a.parse(line, result, context));
        assertEquals(2, result.size());

    }

    @Test
    void parseSwitch_2() {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "@string(switch=sw1) @string @string");
        Context context = new AnnotationContext();
        List<Result> result = new ArrayList<>();
        ParsedLine line = new DefaultParsedLine("-sw1 bob");

        assertThrows(EndOfLineException.class, () -> a.parse(line, result, context));
        assertEquals(1, result.size());
        assertEquals("bob", result.get(0).getValue());
    }

    @Test
    void parseSwitch_3() throws EndOfLineException {
        ArgumentParserChain a = new ArgumentParserChain(getParserClasses1(), "@string(switch=sw1) @string @string");
        Context context = new AnnotationContext();
        List<Result> result = new ArrayList<>();
        ParsedLine line = new DefaultParsedLine("alice amy -sw1 bob");

        a.parse(line, result, context);
        assertEquals(3, result.size());
        assertEquals("bob", result.get(0).getValue());
        assertEquals("alice", result.get(1).getValue());
        assertEquals("amy", result.get(2).getValue());
    }


}