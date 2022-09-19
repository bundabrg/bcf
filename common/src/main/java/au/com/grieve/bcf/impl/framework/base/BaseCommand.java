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

package au.com.grieve.bcf.impl.framework.base;

import au.com.grieve.bcf.Command;
import au.com.grieve.bcf.ExecutionCandidate;
import au.com.grieve.bcf.ExecutionContext;
import au.com.grieve.bcf.impl.execution.DefaultExecutionCandidate;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public abstract class BaseCommand implements Command {
  private final Set<Command> children = new HashSet<>();

  public BaseCommand() {}

  protected abstract Method getDefaultMethod();

  protected abstract Method getErrorMethod();

  protected ExecutionCandidate getErrorExecutionCandidate(
      ExecutionContext context, int weight, String name, String message) {
    for (Command cmd :
        Stream.concat(Stream.of(this), context.getCommandChain().stream())
            .collect(Collectors.toList())) {
      Method method = ((BaseCommand) cmd).getErrorMethod();
      if (method != null) {
        return new DefaultExecutionCandidate(
            cmd,
            method,
            weight,
            Stream.concat(context.getPrependArguments().stream(), Stream.of(context, name, message))
                .collect(Collectors.toList()));
      }
    }
    return null;
  }

  protected ExecutionCandidate getDefaultExecutionCandidate(ExecutionContext context, int weight) {
    for (Command cmd :
        Stream.concat(Stream.of(this), context.getCommandChain().stream())
            .collect(Collectors.toList())) {
      Method method = ((BaseCommand) cmd).getDefaultMethod();
      if (method != null) {
        return new DefaultExecutionCandidate(
            cmd,
            method,
            weight,
            Stream.concat(context.getPrependArguments().stream(), Stream.of(context))
                .collect(Collectors.toList()));
      }
    }
    return null;
  }

  @Override
  public void addChild(Command childCommand) {
    this.children.add(childCommand);
  }
}
