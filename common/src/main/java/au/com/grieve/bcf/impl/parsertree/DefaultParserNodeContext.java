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

import au.com.grieve.bcf.CommandErrorCandidate;
import au.com.grieve.bcf.ParsedLine;
import au.com.grieve.bcf.Parser;
import au.com.grieve.bcf.ParserTreeContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
public class DefaultParserNodeContext<DATA> implements ParserTreeContext<DATA> {
  private final Map<String, Class<? extends Parser<DATA, ?>>> parserClasses = new HashMap<>();
  private final DATA data;
  private final ParsedLine line;
  private final List<Object> results = new ArrayList<>();
  private final List<CommandErrorCandidate> errors = new ArrayList<>();
  @Setter private int weight = 0;

  public DefaultParserNodeContext(
      ParsedLine line, Map<String, Class<? extends Parser<DATA, ?>>> parserClasses, DATA data) {
    this.line = line;
    this.data = data;
    this.parserClasses.putAll(parserClasses);
  }

  @Override
  public ParserTreeContext<DATA> copy() {
    ParserTreeContext<DATA> clone =
        new DefaultParserNodeContext<>(line.copy(), parserClasses, data);
    clone.getErrors().addAll(errors);
    clone.getResults().addAll(results);
    clone.setWeight(weight);
    return clone;
  }
}
