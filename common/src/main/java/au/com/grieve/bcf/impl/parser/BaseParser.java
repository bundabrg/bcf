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
import au.com.grieve.bcf.Parser;
import au.com.grieve.bcf.ParserContext;
import au.com.grieve.bcf.exception.EndOfLineException;
import au.com.grieve.bcf.exception.ParserSyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public abstract class BaseParser<DATA, RT> implements Parser<DATA, RT> {
  private final Map<String, String> parameters = new HashMap<>();

  public BaseParser(Map<String, String> parameters) {
    this.parameters.putAll(parameters);
  }

  /**
   * Call doComplete and make sure that errors don't mutate line
   *
   * @param context The context
   * @param candidates List of candidates
   */
  @Override
  public void complete(
      ParserContext<DATA> context, ParsedLine line, List<CompletionCandidateGroup> candidates)
      throws EndOfLineException {

    List<CompletionCandidateGroup> groups = new ArrayList<>();
    try {
      doComplete(context, line, groups);
    } finally {
      // Only add groups that actually have any candidates
      candidates.addAll(
          groups.stream()
              .filter(g -> g.getCompletionCandidates().size() > 0)
              .collect(Collectors.toList()));
    }
  }

  /**
   * Call doParse
   *
   * @param context The Context
   * @return Return Object
   * @throws EndOfLineException Ran out of input
   * @throws IllegalArgumentException Invalid input
   */
  @Override
  public RT parse(ParserContext<DATA> context, ParsedLine line)
      throws EndOfLineException, ParserSyntaxException {
    RT result;
    try {
      result = doParse(context, line);
    } catch (EndOfLineException e) {
      // Handle default
      if (parameters.getOrDefault("required", "true").equals("false")
          || parameters.containsKey("default")) {

        String defaultValue = parameters.get("default");
        if (defaultValue != null) {
          ParsedLine lineCopy = line.copy();
          lineCopy.insert(defaultValue);
          result = doParse(context, lineCopy);
        } else {
          result = null;
        }
      } else {
        throw e;
      }
    }

    return result;
  }

  /**
   * Handle parsing the line.
   *
   * @param line The input
   * @return Return Object
   * @throws EndOfLineException Ran out of input
   * @throws ParserSyntaxException Invalid input
   */
  protected abstract RT doParse(ParserContext<DATA> context, ParsedLine line)
      throws EndOfLineException, ParserSyntaxException;

  protected abstract void doComplete(
      ParserContext<DATA> context, ParsedLine line, List<CompletionCandidateGroup> candidates)
      throws EndOfLineException;
}
