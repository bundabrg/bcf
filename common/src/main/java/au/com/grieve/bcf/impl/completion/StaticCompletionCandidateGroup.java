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

package au.com.grieve.bcf.impl.completion;

import au.com.grieve.bcf.CompletionCandidate;
import au.com.grieve.bcf.CompletionCandidateGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class StaticCompletionCandidateGroup implements CompletionCandidateGroup {

  private final List<CompletionCandidate> completionCandidates = new ArrayList<>();

  private final String input;
  private final String description;

  public StaticCompletionCandidateGroup(String input) {
    this(input, null);
  }

  public StaticCompletionCandidateGroup(String input, String description) {
    this.input = input;
    this.description = description;
  }

  @Override
  public List<CompletionCandidate> getMatchingCompletionCandidates() {
    return getCompletionCandidates().stream()
        .filter(c -> c.getValue().startsWith(getInput()))
        .collect(Collectors.toList());
  }
}
