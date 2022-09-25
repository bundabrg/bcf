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

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ParserTreeResult<DATA> {
  private final ExecuteCandidate<DATA> executeCandidate;
  private final ErrorCandidate<DATA> errorCandidate;
  private final CompleteCandidate<DATA> completeCandidate;
  private final CommandErrorCollection errors;
  private final List<CompletionCandidateGroup> completions;

  public ParserTreeResult(
      ExecuteCandidate<DATA> executeCandidate,
      ErrorCandidate<DATA> errorCandidate,
      CompleteCandidate<DATA> completeCandidate,
      CommandErrorCollection errors,
      List<CompletionCandidateGroup> completions) {
    this.executeCandidate = executeCandidate;
    this.errorCandidate = errorCandidate;
    this.completeCandidate = completeCandidate;
    this.errors = errors;
    this.completions = completions;
  }

  public boolean canExecute() {
    return executeCandidate != null || errorCandidate != null;
  }

  public void execute() {
    if (executeCandidate != null) {
      executeCandidate
          .getHandler()
          .handle(
              new ExecuteContext<>(
                  executeCandidate.getContext().getResults(),
                  executeCandidate.getContext().getData()));
    } else {
      errorCandidate
          .getHandler()
          .handle(
              new ErrorContext<>(
                  errorCandidate.getContext().getLine(),
                  errorCandidate.getErrors(),
                  errorCandidate.getContext().getData()));
    }
  }

  public List<CompletionCandidateGroup> complete() {
    return completeCandidate != null
        ? completeCandidate
            .getHandler()
            .handle(
                new CompleteContext<>(
                    completeCandidate.getContext().getLine(),
                    completeCandidate.getCompletions(),
                    completeCandidate.getContext().getData()))
        : new ArrayList<>();
  }
}
