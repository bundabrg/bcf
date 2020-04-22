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
import au.com.grieve.bcf.Parser;
import au.com.grieve.bcf.ParserContext;
import au.com.grieve.bcf.ParserNode;
import au.com.grieve.bcf.exceptions.ParserInvalidResultException;
import au.com.grieve.bcf.exceptions.ParserNoResultException;
import au.com.grieve.bcf.exceptions.ParserRequiredArgumentException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Supports a single argument parser
 */
public abstract class SingleParser extends Parser {
    @Getter
    private String input;

    public SingleParser(CommandManager manager, ParserNode node, ParserContext context) {
        super(manager, node, context);
    }

    @Override
    public String parse(String input) throws ParserRequiredArgumentException {
        parsed = true;
        if (input == null) {
            Map<String, String> parameters = node.getData().getParameters();

            // Check if a default is provided or if its not required
            if (!parameters.containsKey("default") && parameters.getOrDefault("required", "true").equals("true")) {
                throw new ParserRequiredArgumentException();
            }

            this.input = parameters.getOrDefault("default", null);
            return null;
        }

        String[] result = input.split(" ", 2);

        this.input = result[0];
        return result.length > 1 ? result[1] : null;
    }

    @Override
    public List<String> getCompletions() {
        if (input == null) {
            return new ArrayList<>();
        }

        return super.getCompletions();
    }

    @Override
    public Object getResult() throws ParserInvalidResultException, ParserNoResultException {
        if (input == null) {
            return null;
        }

        return super.getResult();
    }

}
