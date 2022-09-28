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

import au.com.grieve.bcf.CompleteContext;
import au.com.grieve.bcf.ErrorContext;
import au.com.grieve.bcf.ExecuteContext;
import au.com.grieve.bcf.Parser;
import au.com.grieve.bcf.ParserTree;
import au.com.grieve.bcf.ParserTreeContext;
import au.com.grieve.bcf.ParserTreeFallbackHandler;
import au.com.grieve.bcf.ParserTreeHandler;
import au.com.grieve.bcf.ParserTreeResult;
import au.com.grieve.bcf.impl.error.AmbiguousExecuteHandlersError;
import au.com.grieve.bcf.impl.error.DefaultErrorCollection;
import au.com.grieve.bcf.impl.error.InputExpectedError;
import au.com.grieve.bcf.impl.error.InvalidOptionError;
import au.com.grieve.bcf.impl.parser.IntegerParser;
import au.com.grieve.bcf.impl.parser.StringParser;
import au.com.grieve.bcf.impl.parsertree.generator.StringParserGenerator;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

class ParserNodeTest {

  StringParserGenerator<Void> generator =
      new StringParserGenerator<>(
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
              return (Parser<Void, ?>)
                  parserClass.getConstructor(Map.class).newInstance(parameters);
            } catch (InstantiationException
                | NoSuchMethodException
                | InvocationTargetException
                | IllegalAccessException e) {
              throw new RuntimeException(e);
            }
          });

  @Test
  void parse_1() {
    ParserTree<Void> node = generator.from("");

    ParserTreeResult<Void> e = node.parse("", null);
    assertNull(e.getExecuteCandidate());
  }

  @Test
  void parse_2() {
    ParserTree<Void> node = generator.from("");

    ParserTreeResult<Void> e = node.parse("bob", null);
    assertNull(e.getExecuteCandidate());
  }

  @Test
  void parse_3() {
    TestExecuteHandler executeHandler = new TestExecuteHandler();
    TestErrorHandler errorHandler = new TestErrorHandler();
    TestParserTreeFallbackHandler fallbackHandler = new TestParserTreeFallbackHandler();

    ParserTree<Void> node =
        generator
            .from("")
            .forEachLeaf(
                n -> n.execute(executeHandler).error(errorHandler).fallback(fallbackHandler));

    ParserTreeResult<Void> e = node.parse("bob", null);
    assertNull(e.getExecuteCandidate());
  }

  @Test
  void parse_4() {
    TestExecuteHandler executeHandler = new TestExecuteHandler();
    TestErrorHandler errorHandler = new TestErrorHandler();
    TestParserTreeFallbackHandler fallbackHandler = new TestParserTreeFallbackHandler();
    TestErrorHandler rootErrorHandler = new TestErrorHandler();

    ParserTree<Void> node =
        generator
            .from("literal1")
            .forEachLeaf(
                n -> n.execute(executeHandler).error(errorHandler).fallback(fallbackHandler))
            .error(rootErrorHandler);

    ParserTreeResult<Void> e = node.parse("", null);
    assertNotNull(e);
    assertNull(e.getExecuteCandidate());
    assertEquals(e.getErrorCandidate().getHandler(), rootErrorHandler);
    assertTrue(e.getErrors().stream().anyMatch(err -> err.getClass() == InputExpectedError.class));
    assertEquals(0, fallbackHandler.count);
  }

  @Test
  void parse_5() {
    TestExecuteHandler executeHandler = new TestExecuteHandler();
    TestErrorHandler errorHandler = new TestErrorHandler();
    TestParserTreeFallbackHandler fallbackHandler = new TestParserTreeFallbackHandler();
    TestErrorHandler rootErrorHandler = new TestErrorHandler();

    ParserTree<Void> node =
        generator
            .from("literal1")
            .forEachLeaf(
                n -> n.execute(executeHandler).error(errorHandler).fallback(fallbackHandler))
            .error(rootErrorHandler);

    ParserTreeResult<Void> e = node.parse("bob", null);
    assertNotNull(e);
    assertNull(e.getExecuteCandidate());
    assertEquals(e.getErrorCandidate().getHandler(), rootErrorHandler);
    assertTrue(e.getErrors().stream().anyMatch(err -> err.getClass() == InvalidOptionError.class));
    assertEquals(0, fallbackHandler.count);
  }

  @Test
  void parse_6() {
    TestExecuteHandler executeHandler = new TestExecuteHandler();
    TestErrorHandler errorHandler = new TestErrorHandler();
    TestParserTreeFallbackHandler fallbackHandler = new TestParserTreeFallbackHandler();
    TestErrorHandler rootErrorHandler = new TestErrorHandler();

    ParserTree<Void> node =
        generator
            .from("literal1")
            .forEachLeaf(
                n -> n.execute(executeHandler).error(errorHandler).fallback(fallbackHandler))
            .error(rootErrorHandler);

    ParserTreeResult<Void> e = node.parse("literal1", null);
    assertNotNull(e);
    assertEquals(e.getExecuteCandidate().getHandler(), executeHandler);
    assertEquals(1, fallbackHandler.count);
  }

  @Test
  void parse_7() {
    TestExecuteHandler executeHandler = new TestExecuteHandler();
    TestErrorHandler errorHandler = new TestErrorHandler();
    TestParserTreeFallbackHandler fallbackHandler = new TestParserTreeFallbackHandler();
    TestErrorHandler rootErrorHandler = new TestErrorHandler();

    ParserTree<Void> node =
        generator
            .from("literal1 literal2 literal3")
            .forEachLeaf(
                n -> n.execute(executeHandler).error(errorHandler).fallback(fallbackHandler))
            .error(rootErrorHandler);

    ParserTreeResult<Void> e = node.parse("literal1", null);
    assertNotNull(e);
    assertNull(e.getExecuteCandidate());
    assertEquals(e.getErrorCandidate().getHandler(), rootErrorHandler);
    assertTrue(e.getErrors().stream().anyMatch(err -> err.getClass() == InputExpectedError.class));
    assertEquals(0, fallbackHandler.count);
  }

  @Test
  void parse_8() {
    TestExecuteHandler executeHandler = new TestExecuteHandler();
    TestErrorHandler errorHandler = new TestErrorHandler();
    TestParserTreeFallbackHandler fallbackHandler = new TestParserTreeFallbackHandler();
    TestErrorHandler rootErrorHandler = new TestErrorHandler();

    ParserTree<Void> node =
        generator
            .from("literal1 literal2 literal3")
            .forEachLeaf(
                n -> n.execute(executeHandler).error(errorHandler).fallback(fallbackHandler))
            .error(rootErrorHandler);

    ParserTreeResult<Void> e = node.parse("literal1 bob", null);
    assertNotNull(e);
    assertNull(e.getExecuteCandidate());
    assertEquals(e.getErrorCandidate().getHandler(), rootErrorHandler);
    assertTrue(e.getErrors().stream().anyMatch(err -> err.getClass() == InvalidOptionError.class));
    assertEquals(0, fallbackHandler.count);
  }

  @Test
  void parse_9() {
    TestExecuteHandler executeHandler = new TestExecuteHandler();
    TestErrorHandler errorHandler = new TestErrorHandler();
    TestParserTreeFallbackHandler fallbackHandler = new TestParserTreeFallbackHandler();
    TestErrorHandler rootErrorHandler = new TestErrorHandler();

    ParserTree<Void> node =
        generator
            .from("literal1 literal2 literal3")
            .forEachLeaf(
                n -> n.execute(executeHandler).error(errorHandler).fallback(fallbackHandler))
            .error(rootErrorHandler);

    ParserTreeResult<Void> e = node.parse("literal1 literal2", null);
    assertNotNull(e);
    assertNull(e.getExecuteCandidate());
    assertEquals(e.getErrorCandidate().getHandler(), rootErrorHandler);
    assertTrue(e.getErrors().stream().anyMatch(err -> err.getClass() == InputExpectedError.class));
    assertEquals(0, fallbackHandler.count);
  }

  @Test
  void parse_10() {
    TestExecuteHandler executeHandler = new TestExecuteHandler();
    TestErrorHandler errorHandler = new TestErrorHandler();
    TestParserTreeFallbackHandler fallbackHandler = new TestParserTreeFallbackHandler();
    TestErrorHandler rootErrorHandler = new TestErrorHandler();

    ParserTree<Void> node =
        generator
            .from("literal1 literal2 literal3")
            .forEachLeaf(
                n -> n.execute(executeHandler).error(errorHandler).fallback(fallbackHandler))
            .error(rootErrorHandler);

    ParserTreeResult<Void> e = node.parse("literal1 literal2 literal3", null);
    assertNotNull(e);
    assertEquals(e.getExecuteCandidate().getHandler(), executeHandler);
    assertEquals(1, fallbackHandler.count);
  }

  @Test
  void parse_11() {
    TestExecuteHandler executeHandler = new TestExecuteHandler();
    TestErrorHandler errorHandler = new TestErrorHandler();
    TestParserTreeFallbackHandler fallbackHandler = new TestParserTreeFallbackHandler();
    TestErrorHandler rootErrorHandler = new TestErrorHandler();

    ParserTree<Void> node =
        generator
            .from("literal1 literal2 literal3")
            .forEachLeaf(
                n -> n.execute(executeHandler).error(errorHandler).fallback(fallbackHandler))
            .error(rootErrorHandler);

    ParserTreeResult<Void> e = node.parse("literal1 literal2 literal3 bob", null);
    assertNull(e.getExecuteCandidate());
    assertEquals(1, fallbackHandler.count);
  }

  @Test
  void parse_12() {
    TestExecuteHandler executeHandler = new TestExecuteHandler();
    TestErrorHandler errorHandler = new TestErrorHandler();
    TestParserTreeFallbackHandler fallbackHandler = new TestParserTreeFallbackHandler();
    TestErrorHandler rootErrorHandler = new TestErrorHandler();

    ParserTree<Void> node =
        generator
            .from("literal1|literal2|literal3")
            .forEachLeaf(
                n -> n.execute(executeHandler).error(errorHandler).fallback(fallbackHandler))
            .error(rootErrorHandler);

    ParserTreeResult<Void> e = node.parse("bob", null);
    assertNotNull(e);
    assertNull(e.getExecuteCandidate());
    assertEquals(e.getErrorCandidate().getHandler(), rootErrorHandler);
    assertTrue(e.getErrors().stream().anyMatch(err -> err.getClass() == InvalidOptionError.class));
    assertEquals(0, fallbackHandler.count);
  }

  @Test
  void parse_13() {
    TestExecuteHandler executeHandler = new TestExecuteHandler();
    TestErrorHandler errorHandler = new TestErrorHandler();
    TestParserTreeFallbackHandler fallbackHandler = new TestParserTreeFallbackHandler();
    TestErrorHandler rootErrorHandler = new TestErrorHandler();

    ParserTree<Void> node =
        generator
            .from("literal1|literal2|literal3")
            .forEachLeaf(
                n -> n.execute(executeHandler).error(errorHandler).fallback(fallbackHandler))
            .error(rootErrorHandler);

    ParserTreeResult<Void> e = node.parse("literal1", null);
    assertNotNull(e);
    assertEquals(e.getExecuteCandidate().getHandler(), executeHandler);
    assertEquals(1, fallbackHandler.count);
  }

  @Test
  void parse_14() {
    TestExecuteHandler executeHandler = new TestExecuteHandler();
    TestErrorHandler errorHandler = new TestErrorHandler();
    TestParserTreeFallbackHandler fallbackHandler = new TestParserTreeFallbackHandler();
    TestErrorHandler rootErrorHandler = new TestErrorHandler();

    ParserTree<Void> node =
        generator
            .from("literal1 literal2(default=literal2) literal3(default=literal3)")
            .forEachLeaf(
                n -> n.execute(executeHandler).error(errorHandler).fallback(fallbackHandler))
            .error(rootErrorHandler);

    ParserTreeResult<Void> e = node.parse("literal1", null);
    assertNotNull(e);
    assertEquals(e.getExecuteCandidate().getHandler(), executeHandler);
    assertEquals(1, fallbackHandler.count);
  }

  @Test
  void multi_1() {
    TestErrorHandler rootErrorHandler = new TestErrorHandler();

    ParserTree<Void> node = new NullNode<Void>().error(rootErrorHandler);

    TestExecuteHandler executeHandler1 = new TestExecuteHandler();
    node.then(generator.from("milly|marta|mike").forEachLeaf(n -> n.execute(executeHandler1)));

    TestExecuteHandler executeHandler2 = new TestExecuteHandler();
    node.then(generator.from("alice|bob").forEachLeaf(n -> n.execute(executeHandler2)));

    ParserTreeResult<Void> e = node.parse("zoe", null);
    assertNotNull(e);
    assertNull(e.getExecuteCandidate());
    assertEquals(e.getErrorCandidate().getHandler(), rootErrorHandler);
    assertTrue(e.getErrors().stream().anyMatch(err -> err.getClass() == InvalidOptionError.class));
  }

  @Test
  void multi_2() {
    TestErrorHandler rootErrorHandler = new TestErrorHandler();

    ParserTree<Void> node = new NullNode<Void>().error(rootErrorHandler);

    TestExecuteHandler executeHandler1 = new TestExecuteHandler();
    node.then(generator.from("milly|marta|mike").forEachLeaf(n -> n.execute(executeHandler1)));

    TestExecuteHandler executeHandler2 = new TestExecuteHandler();
    node.then(generator.from("alice|bob").forEachLeaf(n -> n.execute(executeHandler2)));

    ParserTreeResult<Void> e = node.parse("bob", null);
    assertNotNull(e);
    assertEquals(e.getExecuteCandidate().getHandler(), executeHandler2);
  }

  @Test
  void multi_3() {
    TestErrorHandler rootErrorHandler = new TestErrorHandler();

    ParserTree<Void> node = new NullNode<Void>().error(rootErrorHandler);

    TestExecuteHandler executeHandler1 = new TestExecuteHandler();
    node.then(generator.from("milly|marta|mike").forEachLeaf(n -> n.execute(executeHandler1)));

    TestExecuteHandler executeHandler2 = new TestExecuteHandler();
    node.then(generator.from("alice|bob").forEachLeaf(n -> n.execute(executeHandler2)));

    ParserTreeResult<Void> e = node.parse("marta", null);
    assertNotNull(e);
    assertEquals(e.getExecuteCandidate().getHandler(), executeHandler1);
  }

  @Test
  void multi_4() {
    TestErrorHandler rootErrorHandler = new TestErrorHandler();

    ParserTree<Void> node = new NullNode<Void>().error(rootErrorHandler);

    TestExecuteHandler executeHandler1 = new TestExecuteHandler();
    node.then(
        generator.from("milly|marta|mike peter").forEachLeaf(n -> n.execute(executeHandler1)));

    TestExecuteHandler executeHandler2 = new TestExecuteHandler();
    node.then(
        generator.from("alice|bob|mike charles").forEachLeaf(n -> n.execute(executeHandler2)));

    ParserTreeResult<Void> e = node.parse("mike", null);
    assertNotNull(e);
    assertNull(e.getExecuteCandidate());
    assertEquals(e.getErrorCandidate().getHandler(), rootErrorHandler);
    assertTrue(e.getErrors().stream().anyMatch(err -> err.getClass() == InputExpectedError.class));
  }

  @Test
  void multi_5() {
    TestErrorHandler rootErrorHandler = new TestErrorHandler();

    ParserTree<Void> node = new NullNode<Void>().error(rootErrorHandler);

    TestExecuteHandler executeHandler1 = new TestExecuteHandler();
    node.then(
        generator.from("milly|marta|mike peter").forEachLeaf(n -> n.execute(executeHandler1)));

    TestExecuteHandler executeHandler2 = new TestExecuteHandler();
    node.then(
        generator
            .from("alice|bob|mike charles(default=charles)")
            .forEachLeaf(n -> n.execute(executeHandler2)));

    ParserTreeResult<Void> e = node.parse("mike peter", null);
    assertNotNull(e);
    assertEquals(e.getExecuteCandidate().getHandler(), executeHandler1);
  }

  @Test
  void multi_6() {
    TestErrorHandler rootErrorHandler = new TestErrorHandler();

    ParserTree<Void> node = new NullNode<Void>().error(rootErrorHandler);

    TestExecuteHandler executeHandler1 = new TestExecuteHandler();
    node.then(
        generator.from("milly|marta|mike peter").forEachLeaf(n -> n.execute(executeHandler1)));

    TestExecuteHandler executeHandler2 = new TestExecuteHandler();
    node.then(
        generator
            .from("alice|bob|mike charles(default=charles)")
            .forEachLeaf(n -> n.execute(executeHandler2)));

    ParserTreeResult<Void> e = node.parse("mike", null);
    assertNotNull(e);
    assertEquals(e.getExecuteCandidate().getHandler(), executeHandler2);
  }

  @Test
  void results_1() {
    TestErrorHandler rootErrorHandler = new TestErrorHandler();

    ParserTree<Void> node = new NullNode<Void>().error(rootErrorHandler);

    TestExecuteHandler executeHandler1 = new TestExecuteHandler();
    node.then(
        generator
            .from("milly|marta|mike(suppress=false) peter(suppress=false)")
            .forEachLeaf(n -> n.execute(executeHandler1)));

    TestExecuteHandler executeHandler2 = new TestExecuteHandler();
    node.then(
        generator
            .from("alice|bob|mike(suppress=false) charles(suppress=false, default=charles)")
            .forEachLeaf(n -> n.execute(executeHandler2)));

    ParserTreeResult<Void> e = node.parse("mike", null);
    assertNotNull(e);
    assertEquals(e.getExecuteCandidate().getHandler(), executeHandler2);
    assertEquals(2, e.getExecuteCandidate().getResults().size());
    assertEquals("mike", e.getExecuteCandidate().getResults().get(0));
    assertEquals("charles", e.getExecuteCandidate().getResults().get(1));
  }

  @Test
  void results_2() {
    TestErrorHandler rootErrorHandler = new TestErrorHandler();

    ParserTree<Void> node = new NullNode<Void>().error(rootErrorHandler);

    TestExecuteHandler executeHandler1 = new TestExecuteHandler();
    node.then(
        generator
            .from(
                "milly|marta|mike(suppress=false) @int(min=3, max=13) peter(suppress=false,default=peter)")
            .forEachLeaf(n -> n.execute(executeHandler1)));

    TestExecuteHandler executeHandler2 = new TestExecuteHandler();
    node.then(
        generator
            .from(
                "alice|bob|mike(suppress=false) @int(min=1,max=10) charles(suppress=false,default=charles)")
            .forEachLeaf(n -> n.execute(executeHandler2)));

    ParserTreeResult<Void> e = node.parse("mike 1", null);
    assertNotNull(e);
    assertEquals(e.getExecuteCandidate().getHandler(), executeHandler2);
    assertEquals(3, e.getExecuteCandidate().getResults().size());
    assertEquals("mike", e.getExecuteCandidate().getResults().get(0));
    assertEquals(1, e.getExecuteCandidate().getResults().get(1));
    assertEquals("charles", e.getExecuteCandidate().getResults().get(2));
  }

  @Test
  void results_3() {
    TestErrorHandler rootErrorHandler = new TestErrorHandler();

    ParserTree<Void> node = new NullNode<Void>().error(rootErrorHandler);

    TestExecuteHandler executeHandler1 = new TestExecuteHandler();
    node.then(
        generator
            .from(
                "milly|marta|mike(suppress=false) @int(min=3, max=13) peter(suppress=false,default=peter)")
            .forEachLeaf(n -> n.execute(executeHandler1)));

    TestExecuteHandler executeHandler2 = new TestExecuteHandler();
    node.then(
        generator
            .from(
                "alice|bob|mike(suppress=false) @int(min=1,max=10) charles(suppress=false,default=charles)")
            .forEachLeaf(n -> n.execute(executeHandler2)));

    ParserTreeResult<Void> e = node.parse("mike 11", null);
    assertNotNull(e);
    assertEquals(e.getExecuteCandidate().getHandler(), executeHandler1);
    assertEquals(3, e.getExecuteCandidate().getResults().size());
    assertEquals("mike", e.getExecuteCandidate().getResults().get(0));
    assertEquals(11, e.getExecuteCandidate().getResults().get(1));
    assertEquals("peter", e.getExecuteCandidate().getResults().get(2));
  }

  @Test
  void results_4() {
    TestErrorHandler rootErrorHandler = new TestErrorHandler();

    ParserTree<Void> node = new NullNode<Void>().error(rootErrorHandler);

    TestExecuteHandler executeHandler1 = new TestExecuteHandler();
    node.then(
        generator
            .from(
                "milly|marta|mike(suppress=false) @int(min=3, max=13) peter(suppress=false,default=peter)")
            .forEachLeaf(n -> n.execute(executeHandler1)));

    TestExecuteHandler executeHandler2 = new TestExecuteHandler();
    node.then(
        generator
            .from(
                "alice|bob|mike(suppress=false) @int(min=1,max=10) charles(suppress=false,default=charles)")
            .forEachLeaf(n -> n.execute(executeHandler2)));

    ParserTreeResult<Void> e = node.parse("mike 111", null);
    assertNotNull(e);
    assertNull(e.getExecuteCandidate());
    assertEquals(e.getErrorCandidate().getHandler(), rootErrorHandler);
  }

  /** No Handler, so all completions should return */
  @Test
  void completeHandler_1() {
    TestErrorHandler rootErrorHandler = new TestErrorHandler();

    ParserTree<Void> node = new NullNode<Void>().error(rootErrorHandler);

    TestExecuteHandler executeHandler1 = new TestExecuteHandler();
    node.then(
        generator
            .from(
                "milly|marta|mike(suppress=false) @int(min=3, max=13) peter(suppress=false,default=peter)")
            .forEachLeaf(n -> n.execute(executeHandler1)));

    TestExecuteHandler executeHandler2 = new TestExecuteHandler();
    node.then(
        generator
            .from("alice|bob|mike(suppress=false)")
            .forEachLeaf(
                n ->
                    n.then(
                        generator
                            .from("@int(min=1,max=10) charles(suppress=false,default=charles)")
                            .forEachLeaf(n2 -> n2.execute(executeHandler2)))));

    ParserTreeResult<Void> e = node.parse("mike 111", null);
    assertNotNull(e);
    assertNull(e.getExecuteCandidate());
    assertEquals(e.getErrorCandidate().getHandler(), rootErrorHandler);
    assertNull(e.getCompleteCandidate());
    assertEquals(2, e.getCompletions().size());
    assertEquals(
        21, e.getCompletions().stream().mapToLong(c -> c.getCompletionCandidates().size()).sum());
  }

  /** Complete handler exists so only completions under it should be returned */
  @Test
  void completeHandler_2() {
    TestErrorHandler rootErrorHandler = new TestErrorHandler();

    ParserTree<Void> node = new NullNode<Void>().error(rootErrorHandler);

    TestExecuteHandler executeHandler1 = new TestExecuteHandler();
    node.then(
        generator
            .from(
                "milly|marta|mike(suppress=false) @int(min=3, max=13) peter(suppress=false,default=peter)")
            .forEachLeaf(n -> n.execute(executeHandler1)));

    TestExecuteHandler executeHandler2 = new TestExecuteHandler();
    TestCompleteHandler completeHandler = new TestCompleteHandler();
    node.then(
        generator
            .from("alice|bob|mike(suppress=false)")
            .forEachLeaf(
                n ->
                    n.complete(completeHandler)
                        .then(
                            generator
                                .from("@int(min=1,max=10) charles(suppress=false,default=charles)")
                                .forEachLeaf(n2 -> n2.execute(executeHandler2)))));

    ParserTreeResult<Void> e = node.parse("mike 111", null);
    assertNotNull(e);

    assertNull(e.getExecuteCandidate());
    assertEquals(e.getErrorCandidate().getHandler(), rootErrorHandler);
    assertEquals(e.getCompleteCandidate().getHandler(), completeHandler);
    assertEquals(1, e.getCompletions().size());
    assertEquals(
        10, e.getCompletions().stream().mapToLong(c -> c.getCompletionCandidates().size()).sum());
  }

  @Test
  void ambiguousExecute_1() {
    TestErrorHandler rootErrorHandler = new TestErrorHandler();

    ParserTree<Void> node = new NullNode<Void>().error(rootErrorHandler);

    TestExecuteHandler executeHandler1 = new TestExecuteHandler();
    TestExecuteHandler executeHandler2 = new TestExecuteHandler();
    node.then(
            generator
                .from("milly|marta|mike(suppress=false) @int(min=3, max=13) peter(suppress=false)")
                .forEachLeaf(n -> n.execute(executeHandler1)))
        .then(
            generator
                .from("alice|bob|mike(suppress=false) @int(min=1,max=7) peter|mark(suppress=false)")
                .forEachLeaf(n -> n.execute(executeHandler2)));

    ParserTreeResult<Void> e = node.parse("mike 5 peter", null);
    assertNotNull(e);
    assertNull(e.getExecuteCandidate());
    assertEquals(e.getErrorCandidate().getHandler(), rootErrorHandler);
    assertTrue(
        e.getErrors().stream()
            .anyMatch(err -> err.getClass() == AmbiguousExecuteHandlersError.class));
  }

  static class TestExecuteHandler implements ParserTreeHandler<ExecuteContext<Void>> {

    @Override
    public void handle(ExecuteContext<Void> context) {}
  }

  static class TestErrorHandler implements ParserTreeHandler<ErrorContext<Void>> {

    @Override
    public void handle(ErrorContext<Void> context) {}
  }

  static class TestCompleteHandler implements ParserTreeHandler<CompleteContext<Void>> {

    @Override
    public void handle(CompleteContext<Void> context) {}
  }

  static class TestParserTreeFallbackHandler implements ParserTreeFallbackHandler<Void> {
    public int count = 0;

    @Override
    public @NotNull ParserTreeResult<Void> handle(ParserTreeContext<Void> context) {
      count++;
      return new ParserTreeResult<>(
          null, null, null, new DefaultErrorCollection(), new ArrayList<>());
    }
  }
}
