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

import au.com.grieve.bcf.Command;
import au.com.grieve.bcf.CommandData;
import au.com.grieve.bcf.CompletionCandidateGroup;
import au.com.grieve.bcf.CompletionContext;
import au.com.grieve.bcf.ExecutionCandidate;
import au.com.grieve.bcf.ExecutionContext;
import au.com.grieve.bcf.ExecutionError;
import au.com.grieve.bcf.ParserChain;
import au.com.grieve.bcf.Result;
import au.com.grieve.bcf.exception.EndOfLineException;
import au.com.grieve.bcf.exception.ParserChainException;
import au.com.grieve.bcf.exception.ParserSyntaxException;
import au.com.grieve.bcf.framework.annotation.annotations.Arg;
import au.com.grieve.bcf.framework.annotation.annotations.Default;
import au.com.grieve.bcf.framework.annotation.annotations.Error;
import au.com.grieve.bcf.impl.error.MissingRequired;
import au.com.grieve.bcf.impl.execution.DefaultExecutionCandidate;
import au.com.grieve.bcf.impl.framework.base.BaseCommand;
import au.com.grieve.bcf.impl.framework.base.BaseCommandData;
import au.com.grieve.bcf.impl.framework.base.BaseExecutionError;
import au.com.grieve.bcf.impl.line.DefaultParsedLine;
import au.com.grieve.bcf.impl.parserchain.StringParserChain;
import au.com.grieve.bcf.impl.result.SwitchParserResult;
import au.com.grieve.bcf.utils.ReflectUtils;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

/**
 * Used by the AnnotationCommandManager as a Base Command class Commands are defined by annotations
 * on methods
 */
@Getter
public class AnnotationCommand extends BaseCommand {
  private final List<ParserChain> classParserChains = new ArrayList<>();
  private final Map<Method, List<ParserChain>> methodParserChains = new HashMap<>();
  private final Method defaultMethod;
  private final Method errorMethod;
  private final CommandData commandData;

  public AnnotationCommand() {
    // If we are a root command then build command data
    au.com.grieve.bcf.framework.annotation.annotations.Command commandAnnotation =
        getClass().getAnnotation(au.com.grieve.bcf.framework.annotation.annotations.Command.class);
    if (commandAnnotation != null) {
      String[] commandArgs = commandAnnotation.value().strip().split(" +", 2);
      String[] commandNames = commandArgs[0].split(("\\|"));
      commandData =
          new BaseCommandData(
              commandNames[0],
              Arrays.stream(commandNames).skip(1).toArray(String[]::new),
              commandAnnotation.description() != null
                      && commandAnnotation.description().length() > 0
                  ? commandAnnotation.description()
                  : null,
              commandArgs.length > 1 ? new StringParserChain(commandArgs[1]) : null,
              commandAnnotation.input());
    } else {
      commandData = null;
    }

    // Class Arguments
    for (Arg arg : ReflectUtils.getAllAnnotationsByType(getClass(), Arg.class)) {
      classParserChains.add(new StringParserChain(String.join(" ", arg.value())));
    }

    // Method Arguments
    for (Method method : getClass().getMethods()) {
      methodParserChains.put(
          method,
          Arrays.stream(method.getAnnotationsByType(Arg.class))
              .map(a -> new StringParserChain(String.join(" ", a.value())))
              .collect(Collectors.toList()));
    }

    // Check if we have special Methods
    errorMethod =
        Arrays.stream(getClass().getMethods())
            .filter(m -> m.isAnnotationPresent(Error.class))
            .findFirst()
            .orElse(null);
    defaultMethod =
        Arrays.stream(getClass().getMethods())
            .filter(m -> m.isAnnotationPresent(Default.class))
            .findFirst()
            .orElse(null);
  }

  protected void executeClass(
      ExecutionContext context, List<ExecutionCandidate> candidates, List<ExecutionError> errors) {

    for (ParserChain p : classParserChains) {
      List<Result> result = new ArrayList<>();
      ExecutionContext currentContext = context.copy();
      try {
        p.parse(currentContext, result);
      } catch (ParserChainException e) {
        errors.add(new BaseExecutionError(e.getLine(), e.getError(), e.getWeight()));
        continue;
      }

      currentContext.getResult().addAll(result);

      // Add a default at this level
      candidates.add(
          getDefaultExecutionCandidate(
              currentContext, currentContext.getParsedLine().getWordIndex()));

      executeMethods(currentContext, candidates, errors);
      executeChildren(currentContext, candidates, errors);
    }
  }

  protected void executeChildren(
      ExecutionContext context, List<ExecutionCandidate> candidates, List<ExecutionError> errors) {

    for (Command cmd : getChildren()) {
      ExecutionContext currentContext = context.copy();
      currentContext.getCommandChain().add(0, this);
      candidates.add(cmd.execute(currentContext, errors));
    }
  }

  protected void executeMethods(
      ExecutionContext context, List<ExecutionCandidate> candidates, List<ExecutionError> errors) {
    for (Method method : methodParserChains.keySet()) {
      executeMethod(context, method, candidates, errors);
    }
  }

  protected void executeMethod(
      ExecutionContext context,
      Method method,
      List<ExecutionCandidate> candidates,
      List<ExecutionError> errors) {

    for (ParserChain parserChain : methodParserChains.get(method)) {
      List<Result> result = new ArrayList<>();
      ExecutionContext currentContext = context.copy();
      try {
        parserChain.parse(currentContext, result);
      } catch (ParserChainException e) {
        errors.add(new BaseExecutionError(e.getLine(), e.getError(), e.getWeight()));
        continue;
      }

      // If we have input left then we reject this pathway
      if (!currentContext.getParsedLine().isEol()) {
        continue;
      }

      currentContext.getResult().addAll(result);

      // Check for required switches values. This feels like it shouldn't be in this file
      boolean isError = false;
      for (SwitchParserResult r :
          currentContext.getResult().stream()
              .filter(r -> r instanceof SwitchParserResult)
              .map(r -> (SwitchParserResult) r)
              .collect(Collectors.toList())) {
        if (!r.isComplete()) {
          try {
            r.setValue(r.getParser().parse(context, new DefaultParsedLine("")));
            currentContext.setWeight(currentContext.getWeight() + 1);
          } catch (EndOfLineException e) {
            errors.add(
                new BaseExecutionError(
                    currentContext.getParsedLine(),
                    new MissingRequired(
                        "-" + r.getParser().getParameters().get("switch").split("\\|")[0]),
                    currentContext.getWeight()));
            isError = true;
          } catch (ParserSyntaxException e) {
            errors.add(
                new BaseExecutionError(e.getLine(), e.getError(), currentContext.getWeight()));
            isError = true;
          }
        }
      }

      if (isError) {
        continue;
      }

      candidates.add(
          new DefaultExecutionCandidate(
              this,
              method,
              currentContext.getParsedLine().getWordIndex() + 1,
              Stream.concat(
                      currentContext
                          // Add prepend arguments first
                          .getPrependArguments()
                          .stream(),
                      currentContext.getResult().stream()
                          .filter(r -> !r.isSuppressed())
                          .map(Result::getValue))
                  .collect(Collectors.toList())));
    }
  }

  @Override
  public ExecutionCandidate execute(ExecutionContext context) {
    List<ExecutionError> errors = new ArrayList<>();

    if (getCommandData() != null && getCommandData().getParserChain() != null) {
      List<Result> result = new ArrayList<>();
      try {
        getCommandData().getParserChain().parse(context, result);
      } catch (ParserChainException e) {
        errors.add(new BaseExecutionError(e.getLine(), e.getError(), e.getWeight()));
        return getErrorExecutionCandidate(context, context.getParsedLine().getWordIndex(), errors);
      }
    }

    return execute(context, errors);
  }

  @Override
  public ExecutionCandidate execute(ExecutionContext context, List<ExecutionError> errors) {
    List<ExecutionCandidate> candidates = new ArrayList<>();
    List<ExecutionError> localErrors = new ArrayList<>();

    if (classParserChains.size() > 0) {
      executeClass(context, candidates, localErrors);
    } else {
      // Add a default at this level
      candidates.add(getDefaultExecutionCandidate(context, context.getParsedLine().getWordIndex()));

      executeMethods(context, candidates, localErrors);
      executeChildren(context, candidates, localErrors);
    }

    ExecutionCandidate bestCandidate =
        candidates.stream()
            .filter(Objects::nonNull)
            .max(Comparator.comparingInt(ExecutionCandidate::getWeight))
            .orElse(getDefaultExecutionCandidate(context, context.getParsedLine().getWordIndex()));

    // Eliminate errors with a lower weight than bestCandidata
    if (bestCandidate != null) {
      localErrors =
          localErrors.stream()
              .filter(e -> e.getParsedLine().getWordIndex() > bestCandidate.getWeight())
              .collect(Collectors.toList());
    }

    errors.addAll(localErrors);

    // Find heaviest error
    int errorWeight =
        errors.stream().map(e -> e.getParsedLine().getWordIndex()).max(Integer::compare).orElse(0);

    // If we have any errors left AND an error method we pass them to the error method
    if (errorWeight > (bestCandidate != null ? bestCandidate.getWeight() : 0)
        && getErrorMethod() != null) {
      // Find heaviest error
      return getErrorExecutionCandidate(
          context,
          errorWeight,
          errors.stream()
              .filter(e -> e.getParsedLine().getWordIndex() == errorWeight)
              .collect(Collectors.toList()));
    }

    return bestCandidate;
  }

  protected void completeClass(
      CompletionContext context, List<CompletionCandidateGroup> candidates) {
    for (ParserChain p : classParserChains) {
      CompletionContext currentContext = context.copy();
      try {
        p.complete(currentContext, candidates);
      } catch (ParserChainException e) {
        // Ignored for now
        continue;
      }

      completeMethods(currentContext, candidates);
      completeChildren(currentContext, candidates);
    }
  }

  protected void completeChildren(
      CompletionContext context, List<CompletionCandidateGroup> candidates) {
    for (Command cmd : getChildren()) {
      CompletionContext currentContext = context.copy();
      currentContext.getCommandChain().add(0, this);
      candidates.addAll(cmd.complete(currentContext));
    }
  }

  protected void completeMethods(
      CompletionContext context, List<CompletionCandidateGroup> candidates) {
    for (Method method : methodParserChains.keySet()) {
      completeMethod(context, method, candidates);
    }
  }

  protected void completeMethod(
      CompletionContext context, Method method, List<CompletionCandidateGroup> candidates) {
    for (ParserChain parserChain : methodParserChains.get(method)) {
      CompletionContext currentContext = context.copy();
      try {
        parserChain.complete(currentContext, candidates);
      } catch (ParserChainException ignored) {
        // Ignored for now
      }
    }
  }

  @Override
  public List<CompletionCandidateGroup> complete(CompletionContext context) {
    List<CompletionCandidateGroup> candidates = new ArrayList<>();

    if (context.getCommandChain().size() == 0
        && getCommandData() != null
        && getCommandData().getParserChain() != null) {
      try {
        getCommandData().getParserChain().complete(context, candidates);
      } catch (ParserChainException e) {
        return candidates;
      }
    }

    if (classParserChains.size() > 0) {
      completeClass(context, candidates);
    } else {
      completeMethods(context, candidates);
      completeChildren(context, candidates);
    }

    return candidates;
  }
}
