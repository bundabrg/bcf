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

import au.com.grieve.bcf.ParserTree;
import au.com.grieve.bcf.ParserTreeContext;
import au.com.grieve.bcf.ParserTreeFallbackHandler;
import au.com.grieve.bcf.ParserTreeHandler;
import au.com.grieve.bcf.ParserTreeHandlerCandidate;
import au.com.grieve.bcf.exception.EndOfLineException;
import au.com.grieve.bcf.impl.error.DefaultErrorCandidate;
import au.com.grieve.bcf.impl.error.InputExpectedError;
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
  public ParserTreeHandlerCandidate<DATA> parse(ParserTreeContext<DATA> context)
      throws EndOfLineException {
    context.setWeight(context.getWeight() + 1);
    return Stream.of(parseChildren(context.copy()), parseFallback(context.copy()))
        .filter(Objects::nonNull)
        .max(Comparator.comparingInt(ParserTreeHandlerCandidate::getWeight))
        .orElse(null);
  }

  protected ParserTreeHandlerCandidate<DATA> parseChildren(ParserTreeContext<DATA> context) {
    return children.stream()
        .map(
            c -> {
              ParserTreeContext<DATA> childContext = context.copy();
              try {
                ParserTreeHandlerCandidate<DATA> candidate = c.parse(childContext);
                context.getErrors().clear();
                context.getErrors().addAll(childContext.getErrors());
                return candidate;
              } catch (EndOfLineException e) {
                if (executeHandler != null) {
                  return new ParserTreeHandlerCandidate<>(
                      childContext, executeHandler, context.getWeight());
                } else {
                  context
                      .getErrors()
                      .add(
                          new DefaultErrorCandidate(
                              context.getLine(), new InputExpectedError(), context.getWeight()));
                  return errorHandler != null
                      ? new ParserTreeHandlerCandidate<>(context, errorHandler, context.getWeight())
                      : null;
                }
              }
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
