/*
 * Copyright (c) 2020-2020 Brendan Grieve (bundabrg) - MIT License
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

package au.com.grieve.bcf.parsers;

import au.com.grieve.bcf.CommandManager;
import au.com.grieve.bcf.ParserContext;
import au.com.grieve.bcf.ParserNode;
import au.com.grieve.bcf.exceptions.ParserInvalidResultException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IntegerParser extends SingleParser {

    public IntegerParser(CommandManager manager, ParserNode node, ParserContext context) {
        super(manager, node, context);
    }

    @Override
    protected List<String> complete() {
        Map<String, String> parameters = getNode().getData().getParameters();

        if (parameters.containsKey("max")) {
            int min;
            int max;

            try {
                max = Integer.parseInt(parameters.get("max"));
            } catch (NumberFormatException e) {
                return super.complete();
            }

            try {
                min = Integer.parseInt(parameters.getOrDefault("min", "0"));
            } catch (NumberFormatException e) {
                min = 0;
            }

            return IntStream.rangeClosed(min, max)
                    .mapToObj(String::valueOf)
                    .filter(s -> s.startsWith(getInput()))
                    .limit(20)
                    .collect(Collectors.toList());
        }

        return super.complete();
    }

    @Override
    protected Object result() throws ParserInvalidResultException {
        int result;

        try {
            result = Integer.parseInt(getInput());

            if (getNode().getData().getParameters().containsKey("min")) {
                if (result < Integer.parseInt(getNode().getData().getParameters().get("min"))) {
                    throw new ParserInvalidResultException();
                }
            }

            if (getNode().getData().getParameters().containsKey("max")) {
                if (result > Integer.parseInt(getNode().getData().getParameters().get("max"))) {
                    throw new ParserInvalidResultException();
                }
            }

        } catch (NumberFormatException e) {
            throw new ParserInvalidResultException();
        }

        return result;
    }
}
