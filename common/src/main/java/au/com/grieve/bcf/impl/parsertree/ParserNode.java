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

import au.com.grieve.bcf.CommandErrorCollection;
import au.com.grieve.bcf.CompletionCandidateGroup;
import au.com.grieve.bcf.ParsedLine;
import au.com.grieve.bcf.Parser;
import au.com.grieve.bcf.ParserTreeContext;
import au.com.grieve.bcf.ParserTreeResult;
import au.com.grieve.bcf.Result;
import au.com.grieve.bcf.exception.EndOfLineException;
import au.com.grieve.bcf.exception.ParserSyntaxException;
import au.com.grieve.bcf.impl.completion.DefaultCompletionCandidate;
import au.com.grieve.bcf.impl.completion.StaticCompletionCandidateGroup;
import au.com.grieve.bcf.impl.error.DefaultErrorCollection;
import au.com.grieve.bcf.impl.error.InputExpectedError;
import au.com.grieve.bcf.impl.error.InvalidOptionError;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

/**
 * StringParserTree uses a string argument to define the parsers to use. The parsers are later
 * provided during the parsing stage
 *
 * @param <DATA>
 */
@Getter
@ToString(callSuper = true)
public class ParserNode<DATA> extends BaseParserTree<DATA> {

  private final Parser<DATA, ?> parser;

  public ParserNode(Parser<DATA, ?> parser) {
    this.parser = parser;
  }

  protected ParserTreeResult<DATA> switchHandler(
      FutureResult<DATA> result, ParserTreeContext<DATA> context) {
    if (!context.getLine().getCurrentWord().startsWith("-")) {
      return ParserTreeResult.EMPTY_RESULT();
    }

    CommandErrorCollection errors = new DefaultErrorCollection();
    List<CompletionCandidateGroup> completions = new ArrayList<>();

    ParsedLine lineCopy = context.getLine().copy();
    String switchName;
    try {
      switchName = lineCopy.next().substring(1);
    } catch (EndOfLineException e) {
      return ParserTreeResult.EMPTY_RESULT();
    }

    // If at EOL then return completions on switch names
    if (lineCopy.isEol()) {
      CompletionCandidateGroup group =
          new StaticCompletionCandidateGroup(switchName, parser.getDescription());
      group
          .getCompletionCandidates()
          .addAll(
              parser.getSwitch().stream()
                  .map(DefaultCompletionCandidate::new)
                  .collect(Collectors.toList()));
      completions.add(group);
      return new ParserTreeResult<>(null, null, null, errors, completions);
    }

    // If it doesn't match us, return an error
    if (parser.getSwitch().stream().noneMatch(s -> s.equals(switchName))) {
      errors.add(
          new InvalidOptionError(
              parser.getSwitch().stream().map(s -> "-" + s).collect(Collectors.toList())),
          lineCopy,
          context.getWeight());
      return new ParserTreeResult<>(null, null, null, errors, completions);
    }

    // Parse it - TODO: Fix duplication below
    try {
      context.getLine().next();
    } catch (EndOfLineException e) {
      return ParserTreeResult.EMPTY_RESULT();
    }

    try {
      result.setValue(parser.parse(context, context.getLine()));
    } catch (ParserSyntaxException e) {
      errors.add(e.getError(), e.getLine(), context.getWeight());
      if (e.getLine().isEol()) {
        try {
          parser.complete(context, lineCopy, completions);
        } catch (EndOfLineException ignored) {
        }
      }
      return new ParserTreeResult<>(null, null, null, errors, completions);
    } catch (EndOfLineException e) {
      errors.add(new InputExpectedError(), context.getLine(), context.getWeight());
      try {
        parser.complete(context, lineCopy, completions);
      } catch (EndOfLineException ignored) {
      }
      return new ParserTreeResult<>(null, null, null, errors, completions);
    }

    // If we are at the EOL then return completions as well
    if (context.getLine().isEol()) {
      try {
        parser.complete(context, lineCopy, completions);
      } catch (EndOfLineException ignored) {
      }
    }

    // TODO I feel that an execute on a switch will override a better execute later in the chain.
    // This needs to be addressed
    // and as I pass through the original context in `result` I should be able to give it the
    // appropriate weight here

    return new ParserTreeResult<>(null, null, null, errors, completions);
  }

  @Override
  public @NotNull ParserTreeResult<DATA> parse(ParserTreeContext<DATA> context) {
    CommandErrorCollection errors = new DefaultErrorCollection();
    List<CompletionCandidateGroup> completions = new ArrayList<>();

    if (parser.getSwitch().size() > 0) {
      context
          .getResults()
          .add(new FutureResult<>(context.copy(), this::switchHandler, parser.isSuppress()));
      return parseChildren(context.copy());
    }

    ParsedLine originalLine = context.getLine().copy();
    try {
      context
          .getResults()
          .add(new Result(parser.parse(context, context.getLine()), parser.isSuppress()));
    } catch (ParserSyntaxException e) {
      errors.add(e.getError(), e.getLine(), context.getWeight());
      if (e.getLine().isEol()) {
        try {
          parser.complete(context, originalLine, completions);
        } catch (EndOfLineException ignored) {
        }
      }
      return new ParserTreeResult<>(null, null, null, errors, completions);
    } catch (EndOfLineException e) {
      errors.add(new InputExpectedError(), context.getLine(), context.getWeight());
      try {
        parser.complete(context, originalLine, completions);
      } catch (EndOfLineException ignored) {
      }
      return new ParserTreeResult<>(null, null, null, errors, completions);
    }

    ParserTreeResult<DATA> childResult = parseChildren(context.copy());

    // If we are at the EOL then return completions as well
    if (context.getLine().isEol()) {
      try {
        parser.complete(context, originalLine, childResult.getCompletions());
      } catch (EndOfLineException ignored) {
      }
    }
    return childResult;
  }
}
