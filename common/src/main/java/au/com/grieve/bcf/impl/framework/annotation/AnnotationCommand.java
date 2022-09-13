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
import au.com.grieve.bcf.CompletionCandidate;
import au.com.grieve.bcf.ExecutionCandidate;
import au.com.grieve.bcf.exception.EndOfLineException;
import au.com.grieve.bcf.framework.annotation.annotations.Arg;
import au.com.grieve.bcf.impl.line.DefaultParsedLine;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Used by the AnnotationCommandManager as a Base Command class
 *
 * Commands are defined by annotations on methods
 */
@Getter
public class AnnotationCommand implements Command<DefaultParsedLine, AnnotationContext> {
    private final Set<Command<DefaultParsedLine, AnnotationContext>> children = new HashSet<>();
    private final List<String> classArgStrings = new ArrayList<>();
    private final Map<String, Method> methodArgStrings = new HashMap<>();

    public AnnotationCommand() {
        // Class Arguments
        for (Arg arg : getClass().getAnnotationsByType(Arg.class)) {
            classArgStrings.add(String.join(" ", arg.value()));
        }

        // Method Arguments
        for (Method method : getClass().getMethods()) {
            for (Arg arg : method.getAnnotationsByType(Arg.class)) {
                methodArgStrings.put(String.join(" ", arg.value()), method);
            }
        }
    }

    public Method getErrorMethod() {
        return null;
    }

    public Method getDefaultMethod() {
        return null;
    }

    protected boolean hasCommand() {
        return getClass().getAnnotation(au.com.grieve.bcf.framework.annotation.annotations.Command.class) != null;
    }

    protected boolean hasArg() {
        return getClass().getAnnotation(Arg.class) != null;
    }

    @Override
    public void complete(DefaultParsedLine line, List<CompletionCandidate> candidates, AnnotationContext context) {

    }

    @Override
    public ExecutionCandidate execute(DefaultParsedLine line, AnnotationContext context) {
        if (classArgStrings.size() > 0) {
            List<ArgumentParserChain> classChain = classArgStrings.stream()
                    .map(s -> new ArgumentParserChain(context.getParserClasses(), s))
                    .collect(Collectors.toList());

            for (ArgumentParserChain p : classChain) {
                List<Object> result = new ArrayList<>();
                DefaultParsedLine currentLine = line.copy();
                try {
                    p.parse(currentLine, result);
                } catch (EndOfLineException e) {
                    throw new RuntimeException(e);
                } catch (IllegalArgumentException e) {

                }
            }

        } else {
            executeMethod(line, context);
        }

        return null;

    }

    @Override
    public void addChild(Command<DefaultParsedLine, AnnotationContext> childCommand) {
        this.children.add(childCommand);
    }
}