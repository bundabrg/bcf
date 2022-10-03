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

import au.com.grieve.bcf.CommandErrorCollection;
import au.com.grieve.bcf.CompleteContext;
import au.com.grieve.bcf.CompletionCandidateGroup;
import au.com.grieve.bcf.ErrorContext;
import au.com.grieve.bcf.ExecuteContext;
import au.com.grieve.bcf.ParsedLine;
import au.com.grieve.bcf.ParserTree;
import au.com.grieve.bcf.ParserTreeCandidate;
import au.com.grieve.bcf.ParserTreeContext;
import au.com.grieve.bcf.ParserTreeFallbackHandler;
import au.com.grieve.bcf.ParserTreeHandler;
import au.com.grieve.bcf.ParserTreeResult;
import au.com.grieve.bcf.RequiresContext;
import au.com.grieve.bcf.Result;
import au.com.grieve.bcf.exception.EndOfLineException;
import au.com.grieve.bcf.exception.ResultNotSetException;
import au.com.grieve.bcf.impl.error.AmbiguousExecuteHandlersError;
import au.com.grieve.bcf.impl.error.DefaultErrorCollection;
import au.com.grieve.bcf.impl.error.InputExpectedError;
import au.com.grieve.bcf.impl.error.UnexpectedInputError;
import au.com.grieve.bcf.impl.line.DefaultParsedLine;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.ToString;

@ToString
public abstract class BaseParserTree<DATA> implements ParserTree<DATA> {
  @ToString.Exclude protected final Set<ParserTree<DATA>> children = new HashSet<>();
  protected ParserTreeHandler<ExecuteContext<DATA>> executeHandler;
  protected ParserTreeHandler<ErrorContext<DATA>> errorHandler;
  protected ParserTreeHandler<CompleteContext<DATA>> completeHandler;
  protected ParserTreeFallbackHandler<DATA> fallbackHandler;
  protected Predicate<RequiresContext<DATA>> requirement;

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
  public ParserTree<DATA> remove(ParserTree<DATA> node) {
    children.remove(node);
    return this;
  }

  @Override
  public ParserTree<DATA> execute(ParserTreeHandler<ExecuteContext<DATA>> handler) {
    executeHandler = handler;
    return this;
  }

  @Override
  public ParserTree<DATA> complete(ParserTreeHandler<CompleteContext<DATA>> handler) {
    completeHandler = handler;
    return this;
  }

  @Override
  public ParserTree<DATA> error(ParserTreeHandler<ErrorContext<DATA>> handler) {
    errorHandler = handler;
    return this;
  }

  @Override
  public ParserTree<DATA> fallback(ParserTreeFallbackHandler<DATA> handler) {
    fallbackHandler = handler;
    return this;
  }

  @Override
  public ParserTree<DATA> requires(Predicate<RequiresContext<DATA>> requirement) {
    this.requirement = requirement;
    return this;
  }

  @Override
  public @NonNull ParserTreeResult<DATA> parse(ParsedLine line, DATA data) {
    DefaultParserTreeContext<DATA> context = new DefaultParserTreeContext<>(line, data);
    return parse(context);
  }

  @Override
  public @NonNull ParserTreeResult<DATA> parse(String line, DATA data) {
    return parse(new DefaultParsedLine(line), data);
  }

  protected @NonNull ParserTreeResult<DATA> parseHere(ParserTreeContext<DATA> context) {
    return ParserTreeResult.EMPTY_RESULT();
  }

  @Override
  public @NonNull ParserTreeResult<DATA> parse(ParserTreeContext<DATA> context) {
    final List<CompletionCandidateGroup> completions = new ArrayList<>();
    final CommandErrorCollection errors = new DefaultErrorCollection();

    // If a requirement is set, and we fail it then process things differently
    if (requirement != null
        && !requirement.test(
            new RequiresContext<>(context.getLine(), context.getResults(), context.getData()))) {

      // If we have more input, then add an error
      if (!context.getLine().isEol()) {
        errors.add(new UnexpectedInputError(), context.getLine(), context.getWeight());
      }
      return new ParserTreeResult<>(null, null, null, errors, completions);
    }

    ParserTreeResult<DATA> result = parseHere(context);
    completions.addAll(result.getCompletions());
    errors.addAll(result.getErrors());

    if (result.getErrors().size() > 0) {
      return result;
    }

    ParserTreeResult<DATA> childResult = parseChildren(context);
    completions.addAll(childResult.getCompletions());
    errors.addAll(childResult.getErrors());

    result =
        new ParserTreeResult<>(
            childResult.getExecuteCandidate() != null
                ? childResult.getExecuteCandidate()
                : result.getExecuteCandidate(),
            childResult.getErrorCandidate() != null
                ? childResult.getErrorCandidate()
                : result.getErrorCandidate(),
            childResult.getCompleteCandidate() != null
                ? childResult.getCompleteCandidate()
                : result.getCompleteCandidate(),
            errors,
            completions);
    //      // If we have more input, then add an error
    //      if (!context.getLine().isEol()) {
    //        errors.add(new UnexpectedInputError(), context.getLine(), context.getWeight());
    //      }

    //    final List<CompletionCandidateGroup> completions = new ArrayList<>();
    //    final CommandErrorCollection errors = new DefaultErrorCollection();

    ParserTreeCandidate<ExecuteContext<DATA>, DATA> executeCandidate = result.getExecuteCandidate();
    ParserTreeCandidate<ErrorContext<DATA>, DATA> errorCandidate = result.getErrorCandidate();
    ParserTreeCandidate<CompleteContext<DATA>, DATA> completeCandidate =
        result.getCompleteCandidate();

    if (context.getLine().isEol()) {
      if (executeCandidate == null && executeHandler != null) {
        try {
          executeCandidate =
              new ParserTreeCandidate<>(
                  context.getLine(),
                  executeHandler,
                  context.getResults().toObjects(),
                  errors,
                  completions,
                  context.getData(),
                  context.getWeight());
        } catch (ResultNotSetException e) {
          errors.addAll(e.getErrors());
        }
      } else {
        errors.add(new InputExpectedError(), context.getLine(), context.getWeight());
      }
    }

    if (errorCandidate == null && errorHandler != null && errors.size() > 0) {
      try {
        errorCandidate =
            new ParserTreeCandidate<>(
                context.getLine(),
                errorHandler,
                context.getResults().toObjects(),
                errors,
                completions,
                context.getData(),
                context.getWeight());
      } catch (ResultNotSetException e) {
        errors.addAll(e.getErrors());
      }
    }

    // Completions
    if (completeCandidate == null && completeHandler != null) {
      try {
        completeCandidate =
            new ParserTreeCandidate<>(
                context.getLine(),
                completeHandler,
                context.getResults().toObjects(),
                errors,
                completions,
                context.getData(),
                context.getWeight());
      } catch (ResultNotSetException e) {
        errors.addAll(e.getErrors());
      }
    }

    return new ParserTreeResult<>(
        executeCandidate, errorCandidate, completeCandidate, errors, completions);
  }

  protected @NonNull List<ParserTreeResult<DATA>> parseFutureResults(
      ParserTreeContext<DATA> context) {
    // Look backwards through output for a non-completed future result
    Map<Result, List<ParserTreeResult<DATA>>> data = new HashMap<>();
    int wordIndex;
    do {
      wordIndex = context.getLine().getWordIndex();
      context.getResults().stream()
          .collect(
              Collectors.collectingAndThen(
                  Collectors.toList(),
                  l -> {
                    Collections.reverse(l);
                    return l;
                  }))
          .stream()
          .filter(r -> !r.isSet())
          .filter(r -> r instanceof FutureResult)
          .forEach(
              r -> {
                //noinspection unchecked
                data.computeIfAbsent(r, k -> new ArrayList<>())
                    .add(((FutureResult<DATA>) r).handle(context));
              });
    } while (wordIndex != context.getLine().getWordIndex());

    return data.entrySet().stream()
        .flatMap(d -> d.getValue().stream())
        .collect(Collectors.toList());
  }

  protected @NonNull ParserTreeResult<DATA> parseChildren(ParserTreeContext<DATA> context) {
    context.setWeight(context.getWeight() + 1);
    final List<CompletionCandidateGroup> completions = new ArrayList<>();
    final CommandErrorCollection errors = new DefaultErrorCollection();

    List<ParserTreeResult<DATA>> results =
        Stream.concat(
                parseFutureResults(context).stream(),
                Stream.concat(
                    children.stream().map(c -> c.parse(context.copy())),
                    Stream.of(parseFallback(context.copy()))))
            .collect(Collectors.toList());

    if (results.size() == 1 && !context.getLine().isEol()) {
      try {
        ParsedLine lineCopy = context.getLine().copy();
        lineCopy.next();
        errors.add(new UnexpectedInputError(), lineCopy, context.getWeight());
      } catch (EndOfLineException ignored) {
      }
    }

    // Extract errors and Completions
    results.forEach(
        r -> {
          completions.addAll(r.getCompletions());
          errors.addAll(r.getErrors());
        });

    // Separate everything out
    Map<ParserTreeCandidate<ExecuteContext<DATA>, DATA>, ParserTreeResult<DATA>> executeCandidates =
        results.stream()
            .filter(r -> r.getExecuteCandidate() != null)
            .collect(Collectors.toMap(ParserTreeResult::getExecuteCandidate, r -> r));
    Map<ParserTreeCandidate<ErrorContext<DATA>, DATA>, ParserTreeResult<DATA>> errorCandidates =
        results.stream()
            .filter(r -> r.getErrorCandidate() != null)
            .collect(Collectors.toMap(ParserTreeResult::getErrorCandidate, r -> r));
    Map<ParserTreeCandidate<CompleteContext<DATA>, DATA>, ParserTreeResult<DATA>>
        completeCandidates =
            results.stream()
                .filter(r -> r.getCompleteCandidate() != null)
                .collect(Collectors.toMap(ParserTreeResult::getCompleteCandidate, r -> r));

    // Find heaviest
    int heaviestExecute =
        executeCandidates.keySet().stream()
            .map(ParserTreeCandidate::getWeight)
            .max(Integer::compare)
            .orElse(0);
    int heaviestError =
        errorCandidates.keySet().stream()
            .map(ParserTreeCandidate::getWeight)
            .max(Integer::compare)
            .orElse(0);
    int heaviestComplete =
        completeCandidates.keySet().stream()
            .map(ParserTreeCandidate::getWeight)
            .max(Integer::compare)
            .orElse(0);

    // Remove all lighter candidates
    executeCandidates =
        executeCandidates.entrySet().stream()
            .filter(e -> e.getKey().getWeight() == heaviestExecute)
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    errorCandidates =
        errorCandidates.entrySet().stream()
            .filter(e -> e.getKey().getWeight() == heaviestError)
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    completeCandidates =
        completeCandidates.entrySet().stream()
            .filter(e -> e.getKey().getWeight() == heaviestComplete)
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    ParserTreeCandidate<ExecuteContext<DATA>, DATA> executeCandidate = null;
    ParserTreeCandidate<ErrorContext<DATA>, DATA> errorCandidate = null;
    ParserTreeCandidate<CompleteContext<DATA>, DATA> completeCandidate = null;

    // Check for ambiguous handlers
    if (executeCandidates.size() > 1) {
      errors.add(new AmbiguousExecuteHandlersError(), context.getLine(), context.getWeight());
    } else if (executeCandidates.size() == 1) {
      executeCandidate = executeCandidates.keySet().stream().findFirst().orElseThrow();
    }

    if (errorCandidates.size() == 1) {
      errorCandidate = errorCandidates.keySet().stream().findFirst().orElseThrow();
      errors.clear();
      errors.addAll(errorCandidates.get(errorCandidate).getErrors());
    }

    if (completeCandidates.size() == 1) {
      completeCandidate = completeCandidates.keySet().stream().findFirst().orElseThrow();
      completions.clear();
      completions.addAll(completeCandidates.get(completeCandidate).getCompletions());
    }

    return new ParserTreeResult<>(
        executeCandidate, errorCandidate, completeCandidate, errors, completions);
  }

  protected ParserTreeResult<DATA> parseFallback(ParserTreeContext<DATA> context) {
    return fallbackHandler != null
        ? fallbackHandler.handle(context)
        : new ParserTreeResult<>(null, null, null, new DefaultErrorCollection(), new ArrayList<>());
  }

  @Override
  public ParserTree<DATA> forEachLeaf(Consumer<ParserTree<DATA>> consumer) {
    leafs().forEach(consumer);
    return this;
  }

  @Override
  public Collection<ParserTree<DATA>> children() {
    return children;
  }

  @Override
  public ParserTree<DATA> forEach(Consumer<ParserTree<DATA>> consumer) {
    children.forEach(consumer);
    return this;
  }

  @Override
  public Collection<ParserTree<DATA>> leafs() {
    if (children.isEmpty()) {
      return Collections.singleton(this);
    }
    return children.stream().flatMap(c -> c.leafs().stream()).collect(Collectors.toSet());
  }

  @Override
  public ParserTree<DATA> walk(Consumer<List<ParserTree<DATA>>> consumer) {
    walk(consumer, new ArrayList<>());
    return this;
  }

  public void walk(Consumer<List<ParserTree<DATA>>> consumer, List<ParserTree<DATA>> chain) {
    chain.add(this);
    consumer.accept(chain);
    children.forEach(c -> c.walk(consumer, chain));
  }

  public int size() {
    return children.size();
  }
}
