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

package au.com.grieve.bcf;

import au.com.grieve.bcf.impl.error.DefaultErrorCollection;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ParserTreeResult<DATA> {
  private final ParserTreeCandidate<ExecuteContext<DATA>, DATA> executeCandidate;
  private final ParserTreeCandidate<ErrorContext<DATA>, DATA> errorCandidate;
  private final ParserTreeCandidate<CompleteContext<DATA>, DATA> completeCandidate;
  private final CommandErrorCollection errors;
  private final List<CompletionCandidateGroup> completions;
  public ParserTreeResult(
      ParserTreeCandidate<ExecuteContext<DATA>, DATA> executeCandidate,
      ParserTreeCandidate<ErrorContext<DATA>, DATA> errorCandidate,
      ParserTreeCandidate<CompleteContext<DATA>, DATA> completeCandidate,
      CommandErrorCollection errors,
      List<CompletionCandidateGroup> completions) {
    this.executeCandidate = executeCandidate;
    this.errorCandidate = errorCandidate;
    this.completeCandidate = completeCandidate;
    this.errors = errors;
    this.completions = completions;
  }

  public static <DATA> ParserTreeResult<DATA> EMPTY_RESULT() {
    return new ParserTreeResult<>(
        null, null, null, new DefaultErrorCollection(), new ArrayList<>());
  }

  public void execute() {
    if (executeCandidate != null) {
      executeCandidate
          .getHandler()
          .handle(
              new ExecuteContext<>(
                  executeCandidate.getLine(),
                  executeCandidate.getResults(),
                  executeCandidate.getData()));
    } else {
      errorCandidate
          .getHandler()
          .handle(
              new ErrorContext<>(
                  errorCandidate.getLine(), errorCandidate.getErrors(), errorCandidate.getData()));
    }
  }

  public List<CompletionCandidateGroup> complete() {
    if (completeCandidate != null) {
      completeCandidate
          .getHandler()
          .handle(
              new CompleteContext<>(
                  completeCandidate.getLine(),
                  completeCandidate.getCompletions(),
                  completeCandidate.getData()));
      return completeCandidate.getCompletions();
    }
    return completions;
  }
}
