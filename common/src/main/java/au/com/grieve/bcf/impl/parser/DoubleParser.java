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
import au.com.grieve.bcf.ParserMinMax;
import au.com.grieve.bcf.exception.EndOfLineException;
import au.com.grieve.bcf.exception.ParserSyntaxException;
import au.com.grieve.bcf.impl.completion.DefaultCompletionCandidate;
import au.com.grieve.bcf.impl.completion.StaticCompletionCandidateGroup;
import au.com.grieve.bcf.impl.error.InvalidFormatError;
import au.com.grieve.bcf.impl.error.NumberTooBigError;
import au.com.grieve.bcf.impl.error.NumberTooSmallError;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;

@SuppressWarnings("unused")
@Getter
@ToString(callSuper = true)
public class DoubleParser extends BaseParser<Object, Double> implements ParserMinMax<Double> {

  private final Double min;
  private final Double max;

  public DoubleParser(Map<String, String> parameters) {
    super(parameters);
    min = parameters.containsKey("min") ? Double.parseDouble(parameters.get("min")) : null;
    max = parameters.containsKey("max") ? Double.parseDouble(parameters.get("max")) : null;
  }

  public DoubleParser(
      String description,
      String defaultValue,
      boolean suppress,
      boolean required,
      boolean complete,
      String placeholder,
      List<String> switchValue,
      Double min,
      Double max) {
    super(description, defaultValue, suppress, required, complete, placeholder, switchValue);
    this.min = min;
    this.max = max;
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

    if (getMax() != null && result > getMax()) {
      throw new ParserSyntaxException(line, new NumberTooBigError(getMax().toString()));
    }

    if (getMin() != null && result < getMin()) {
      throw new ParserSyntaxException(line, new NumberTooSmallError(getMin().toString()));
    }

    return result;
  }

  @Override
  protected void doComplete(
      ParserContext<Object> context, ParsedLine line, List<CompletionCandidateGroup> candidates)
      throws EndOfLineException {
    String input = line.next();

    CompletionCandidateGroup group = new StaticCompletionCandidateGroup(input, getDescription());
    group
        .getCompletionCandidates()
        .add(
            new DefaultCompletionCandidate(
                "", getPlaceholder() != null ? getPlaceholder() : "<float>"));
    candidates.add(group);
  }
}
