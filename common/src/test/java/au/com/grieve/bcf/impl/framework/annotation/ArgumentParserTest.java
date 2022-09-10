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

import au.com.grieve.bcf.Parser;
import au.com.grieve.bcf.impl.parser.IntegerParser;
import au.com.grieve.bcf.impl.parser.StringParser;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArgumentParserTest {

    @Test
    void noParsers() {
        final Map<String, Class<? extends Parser<?>>> parserClasses = new HashMap<>();

        // Should work
        assertEquals(0, new ArgumentParser(parserClasses, "").getParsers().size());

        // No such parser
        assertThrows(RuntimeException.class, () -> new ArgumentParser(parserClasses, "first second third"));

        // No such parser
        assertThrows(RuntimeException.class, () -> new ArgumentParser(parserClasses, "@string"));
    }

    @Test
    void parsers() {
        final Map<String, Class<? extends Parser<?>>> parserClasses = new HashMap<>();
        parserClasses.put("string", StringParser.class);
        parserClasses.put("int", IntegerParser.class);
        ArgumentParser a;

        // 1 Parser
        a = new ArgumentParser(parserClasses, "");
        assertEquals(0, a.getParsers().size());

        a = new ArgumentParser(parserClasses, "literal");
        assertEquals(1, a.getParsers().size());
        assertEquals(StringParser.class, a.getParsers().get(0).getClass());

        a = new ArgumentParser(parserClasses, "@string");
        assertEquals(1, a.getParsers().size());
        assertEquals(StringParser.class, a.getParsers().get(0).getClass());

        a = new ArgumentParser(parserClasses, "@int");
        assertEquals(1, a.getParsers().size());
        assertEquals(IntegerParser.class, a.getParsers().get(0).getClass());

        // 2 Parsers
        a = new ArgumentParser(parserClasses, "literal literal");
        assertEquals(2, a.getParsers().size());
        assertEquals(StringParser.class, a.getParsers().get(0).getClass());
        assertEquals(StringParser.class, a.getParsers().get(1).getClass());

        a = new ArgumentParser(parserClasses, "@string @string");
        assertEquals(2, a.getParsers().size());
        assertEquals(StringParser.class, a.getParsers().get(0).getClass());
        assertEquals(StringParser.class, a.getParsers().get(1).getClass());

        a = new ArgumentParser(parserClasses, "@int @int");
        assertEquals(2, a.getParsers().size());
        assertEquals(IntegerParser.class, a.getParsers().get(0).getClass());
        assertEquals(IntegerParser.class, a.getParsers().get(1).getClass());

        a = new ArgumentParser(parserClasses, "literal @string @int");
        assertEquals(3, a.getParsers().size());
        assertEquals(StringParser.class, a.getParsers().get(0).getClass());
        assertEquals(StringParser.class, a.getParsers().get(1).getClass());
        assertEquals(IntegerParser.class, a.getParsers().get(2).getClass());


        // No such parser
        assertThrows(RuntimeException.class, () -> new ArgumentParser(parserClasses, "@bob"));
        assertThrows(RuntimeException.class, () -> new ArgumentParser(parserClasses, "literal @bob"));
        assertThrows(RuntimeException.class, () -> new ArgumentParser(parserClasses, "@int @bob"));
        assertThrows(RuntimeException.class, () -> new ArgumentParser(parserClasses, "literal string @int @bob"));

    }
}