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

import au.com.grieve.bcf.ArgNode;
import au.com.grieve.bcf.CommandContext;
import au.com.grieve.bcf.CommandManager;
import au.com.grieve.bcf.Parser;
import au.com.grieve.bcf.exceptions.ParserInvalidResultException;
import au.com.grieve.bcf.exceptions.ParserRequiredArgumentException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Supports a single argument parser
 */
public abstract class SingleParser extends Parser {
    @Getter
    private String input;

    public SingleParser(CommandManager manager, ArgNode argNode, CommandContext context) {
        super(manager, argNode, context);
    }

    @Override
    public void parse(List<String> input, boolean defaults) throws ParserRequiredArgumentException {
        parsed = true;
        if (input == null || input.size() == 0) {
            // Check if a default is provided or if its not required
            if (!defaults || (getParameter("default") == null && getParameter("required", "true").equals("true"))) {
                throw new ParserRequiredArgumentException(this);
            }

            this.input = getParameter("default");
            return;
        }

        this.input = input.remove(0);
    }

    @Override
    public List<String> getCompletions() {
        if (input == null) {
            return new ArrayList<>();
        }

        return super.getCompletions();
    }

    @Override
    public Object getResult() throws ParserInvalidResultException {
        if (input == null || input.isEmpty()) {
            throw new ParserInvalidResultException(this, "Invalid command");
        }

        return super.getResult();
    }

}
