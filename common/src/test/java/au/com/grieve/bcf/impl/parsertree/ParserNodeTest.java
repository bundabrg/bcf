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

package au.com.grieve.bcf.impl.parsertree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.com.grieve.bcf.ParserTree;
import au.com.grieve.bcf.ParserTreeContext;
import au.com.grieve.bcf.ParserTreeFallbackHandler;
import au.com.grieve.bcf.ParserTreeHandler;
import au.com.grieve.bcf.ParserTreeHandlerCandidate;
import au.com.grieve.bcf.impl.error.InputExpectedError;
import au.com.grieve.bcf.impl.error.InvalidOptionError;
import au.com.grieve.bcf.impl.parser.StringParser;
import au.com.grieve.bcf.impl.parsertree.generator.StringParserGenerator;
import org.junit.jupiter.api.Test;

class ParserNodeTest {

  StringParserGenerator<Object> generator =
      new StringParserGenerator<>((name, parameters) -> new StringParser(parameters));

  @Test
  void parse_1() {
    ParserTree<Object> node = generator.from("");

    ParserTreeHandlerCandidate<Object> e = node.parse("", null);
    assertNull(e);
  }

  @Test
  void parse_2() {
    ParserTree<Object> node = generator.from("");

    ParserTreeHandlerCandidate<Object> e = node.parse("bob", null);
    assertNull(e);
  }

  @Test
  void parse_3() {
    TestParserTreeHandler executeHandler = new TestParserTreeHandler();
    TestParserTreeHandler errorHandler = new TestParserTreeHandler();
    TestParserTreeFallbackHandler fallbackHandler = new TestParserTreeFallbackHandler();

    ParserTree<Object> node =
        generator
            .from("")
            .forEachLeaf(
                n -> n.execute(executeHandler).error(errorHandler).fallback(fallbackHandler));

    ParserTreeHandlerCandidate<Object> e = node.parse("bob", null);
    assertNull(e);
  }

  @Test
  void parse_4() {
    TestParserTreeHandler executeHandler = new TestParserTreeHandler();
    TestParserTreeHandler errorHandler = new TestParserTreeHandler();
    TestParserTreeFallbackHandler fallbackHandler = new TestParserTreeFallbackHandler();
    TestParserTreeHandler rootErrorHandler = new TestParserTreeHandler();

    ParserTree<Object> node =
        generator
            .from("literal1")
            .forEachLeaf(
                n -> n.execute(executeHandler).error(errorHandler).fallback(fallbackHandler))
            .error(rootErrorHandler);

    ParserTreeHandlerCandidate<Object> e = node.parse("", null);
    assertNotNull(e);
    assertEquals(e.getHandler(), rootErrorHandler);
    assertTrue(
        e.getContext().getErrors().stream()
            .anyMatch(err -> err.getClass() == InputExpectedError.class));
    assertEquals(0, fallbackHandler.count);
  }

  @Test
  void parse_5() {
    TestParserTreeHandler executeHandler = new TestParserTreeHandler();
    TestParserTreeHandler errorHandler = new TestParserTreeHandler();
    TestParserTreeFallbackHandler fallbackHandler = new TestParserTreeFallbackHandler();
    TestParserTreeHandler rootErrorHandler = new TestParserTreeHandler();

    ParserTree<Object> node =
        generator
            .from("literal1")
            .forEachLeaf(
                n -> n.execute(executeHandler).error(errorHandler).fallback(fallbackHandler))
            .error(rootErrorHandler);

    ParserTreeHandlerCandidate<Object> e = node.parse("bob", null);
    assertNotNull(e);
    assertEquals(e.getHandler(), rootErrorHandler);
    assertTrue(
        e.getContext().getErrors().stream()
            .anyMatch(err -> err.getClass() == InvalidOptionError.class));
    assertEquals(0, fallbackHandler.count);
  }

  @Test
  void parse_6() {
    TestParserTreeHandler executeHandler = new TestParserTreeHandler();
    TestParserTreeHandler errorHandler = new TestParserTreeHandler();
    TestParserTreeFallbackHandler fallbackHandler = new TestParserTreeFallbackHandler();
    TestParserTreeHandler rootErrorHandler = new TestParserTreeHandler();

    ParserTree<Object> node =
        generator
            .from("literal1")
            .forEachLeaf(
                n -> n.execute(executeHandler).error(errorHandler).fallback(fallbackHandler))
            .error(rootErrorHandler);

    ParserTreeHandlerCandidate<Object> e = node.parse("literal1", null);
    assertNotNull(e);
    assertEquals(e.getHandler(), executeHandler);
    assertEquals(1, fallbackHandler.count);
  }

  @Test
  void parse_7() {
    TestParserTreeHandler executeHandler = new TestParserTreeHandler();
    TestParserTreeHandler errorHandler = new TestParserTreeHandler();
    TestParserTreeFallbackHandler fallbackHandler = new TestParserTreeFallbackHandler();
    TestParserTreeHandler rootErrorHandler = new TestParserTreeHandler();

    ParserTree<Object> node =
        generator
            .from("literal1 literal2 literal3")
            .forEachLeaf(
                n -> n.execute(executeHandler).error(errorHandler).fallback(fallbackHandler))
            .error(rootErrorHandler);

    ParserTreeHandlerCandidate<Object> e = node.parse("literal1", null);
    assertNotNull(e);
    assertEquals(e.getHandler(), rootErrorHandler);
    assertTrue(
        e.getContext().getErrors().stream()
            .anyMatch(err -> err.getClass() == InputExpectedError.class));
    assertEquals(0, fallbackHandler.count);
  }

  @Test
  void parse_8() {
    TestParserTreeHandler executeHandler = new TestParserTreeHandler();
    TestParserTreeHandler errorHandler = new TestParserTreeHandler();
    TestParserTreeFallbackHandler fallbackHandler = new TestParserTreeFallbackHandler();
    TestParserTreeHandler rootErrorHandler = new TestParserTreeHandler();

    ParserTree<Object> node =
        generator
            .from("literal1 literal2 literal3")
            .forEachLeaf(
                n -> n.execute(executeHandler).error(errorHandler).fallback(fallbackHandler))
            .error(rootErrorHandler);

    ParserTreeHandlerCandidate<Object> e = node.parse("literal1 bob", null);
    assertNotNull(e);
    assertEquals(e.getHandler(), rootErrorHandler);
    assertTrue(
        e.getContext().getErrors().stream()
            .anyMatch(err -> err.getClass() == InvalidOptionError.class));
    assertEquals(0, fallbackHandler.count);
  }

  @Test
  void parse_9() {
    TestParserTreeHandler executeHandler = new TestParserTreeHandler();
    TestParserTreeHandler errorHandler = new TestParserTreeHandler();
    TestParserTreeFallbackHandler fallbackHandler = new TestParserTreeFallbackHandler();
    TestParserTreeHandler rootErrorHandler = new TestParserTreeHandler();

    ParserTree<Object> node =
        generator
            .from("literal1 literal2 literal3")
            .forEachLeaf(
                n -> n.execute(executeHandler).error(errorHandler).fallback(fallbackHandler))
            .error(rootErrorHandler);

    ParserTreeHandlerCandidate<Object> e = node.parse("literal1 literal2", null);
    assertNotNull(e);
    assertEquals(e.getHandler(), rootErrorHandler);
    assertTrue(
        e.getContext().getErrors().stream()
            .anyMatch(err -> err.getClass() == InputExpectedError.class));
    assertEquals(0, fallbackHandler.count);
  }

  @Test
  void parse_10() {
    TestParserTreeHandler executeHandler = new TestParserTreeHandler();
    TestParserTreeHandler errorHandler = new TestParserTreeHandler();
    TestParserTreeFallbackHandler fallbackHandler = new TestParserTreeFallbackHandler();
    TestParserTreeHandler rootErrorHandler = new TestParserTreeHandler();

    ParserTree<Object> node =
        generator
            .from("literal1 literal2 literal3")
            .forEachLeaf(
                n -> n.execute(executeHandler).error(errorHandler).fallback(fallbackHandler))
            .error(rootErrorHandler);

    ParserTreeHandlerCandidate<Object> e = node.parse("literal1 literal2 literal3", null);
    assertNotNull(e);
    assertEquals(e.getHandler(), executeHandler);
    assertEquals(1, fallbackHandler.count);
  }

  @Test
  void parse_11() {
    TestParserTreeHandler executeHandler = new TestParserTreeHandler();
    TestParserTreeHandler errorHandler = new TestParserTreeHandler();
    TestParserTreeFallbackHandler fallbackHandler = new TestParserTreeFallbackHandler();
    TestParserTreeHandler rootErrorHandler = new TestParserTreeHandler();

    ParserTree<Object> node =
        generator
            .from("literal1 literal2 literal3")
            .forEachLeaf(
                n -> n.execute(executeHandler).error(errorHandler).fallback(fallbackHandler))
            .error(rootErrorHandler);

    ParserTreeHandlerCandidate<Object> e = node.parse("literal1 literal2 literal3 bob", null);
    assertNull(e);
    assertEquals(1, fallbackHandler.count);
  }

  @Test
  void parse_12() {
    TestParserTreeHandler executeHandler = new TestParserTreeHandler();
    TestParserTreeHandler errorHandler = new TestParserTreeHandler();
    TestParserTreeFallbackHandler fallbackHandler = new TestParserTreeFallbackHandler();
    TestParserTreeHandler rootErrorHandler = new TestParserTreeHandler();

    ParserTree<Object> node =
        generator
            .from("literal1|literal2|literal3")
            .forEachLeaf(
                n -> n.execute(executeHandler).error(errorHandler).fallback(fallbackHandler))
            .error(rootErrorHandler);

    ParserTreeHandlerCandidate<Object> e = node.parse("bob", null);
    assertNotNull(e);
    assertEquals(e.getHandler(), rootErrorHandler);
    assertTrue(
        e.getContext().getErrors().stream()
            .anyMatch(err -> err.getClass() == InvalidOptionError.class));
    assertEquals(0, fallbackHandler.count);
  }

  @Test
  void parse_13() {
    TestParserTreeHandler executeHandler = new TestParserTreeHandler();
    TestParserTreeHandler errorHandler = new TestParserTreeHandler();
    TestParserTreeFallbackHandler fallbackHandler = new TestParserTreeFallbackHandler();
    TestParserTreeHandler rootErrorHandler = new TestParserTreeHandler();

    ParserTree<Object> node =
        generator
            .from("literal1|literal2|literal3")
            .forEachLeaf(
                n -> n.execute(executeHandler).error(errorHandler).fallback(fallbackHandler))
            .error(rootErrorHandler);

    ParserTreeHandlerCandidate<Object> e = node.parse("literal1", null);
    assertNotNull(e);
    assertEquals(e.getHandler(), executeHandler);
    assertEquals(1, fallbackHandler.count);
  }

  @Test
  void parse_14() {
    TestParserTreeHandler executeHandler = new TestParserTreeHandler();
    TestParserTreeHandler errorHandler = new TestParserTreeHandler();
    TestParserTreeFallbackHandler fallbackHandler = new TestParserTreeFallbackHandler();
    TestParserTreeHandler rootErrorHandler = new TestParserTreeHandler();

    ParserTree<Object> node =
        generator
            .from("literal1 literal2(default=literal2) literal3(default=literal3)")
            .forEachLeaf(
                n -> n.execute(executeHandler).error(errorHandler).fallback(fallbackHandler))
            .error(rootErrorHandler);

    ParserTreeHandlerCandidate<Object> e = node.parse("literal1", null);
    assertNotNull(e);
    assertEquals(e.getHandler(), executeHandler);
    assertEquals(1, fallbackHandler.count);
  }

  @Test
  void multi_1() {
    TestParserTreeHandler rootErrorHandler = new TestParserTreeHandler();

    ParserTree<Object> node = new NullNode<>().error(rootErrorHandler);

    TestParserTreeHandler executeHandler1 = new TestParserTreeHandler();
    node.then(generator.from("milly|marta|mike").forEachLeaf(n -> n.execute(executeHandler1)));

    TestParserTreeHandler executeHandler2 = new TestParserTreeHandler();
    node.then(generator.from("alice|bob").forEachLeaf(n -> n.execute(executeHandler2)));

    ParserTreeHandlerCandidate<Object> e = node.parse("zoe", null);
    assertNotNull(e);
    assertEquals(e.getHandler(), rootErrorHandler);
    assertTrue(
        e.getContext().getErrors().stream()
            .anyMatch(err -> err.getClass() == InvalidOptionError.class));
  }

  @Test
  void multi_2() {
    TestParserTreeHandler rootErrorHandler = new TestParserTreeHandler();

    ParserTree<Object> node = new NullNode<>().error(rootErrorHandler);

    TestParserTreeHandler executeHandler1 = new TestParserTreeHandler();
    node.then(generator.from("milly|marta|mike").forEachLeaf(n -> n.execute(executeHandler1)));

    TestParserTreeHandler executeHandler2 = new TestParserTreeHandler();
    node.then(generator.from("alice|bob").forEachLeaf(n -> n.execute(executeHandler2)));

    ParserTreeHandlerCandidate<Object> e = node.parse("bob", null);
    assertNotNull(e);
    assertEquals(e.getHandler(), executeHandler2);
  }

  @Test
  void multi_3() {
    TestParserTreeHandler rootErrorHandler = new TestParserTreeHandler();

    ParserTree<Object> node = new NullNode<>().error(rootErrorHandler);

    TestParserTreeHandler executeHandler1 = new TestParserTreeHandler();
    node.then(generator.from("milly|marta|mike").forEachLeaf(n -> n.execute(executeHandler1)));

    TestParserTreeHandler executeHandler2 = new TestParserTreeHandler();
    node.then(generator.from("alice|bob").forEachLeaf(n -> n.execute(executeHandler2)));

    ParserTreeHandlerCandidate<Object> e = node.parse("marta", null);
    assertNotNull(e);
    assertEquals(e.getHandler(), executeHandler1);
  }

  @Test
  void multi_4() {
    TestParserTreeHandler rootErrorHandler = new TestParserTreeHandler();

    ParserTree<Object> node = new NullNode<>().error(rootErrorHandler);

    TestParserTreeHandler executeHandler1 = new TestParserTreeHandler();
    node.then(
        generator.from("milly|marta|mike peter").forEachLeaf(n -> n.execute(executeHandler1)));

    TestParserTreeHandler executeHandler2 = new TestParserTreeHandler();
    node.then(
        generator.from("alice|bob|mike charles").forEachLeaf(n -> n.execute(executeHandler2)));

    ParserTreeHandlerCandidate<Object> e = node.parse("mike", null);
    assertNotNull(e);
    assertEquals(e.getHandler(), rootErrorHandler);
    assertTrue(
        e.getContext().getErrors().stream()
            .anyMatch(err -> err.getClass() == InputExpectedError.class));
  }

  @Test
  void multi_5() {
    TestParserTreeHandler rootErrorHandler = new TestParserTreeHandler();

    ParserTree<Object> node = new NullNode<>().error(rootErrorHandler);

    TestParserTreeHandler executeHandler1 = new TestParserTreeHandler();
    node.then(
        generator.from("milly|marta|mike peter").forEachLeaf(n -> n.execute(executeHandler1)));

    TestParserTreeHandler executeHandler2 = new TestParserTreeHandler();
    node.then(
        generator
            .from("alice|bob|mike charles(default=charles)")
            .forEachLeaf(n -> n.execute(executeHandler2)));

    ParserTreeHandlerCandidate<Object> e = node.parse("mike peter", null);
    assertNotNull(e);
    assertEquals(e.getHandler(), executeHandler1);
  }

  @Test
  void multi_6() {
    TestParserTreeHandler rootErrorHandler = new TestParserTreeHandler();

    ParserTree<Object> node = new NullNode<>().error(rootErrorHandler);

    TestParserTreeHandler executeHandler1 = new TestParserTreeHandler();
    node.then(
        generator.from("milly|marta|mike peter").forEachLeaf(n -> n.execute(executeHandler1)));

    TestParserTreeHandler executeHandler2 = new TestParserTreeHandler();
    node.then(
        generator
            .from("alice|bob|mike charles(default=charles)")
            .forEachLeaf(n -> n.execute(executeHandler2)));

    ParserTreeHandlerCandidate<Object> e = node.parse("mike", null);
    assertNotNull(e);
    assertEquals(e.getHandler(), executeHandler2);
  }

  static class TestParserTreeHandler implements ParserTreeHandler<Object> {

    @Override
    public void handle(ParserTreeContext<Object> context) {}
  }

  static class TestParserTreeFallbackHandler implements ParserTreeFallbackHandler<Object> {
    public int count = 0;

    @Override
    public ParserTreeHandlerCandidate<Object> handle(ParserTreeContext<Object> context) {
      count++;
      return null;
    }
  }
}
