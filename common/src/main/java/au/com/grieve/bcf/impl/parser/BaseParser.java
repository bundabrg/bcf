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

import au.com.grieve.bcf.CompletionCandidate;
import au.com.grieve.bcf.ParsedLine;
import au.com.grieve.bcf.Parser;
import au.com.grieve.bcf.exception.EndOfLineException;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@ToString(callSuper = true)
public abstract class BaseParser<RT> extends Parser<RT> {
    public BaseParser(Map<String, String> parameters) {
        super(parameters);
    }

    @Override
    public abstract void complete(ParsedLine line, List<CompletionCandidate> candidates);

    /**
     * Call doParse and make sure that errors don't mutate line
     *
     * @param line The input
     * @return Return Object
     * @throws EndOfLineException Ran out of input
     * @throws IllegalArgumentException Invalid input
     */
    @Override
    public RT parse(ParsedLine line) throws EndOfLineException, IllegalArgumentException {
        ParsedLine currentLine = line.copy();
        RT result = doParse(currentLine);
        line.setWordIndex(currentLine.getWordIndex());
        return result;
    }

    /**
     * Handle parsing the line.
     * @param line The input
     * @return Return Object
     * @throws EndOfLineException Ran out of input
     * @throws IllegalArgumentException Invalid input
     */
    protected abstract RT doParse(ParsedLine line) throws EndOfLineException, IllegalArgumentException;
}
