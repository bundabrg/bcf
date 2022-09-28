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
import au.com.grieve.bcf.ParserOptions;
import au.com.grieve.bcf.exception.EndOfLineException;
import au.com.grieve.bcf.exception.ParserSyntaxException;
import au.com.grieve.bcf.impl.completion.DefaultCompletionCandidate;
import au.com.grieve.bcf.impl.completion.StaticCompletionCandidateGroup;
import au.com.grieve.bcf.impl.error.InvalidOptionError;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class StringParser extends BaseParser<Object, String> implements ParserOptions<String> {
  private final List<String> options = new ArrayList<>();

  public StringParser(Map<String, String> parameters) {
    super(parameters);
    options.addAll(
        Stream.of(parameters.getOrDefault("options", "").split("\\|"))
            .filter(r -> !r.isEmpty())
            .collect(Collectors.toList()));
  }

  public StringParser(
      String description,
      String defaultValue,
      boolean suppress,
      boolean required,
      String placeholder,
      List<String> switchValue,
      List<String> options) {
    super(description, defaultValue, suppress, required, placeholder, switchValue);
    this.options.addAll(options);
  }

  @Override
  protected String doParse(ParserContext<Object> context, ParsedLine line)
      throws EndOfLineException, ParserSyntaxException {
    String result = line.next();
    if (getOptions().size() > 0 && !getOptions().contains(result)) {
      throw new ParserSyntaxException(line, new InvalidOptionError(options));
    }

    return result;
  }

  @Override
  protected void doComplete(
      ParserContext<Object> context, ParsedLine line, List<CompletionCandidateGroup> candidates)
      throws EndOfLineException {
    String input = line.next();

    if (getOptions().size() > 0) {
      CompletionCandidateGroup group = new StaticCompletionCandidateGroup(input, getDescription());
      group
          .getCompletionCandidates()
          .addAll(
              getOptions().stream()
                  .map(DefaultCompletionCandidate::new)
                  .collect(Collectors.toList()));
      candidates.add(group);
    } else {
      CompletionCandidateGroup group = new StaticCompletionCandidateGroup(input, getDescription());
      group
          .getCompletionCandidates()
          .add(
              new DefaultCompletionCandidate(
                  "", getPlaceholder() != null ? getPlaceholder() : "<string>"));
      candidates.add(group);
    }
  }
}
