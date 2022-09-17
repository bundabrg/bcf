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
import au.com.grieve.bcf.framework.annotation.annotations.Arg;
import au.com.grieve.bcf.framework.annotation.annotations.Default;
import au.com.grieve.bcf.framework.annotation.annotations.Error;
import au.com.grieve.bcf.impl.execution.DefaultExecutionCandidate;
import au.com.grieve.bcf.utils.ReflectUtils;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Used by the AnnotationCommandManager as a Base Command class
 * Commands are defined by annotations on methods
 */
@Getter
public class AnnotationCommand implements Command {
    private final Set<Command> children = new HashSet<>();
    private final List<String> classArgStrings = new ArrayList<>();
    private final Map<String, Method> methodArgStrings = new HashMap<>();
    private final Method defaultMethod;
    private final Method errorMethod;

    public AnnotationCommand() {
        // Class Arguments
        for (Arg arg : ReflectUtils.getAllAnnotationsByType(getClass(), Arg.class)) {
            classArgStrings.add(String.join(" ", arg.value()));
        }

        // Method Arguments
        for (Method method : getClass().getMethods()) {
            for (Arg arg : method.getAnnotationsByType(Arg.class)) {
                methodArgStrings.put(String.join(" ", arg.value()), method);
            }
        }

        // Check if we have special Methods
        errorMethod = Arrays.stream(getClass().getMethods())
                .filter(m -> m.isAnnotationPresent(Error.class))
                .findFirst()
                .orElse(null);
        defaultMethod = Arrays.stream(getClass().getMethods())
                .filter(m -> m.isAnnotationPresent(Default.class))
                .findFirst()
                .orElse(null);
    }


    protected ExecutionCandidate getErrorExecutionCandidate(List<Command> chain, int weight) {
        for(Command cmd : Stream.concat(
                Stream.of(this),
                chain.stream()
        ).collect(Collectors.toList())) {
            Method method = ((AnnotationCommand) cmd).getErrorMethod();
            if (method != null) {
                return new DefaultExecutionCandidate(cmd, method, weight, new ArrayList<>());
            }
        }
        return null;
    }
    protected ExecutionCandidate getDefaultExecutionCandidate(List<Command> chain, int weight) {
        for(Command cmd : Stream.concat(
            Stream.of(this),
            chain.stream()
        ).collect(Collectors.toList())) {
            Method method = ((AnnotationCommand) cmd).getDefaultMethod();
            if (method != null) {
                return new DefaultExecutionCandidate(cmd, method, weight, new ArrayList<>());
            }
        }
        return null;
    }

    protected ExecutionCandidate executeClass(ParsedLine line, ExecuteContext context) {
        List<ExecutionCandidate> candidates = new ArrayList<>();

        List<ArgumentParserChain> classChain = classArgStrings.stream()
                .map(s -> new ArgumentParserChain(((AnnotationExecuteContext) context).getParserClasses(), s))
                .collect(Collectors.toList());

        for (ArgumentParserChain p : classChain) {
            List<Result> result = new ArrayList<>();
            ParsedLine currentLine = line.copy();
            ExecuteContext currentContext = context.copy();
            try {
                p.parse(currentLine, result, currentContext);
            } catch (EndOfLineException e) {
                // Ran out of input to satisfy this chain
                candidates.add(getErrorExecutionCandidate(context.getCommandChain(), currentLine.getWordIndex()));
                continue;
            } catch (IllegalArgumentException e) {
                // Error has occurred
                candidates.add(getErrorExecutionCandidate(context.getCommandChain(), currentLine.getWordIndex()));
                continue;
            }

            currentContext.getResult().addAll(result);

            // Add a default at this level
            candidates.add(getDefaultExecutionCandidate(currentContext.getCommandChain(), currentLine.getWordIndex()));

            candidates.add(executeMethod(currentLine.copy(), currentContext));
            candidates.add(executeChildren(currentLine.copy(), currentContext));
        }

        return candidates.stream()
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(ExecutionCandidate::getWeight))
                .orElse(null);
    }

    protected ExecutionCandidate executeChildren(ParsedLine line, ExecuteContext context) {
        List<ExecutionCandidate> candidates = new ArrayList<>();

        for(Command cmd : getChildren()) {
            ExecuteContext currentContext = context.copy();
            currentContext.getCommandChain().add(this);
            candidates.add(cmd.execute(line.copy(), currentContext));
        }

        return candidates.stream()
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(ExecutionCandidate::getWeight))
                .orElse(null);
    }

    protected ExecutionCandidate executeMethod(ParsedLine line, ExecuteContext context) {
        List<ExecutionCandidate> candidates = new ArrayList<>();

        for(Map.Entry<String, Method> item : methodArgStrings.entrySet()) {
            ArgumentParserChain methodChain = new ArgumentParserChain(((AnnotationExecuteContext) context).getParserClasses(), item.getKey());

            List<Result> result = new ArrayList<>();
            ParsedLine currentLine = line.copy();
            ExecuteContext currentContext = context.copy();
            try {
                methodChain.parse(currentLine, result, currentContext);
            } catch (EndOfLineException e) {
                // Ran out of input to satisfy this chain
                candidates.add(getErrorExecutionCandidate(context.getCommandChain(), currentLine.getWordIndex()));
                continue;
            } catch (IllegalArgumentException e) {
                // Error has occurred
                candidates.add(getErrorExecutionCandidate(context.getCommandChain(), currentLine.getWordIndex()));
                continue;
            }

            // If we have input left then we reject this pathway
            if (!currentLine.isEol()) {
                continue;
            }

            currentContext.getResult().addAll(result);

            try {
                candidates.add(new DefaultExecutionCandidate(this, item.getValue(), currentLine.getWordIndex(),
                        currentContext.getResult().stream()
                                .map(Result::getValue)
                                .collect(Collectors.toList())
                ));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return candidates.stream()
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(ExecutionCandidate::getWeight))
                .orElse(null);
    }

    @Override
    public ExecutionCandidate execute(ParsedLine line, ExecuteContext context) {
        List<ExecutionCandidate> candidates = new ArrayList<>();

        if (context.getCommandChain().size() == 0 && ((AnnotationExecuteContext) context).getPrefixParserChain() != null) {
            List<Result> result = new ArrayList<>();
            try {
                ((AnnotationExecuteContext) context).getPrefixParserChain().parse(line, result, context);
            } catch (EndOfLineException | IllegalArgumentException e) {
                return getErrorExecutionCandidate(context.getCommandChain(), line.getWordIndex());
            }
        }

        if (classArgStrings.size() > 0) {
            candidates.add(executeClass(line.copy(), context));
        } else {
            // Add a default at this level
            candidates.add(getDefaultExecutionCandidate(context.getCommandChain(), line.getWordIndex()));

            candidates.add(executeMethod(line.copy(), context));
            candidates.add(executeChildren(line.copy(), context));
        }
        return candidates.stream()
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(ExecutionCandidate::getWeight))
                .orElse(getDefaultExecutionCandidate(context.getCommandChain(), line.getWordIndex()));
    }

    protected void completeClass(ParsedLine line, List<CompletionCandidateGroup> candidates, CompletionContext context) {
        List<ArgumentParserChain> classChain = classArgStrings.stream()
                .map(s -> new ArgumentParserChain(((AnnotationCompletionContext) context).getParserClasses(), s))
                .collect(Collectors.toList());

        for (ArgumentParserChain p : classChain) {
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

    protected void completeChildren(ParsedLine line, List<CompletionCandidateGroup> candidates, CompletionContext context) {
        for(Command cmd : getChildren()) {
            CompletionContext currentContext = context.copy();
            currentContext.getCommandChain().add(this);
            cmd.complete(line.copy(), candidates, currentContext);
        }
    }

    protected void completeMethod(ParsedLine line, List<CompletionCandidateGroup> candidates, CompletionContext context) {
        for(Map.Entry<String, Method> item : methodArgStrings.entrySet()) {
            ArgumentParserChain methodChain = new ArgumentParserChain(((AnnotationCompletionContext) context).getParserClasses(), item.getKey());

            ParsedLine currentLine = line.copy();
            CompletionContext currentContext = context.copy();
            try {
                methodChain.complete(currentLine, candidates, currentContext);
            } catch (EndOfLineException ignored) {
            }
        }
    }


    @Override
    public void complete(ParsedLine line, List<CompletionCandidateGroup> candidates, CompletionContext context) {
        if (context.getCommandChain().size() == 0 && ((AnnotationCompletionContext) context).getPrefixParserChain() != null) {
            try {
                ((AnnotationCompletionContext) context).getPrefixParserChain().complete(line, candidates, context);
            } catch (EndOfLineException | IllegalArgumentException e) {
                return;
            }
        }

        if (classArgStrings.size() > 0) {
            completeClass(line.copy(), candidates, context);
        } else {
            completeMethod(line.copy(), candidates, context);
            completeChildren(line.copy(), candidates, context);
        }
    }

    @Override
    public void addChild(Command childCommand) {
        this.children.add(childCommand);
    }
}