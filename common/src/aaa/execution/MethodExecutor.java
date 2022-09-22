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

package au.com.grieve.bcf.impl.execution;

import au.com.grieve.bcf.Executor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MethodExecutor<RT> implements Executor<RT> {
  private final Object instance;
  private final Method method;
  private final List<Object> parameters = new ArrayList<>();

  public MethodExecutor(Object instance, Method method) {
    this(instance, method, new ArrayList<>());
  }

  public MethodExecutor(
      Object instance, Method method, Object... args) {

    this.instance = instance;
    this.method = method;
    this.parameters.addAll(List.of(args));
  }

  @Override
  public RT run(Object... args) {
    List<Object> allArgs = Stream.concat(
        Arrays.stream(args),
        parameters.stream()
    ).collect(Collectors.toList());

    // Fill out extra parameters with null
    while (allArgs.size() < method.getParameterCount()) {
      allArgs.add(null);
    }

    try {
      //noinspection unchecked
      return (RT) method.invoke(instance, allArgs.toArray());
    } catch (InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {
      throw new RuntimeException(
          "Failed to execute command: "
              + instance.getClass().getName()
              + "#"
              + method.getName()
              + "("
              + Arrays.stream(args).map(Object::toString).collect(Collectors.joining(", "))
              + ")",
          e);
    }

  }
}
