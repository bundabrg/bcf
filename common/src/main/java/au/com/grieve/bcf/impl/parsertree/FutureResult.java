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

import au.com.grieve.bcf.FutureResultHandler;
import au.com.grieve.bcf.ParserTreeContext;
import au.com.grieve.bcf.ParserTreeResult;
import au.com.grieve.bcf.Result;
import au.com.grieve.bcf.exception.ResultNotSetException;
import au.com.grieve.bcf.impl.line.DefaultParsedLine;
import lombok.Getter;

@Getter
public class FutureResult<DATA> extends Result {
  private final FutureResultHandler<DATA> handler;
  private final ParserTreeContext<DATA> context;

  public FutureResult(
      ParserTreeContext<DATA> context, FutureResultHandler<DATA> handler, boolean suppress) {
    super(suppress);
    this.context = context.copy();
    this.handler = handler;
  }

  public ParserTreeResult<DATA> handle(ParserTreeContext<DATA> context) {
    return handler.handle(this, context);
  }

  @Override
  public Object getValue() throws ResultNotSetException {
    // If a value is set we return that
    if (isSet()) {
      return super.getValue();
    }

    // Else we try get a default value
    ParserTreeContext<DATA> contextCopy =
        new DefaultParserTreeContext<>(new DefaultParsedLine(""), context.getData());
    ParserTreeResult<DATA> result = handler.handle(this, contextCopy);
    if (isSet()) {
      return super.getValue();
    }
    throw new ResultNotSetException(result.getErrors());
  }

  @Override
  public FutureResult<DATA> copy() {
    FutureResult<DATA> clone = new FutureResult<>(context, handler, suppress);
    clone.set = set;
    clone.value = value;
    return clone;
  }
}
