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
import au.com.grieve.bcf.Parser;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Getter
@ToString
public class SwitchParserCompletionCandidateGroup extends ParserCompletionCandidateGroup {

    public SwitchParserCompletionCandidateGroup(Parser<?> parser, String input) {
        super(parser, input);
    }

    public boolean isComplete() {
        return getCompletionCandidates().size() > 0;
    }

    @Override
    public List<CompletionCandidate> getCompletionCandidates() {
        if (isComplete()) {
            return super.getCompletionCandidates();
        }

        // Return list of switches instead
        return Stream.of(getParser().getParameters().get("switch").split("\\|"))
                .map(s -> new DefaultCompletionCandidate("-" + s))
                .collect(Collectors.toList());
    }
}
