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

import au.com.grieve.bcf.ExecutionCandidate;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class DefaultExecutionCandidate implements ExecutionCandidate {

  private final Object instance;
  private final Method method;
  private final List<Object> parameters;
  private final int weight;

  public DefaultExecutionCandidate(
      Object instance, Method method, int weight, List<Object> parameters) {

    this.instance = instance;
    this.method = method;
    this.weight = weight;
    this.parameters = new ArrayList<>(parameters);
  }

  @Override
  public Object invoke(Object... args) throws InvocationTargetException, IllegalAccessException {
    List<Object> allArgs = new ArrayList<>(Arrays.asList(args));
    allArgs.addAll(parameters);

    // Fill out extra parameters with null
    while (allArgs.size() < method.getParameterCount()) {
      allArgs.add(null);
    }

    return method.invoke(instance, allArgs.toArray());
  }
}
