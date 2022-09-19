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
import au.com.grieve.bcf.ParsedLine;
import au.com.grieve.bcf.ParserChain;
import au.com.grieve.bcf.Result;
import au.com.grieve.bcf.exception.EndOfLineException;
import au.com.grieve.bcf.framework.annotation.annotations.Arg;
import au.com.grieve.bcf.framework.annotation.annotations.Default;
import au.com.grieve.bcf.framework.annotation.annotations.Description;
import au.com.grieve.bcf.framework.annotation.annotations.Error;
import au.com.grieve.bcf.impl.execution.DefaultExecutionCandidate;
import au.com.grieve.bcf.impl.framework.base.BaseCommand;
import au.com.grieve.bcf.impl.framework.base.BaseCommandData;
import au.com.grieve.bcf.impl.parserchain.StringParserChain;
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
  private final Map<ParserChain, Method> methodParserChains = new HashMap<>();
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
              getClass().isAnnotationPresent(Description.class)
                  ? getClass().getAnnotation(Description.class).value()
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
      for (Arg arg : method.getAnnotationsByType(Arg.class)) {
        methodParserChains.put(new StringParserChain(String.join(" ", arg.value())), method);
      }
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

  protected ExecutionCandidate executeClass(ParsedLine line, ExecutionContext context) {
    List<ExecutionCandidate> candidates = new ArrayList<>();

    for (ParserChain p : classParserChains) {
      List<Result> result = new ArrayList<>();
      ParsedLine currentLine = line.copy();
      ExecutionContext currentContext = context.copy();
      try {
        p.parse(currentLine, result, currentContext);
      } catch (EndOfLineException e) {
        // Ran out of input to satisfy this chain
        candidates.add(getErrorExecutionCandidate(context, currentLine.getWordIndex()));
        continue;
      } catch (IllegalArgumentException e) {
        // Error has occurred
        candidates.add(getErrorExecutionCandidate(context, currentLine.getWordIndex()));
        continue;
      }

      currentContext.getResult().addAll(result);

      // Add a default at this level
      candidates.add(getDefaultExecutionCandidate(currentContext, currentLine.getWordIndex()));

      candidates.add(executeMethod(currentLine.copy(), currentContext));
      candidates.add(executeChildren(currentLine.copy(), currentContext));
    }

    return candidates.stream()
        .filter(Objects::nonNull)
        .max(Comparator.comparingInt(ExecutionCandidate::getWeight))
        .orElse(null);
  }

  protected ExecutionCandidate executeChildren(ParsedLine line, ExecutionContext context) {
    List<ExecutionCandidate> candidates = new ArrayList<>();

    for (Command cmd : getChildren()) {
      ExecutionContext currentContext = context.copy();
      currentContext.getCommandChain().add(this);
      candidates.add(cmd.execute(line.copy(), currentContext));
    }

    return candidates.stream()
        .filter(Objects::nonNull)
        .max(Comparator.comparingInt(ExecutionCandidate::getWeight))
        .orElse(null);
  }

  protected ExecutionCandidate executeMethod(ParsedLine line, ExecutionContext context) {
    List<ExecutionCandidate> candidates = new ArrayList<>();

    for (Map.Entry<ParserChain, Method> item : methodParserChains.entrySet()) {
      List<Result> result = new ArrayList<>();
      ParsedLine currentLine = line.copy();
      ExecutionContext currentContext = context.copy();
      try {
        item.getKey().parse(currentLine, result, currentContext);
      } catch (EndOfLineException e) {
        // Ran out of input to satisfy this chain
        candidates.add(getErrorExecutionCandidate(context, currentLine.getWordIndex()));
        continue;
      } catch (IllegalArgumentException e) {
        // Error has occurred
        candidates.add(getErrorExecutionCandidate(context, currentLine.getWordIndex()));
        continue;
      }

      // If we have input left then we reject this pathway
      if (!currentLine.isEol()) {
        continue;
      }

      currentContext.getResult().addAll(result);

      try {
        candidates.add(
            new DefaultExecutionCandidate(
                this,
                item.getValue(),
                currentLine.getWordIndex(),
                Stream.concat(
                        currentContext
                            .getPrependArguments()
                            .stream(), // Add prepend arguments first
                        currentContext.getResult().stream()
                            .filter(
                                r -> {
                                  r.getValue(); // Make sure we are able to return a value.
                                  // Needed for switches.
                                  return true;
                                })
                            .filter(r -> !r.isSuppressed())
                            .map(Result::getValue))
                    .collect(Collectors.toList())));
      } catch (IllegalArgumentException ignored) {
      }
    }
    return candidates.stream()
        .filter(Objects::nonNull)
        .max(Comparator.comparingInt(ExecutionCandidate::getWeight))
        .orElse(null);
  }

  @Override
  public ExecutionCandidate execute(ParsedLine line, ExecutionContext context) {
    List<ExecutionCandidate> candidates = new ArrayList<>();

    if (context.getCommandChain().size() == 0
        && getCommandData() != null
        && getCommandData().getParserChain() != null) {
      List<Result> result = new ArrayList<>();
      try {
        getCommandData().getParserChain().parse(line, result, context);
      } catch (EndOfLineException | IllegalArgumentException e) {
        return getErrorExecutionCandidate(context, line.getWordIndex());
      }
    }

    if (classParserChains.size() > 0) {
      candidates.add(executeClass(line.copy(), context));
    } else {
      // Add a default at this level
      candidates.add(getDefaultExecutionCandidate(context, line.getWordIndex()));

      candidates.add(executeMethod(line.copy(), context));
      candidates.add(executeChildren(line.copy(), context));
    }
    return candidates.stream()
        .filter(Objects::nonNull)
        .max(Comparator.comparingInt(ExecutionCandidate::getWeight))
        .orElse(getDefaultExecutionCandidate(context, line.getWordIndex()));
  }

  protected void completeClass(
      ParsedLine line, List<CompletionCandidateGroup> candidates, CompletionContext context) {
    for (ParserChain p : classParserChains) {
      ParsedLine currentLine = line.copy();
      CompletionContext currentContext = context.copy();
      try {
        p.complete(currentLine, candidates, currentContext);
      } catch (EndOfLineException e) {
        continue;
      }

      completeMethod(currentLine.copy(), candidates, currentContext);
      completeChildren(currentLine.copy(), candidates, currentContext);
    }
  }

  protected void completeChildren(
      ParsedLine line, List<CompletionCandidateGroup> candidates, CompletionContext context) {
    for (Command cmd : getChildren()) {
      CompletionContext currentContext = context.copy();
      currentContext.getCommandChain().add(this);
      cmd.complete(line.copy(), candidates, currentContext);
    }
  }

  protected void completeMethod(
      ParsedLine line, List<CompletionCandidateGroup> candidates, CompletionContext context) {
    for (Map.Entry<ParserChain, Method> item : methodParserChains.entrySet()) {
      ParsedLine currentLine = line.copy();
      CompletionContext currentContext = context.copy();
      try {
        item.getKey().complete(currentLine, candidates, currentContext);
      } catch (EndOfLineException ignored) {
      }
    }
  }

  @Override
  public void complete(
      ParsedLine line, List<CompletionCandidateGroup> candidates, CompletionContext context) {
    if (context.getCommandChain().size() == 0
        && getCommandData() != null
        && getCommandData().getParserChain() != null) {
      try {
        getCommandData().getParserChain().complete(line, candidates, context);
      } catch (EndOfLineException | IllegalArgumentException e) {
        return;
      }
    }

    if (classParserChains.size() > 0) {
      completeClass(line.copy(), candidates, context);
    } else {
      completeMethod(line.copy(), candidates, context);
      completeChildren(line.copy(), candidates, context);
    }
  }
}
