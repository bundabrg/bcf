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

package au.com.grieve.bcf.impl.parser;

import au.com.grieve.bcf.CompletionCandidateGroup;
import au.com.grieve.bcf.ParsedLine;
import au.com.grieve.bcf.ParserContext;
import au.com.grieve.bcf.exception.EndOfLineException;
import au.com.grieve.bcf.exception.ParserSyntaxException;
import au.com.grieve.bcf.impl.completion.DefaultCompletionCandidate;
import au.com.grieve.bcf.impl.completion.StaticCompletionCandidateGroup;
import au.com.grieve.bcf.impl.error.InvalidFormatError;
import au.com.grieve.bcf.impl.error.NumberTooBigError;
import au.com.grieve.bcf.impl.error.NumberTooSmallError;
import java.util.List;
import java.util.Map;
import lombok.ToString;

@ToString(callSuper = true)
public class DoubleParser extends BaseParser<Object, Double> {

  public DoubleParser(Map<String, String> parameters) {
    super(parameters);
  }

  @Override
  protected Double doParse(ParserContext<Object> context, ParsedLine line)
      throws EndOfLineException, ParserSyntaxException {
    String input = line.next();
    double result;

    try {
      result = Double.parseDouble(input);
    } catch (IllegalArgumentException e) {
      throw new ParserSyntaxException(line, new InvalidFormatError("double number"));
    }

    if (getParameters().get("max") != null
        && result > Double.parseDouble(getParameters().get("max"))) {
      throw new ParserSyntaxException(line, new NumberTooBigError(getParameters().get("max")));
    }

    if (getParameters().get("min") != null
        && result < Double.parseDouble(getParameters().get("min"))) {
      throw new ParserSyntaxException(line, new NumberTooSmallError(getParameters().get("min")));
    }

    return result;
  }

  @Override
  protected void doComplete(
      ParserContext<Object> context, ParsedLine line, List<CompletionCandidateGroup> candidates)
      throws EndOfLineException {
    String input = line.getCurrentWord();

    CompletionCandidateGroup group =
        new StaticCompletionCandidateGroup(input, getParameters().get("description"));
    group
        .getCompletionCandidates()
        .add(
            new DefaultCompletionCandidate(
                "", getParameters().getOrDefault("placeholder", "<float>")));
    candidates.add(group);

    line.next();
  }
}
