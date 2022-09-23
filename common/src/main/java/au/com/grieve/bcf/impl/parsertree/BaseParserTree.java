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

import au.com.grieve.bcf.ParsedLine;
import au.com.grieve.bcf.ParserTree;
import au.com.grieve.bcf.ParserTreeContext;
import au.com.grieve.bcf.ParserTreeFallbackHandler;
import au.com.grieve.bcf.ParserTreeHandler;
import au.com.grieve.bcf.ParserTreeHandlerCandidate;
import au.com.grieve.bcf.exception.EndOfLineException;
import au.com.grieve.bcf.impl.error.InputExpectedError;
import au.com.grieve.bcf.impl.line.DefaultParsedLine;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseParserTree<DATA> implements ParserTree<DATA> {
  protected final Collection<ParserTree<DATA>> children = new HashSet<>();
  protected ParserTreeHandler<DATA> executeHandler;
  protected ParserTreeHandler<DATA> errorHandler;
  protected ParserTreeFallbackHandler<DATA> fallbackHandler;

  /**
   * Add a child node
   *
   * @param node Node to add
   * @return Ourself
   */
  @Override
  public ParserTree<DATA> then(ParserTree<DATA> node) {
    if (node != null) {
      children.add(node);
    }
    return this;
  }

  @Override
  public ParserTree<DATA> execute(ParserTreeHandler<DATA> handler) {
    executeHandler = handler;
    return this;
  }

  @Override
  public ParserTree<DATA> error(ParserTreeHandler<DATA> handler) {
    errorHandler = handler;
    return this;
  }

  @Override
  public ParserTree<DATA> fallback(ParserTreeFallbackHandler<DATA> handler) {
    fallbackHandler = handler;
    return this;
  }

  @Override
  public ParserTreeHandlerCandidate<DATA> parse(ParsedLine line, DATA data) {
    DefaultParserTreeContext<DATA> context = new DefaultParserTreeContext<>(line, data);
    try {
      return parse(context);
    } catch (EndOfLineException e) {

      if (executeHandler == null) {
        context.getErrors().add(new InputExpectedError(), context.getLine(), context.getWeight());
        return errorHandler != null
            ? new ParserTreeHandlerCandidate<>(context, errorHandler, context.getWeight())
            : null;
      }
      return new ParserTreeHandlerCandidate<>(context, executeHandler, context.getWeight());
    }
  }

  @Override
  public ParserTreeHandlerCandidate<DATA> parse(String line, DATA data) {
    return parse(new DefaultParsedLine(line), data);
  }

  @Override
  public ParserTreeHandlerCandidate<DATA> parse(ParserTreeContext<DATA> context)
      throws EndOfLineException {

    ParserTreeHandlerCandidate<DATA> childCandidate =
        Stream.of(parseChildren(context), parseFallback(context))
            .filter(Objects::nonNull)
            .max(Comparator.comparingInt(ParserTreeHandlerCandidate::getWeight))
            .orElse(null);

    if (childCandidate != null) {
      return childCandidate;
    }

    // Errors? Return an error candidate if possible, else we don't return anything
    if (context.getErrors().size() > 0) {
      return errorHandler != null
          ? new ParserTreeHandlerCandidate<>(context, errorHandler, context.getWeight())
          : null;
    }

    // Finally if we are at EOL and have an execute handler we return that
    if (context.getLine().isEol()) {
      return executeHandler != null
          ? new ParserTreeHandlerCandidate<>(context, executeHandler, context.getWeight())
          : null;
    }

    return null;
  }

  protected ParserTreeHandlerCandidate<DATA> parseChildren(ParserTreeContext<DATA> context) {
    context.setWeight(context.getWeight() + 1);
    return children.stream()
        .map(
            c -> {
              ParserTreeContext<DATA> childContext = context.copy();
              childContext.getErrors().clear();
              ParserTreeHandlerCandidate<DATA> candidate = null;
              try {
                candidate = c.parse(childContext);
                context.getErrors().addAll(childContext.getErrors());
              } catch (EndOfLineException e) {
                if (executeHandler == null) {
                  context
                      .getErrors()
                      .add(new InputExpectedError(), context.getLine(), context.getWeight());
                }
              }
              return candidate;
            })
        .filter(Objects::nonNull)
        .max(Comparator.comparingInt(ParserTreeHandlerCandidate::getWeight))
        .orElse(null);
  }

  protected ParserTreeHandlerCandidate<DATA> parseFallback(ParserTreeContext<DATA> context) {
    return fallbackHandler != null ? fallbackHandler.handle(context) : null;
  }

  @Override
  public ParserTree<DATA> forEachLeaf(Consumer<ParserTree<DATA>> consumer) {
    leafs().forEach(consumer);
    return this;
  }

  @Override
  public Collection<ParserTree<DATA>> leafs() {
    if (children.isEmpty()) {
      return Collections.singleton(this);
    }
    return children.stream().flatMap(c -> c.leafs().stream()).collect(Collectors.toSet());
  }
}
