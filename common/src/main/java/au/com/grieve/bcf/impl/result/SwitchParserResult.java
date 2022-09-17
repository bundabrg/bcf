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

package au.com.grieve.bcf.impl.result;

import au.com.grieve.bcf.Parser;
import au.com.grieve.bcf.Result;
import au.com.grieve.bcf.exception.EndOfLineException;
import au.com.grieve.bcf.impl.line.DefaultParsedLine;
import lombok.Getter;

public class SwitchParserResult implements Result {

    @Getter
    private final Parser<?> parser;

    @Getter
    private boolean complete;

    private Object value;

    public SwitchParserResult(Parser<?> parser) {
        this.parser = parser;
    }

    public SwitchParserResult(Parser<?> parser, Object value) {
        this(parser);
        setValue(value);
    }

    public void setValue(Object value) {
        this.complete = true;
        this.value = value;
    }

    @Override
    public Object getValue() throws IllegalArgumentException {
        try {
            return complete ? value : parser.parse(new DefaultParsedLine(""));
        } catch (EndOfLineException e) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean isSuppressed() {
        return parser.getParameters().getOrDefault("suppress", "false").equals("true");
    }

    @Override
    public Result copy() {
        SwitchParserResult result = new SwitchParserResult(parser);
        result.value = value;
        result.complete = complete;
        return result;
    }
}
