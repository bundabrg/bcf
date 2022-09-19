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
import au.com.grieve.bcf.ExecutionContext;
import au.com.grieve.bcf.ParsedLine;
import au.com.grieve.bcf.Parser;
import au.com.grieve.bcf.Result;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class BaseExecutionContext implements ExecutionContext {
  private final Map<String, Class<? extends Parser<?>>> parserClasses = new HashMap<>();
  private final List<Object> prependArguments = new ArrayList<>();
  private final List<Command> commandChain = new ArrayList<>();
  private final List<Result> result = new ArrayList<>();
  private final ParsedLine parsedLine;

  public BaseExecutionContext(ParsedLine parsedLine) {
    this.parsedLine = parsedLine;
  }

  @Override
  public ExecutionContext copy() {
    BaseExecutionContext result = new BaseExecutionContext(parsedLine.copy());
    result.parserClasses.putAll(parserClasses);
    result.getCommandChain().addAll(commandChain);
    result.getResult().addAll(this.result.stream().map(Result::copy).collect(Collectors.toList()));
    return result;
  }
}
