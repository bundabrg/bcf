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
import au.com.grieve.bcf.exception.EndOfLineException;
import au.com.grieve.bcf.impl.completion.DefaultCompletionCandidate;
import au.com.grieve.bcf.impl.completion.ParserCompletionCandidateGroup;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.ToString;

@ToString(callSuper = true)
public class StringParser extends BaseParser<String> {
  public StringParser(Map<String, String> parameters) {
    super(parameters);
  }

  @Override
  protected String doParse(ParsedLine line) throws EndOfLineException {
    String result = line.next();
    if (getParameters().containsKey("options")) {
      String r = result;

      result =
          Arrays.stream(getParameters().get("options").split("\\|"))
              .filter(s -> s.equals(r))
              .findFirst()
              .orElseThrow(IllegalArgumentException::new);
    }

    return result;
  }

  @Override
  protected void doComplete(ParsedLine line, List<CompletionCandidateGroup> candidates)
      throws EndOfLineException {
    String input = line.getCurrentWord();

    if (getParameters().containsKey("options")) {
      ParserCompletionCandidateGroup group = new ParserCompletionCandidateGroup(this, input);
      group
          .getCompletionCandidates()
          .addAll(
              Arrays.stream(getParameters().get("options").split("\\|"))
                  .map(DefaultCompletionCandidate::new)
                  .collect(Collectors.toList()));
      candidates.add(group);
    } else {
      ParserCompletionCandidateGroup group = new ParserCompletionCandidateGroup(this, input);
      group
          .getCompletionCandidates()
          .add(
              new DefaultCompletionCandidate(
                  "", getParameters().getOrDefault("placeholder", "<string>")));
      candidates.add(group);
    }

    line.next();
  }
}
