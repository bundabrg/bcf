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
import au.com.grieve.bcf.exception.EndOfLineException;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@ToString(callSuper = true)
public class DoubleParser extends BaseParser<Double> {
    public DoubleParser(Map<String, String> parameters) {
        super(parameters);
    }

    @Override
    public void complete(ParsedLine line, List<CompletionCandidate> candidates) {

    }

    @Override
    protected Double doParse(ParsedLine line) throws EndOfLineException, IllegalArgumentException {
        double result = Double.parseDouble(line.next());
        if (getParameters().get("max") != null && result > Double.parseDouble(getParameters().get("max"))) {
            throw new IllegalArgumentException("Value larger than max");
        }

        if (getParameters().get("min") != null && result < Double.parseDouble(getParameters().get("min"))) {
            throw new IllegalArgumentException("Value smaller than min");
        }

        return result;
    }
}
