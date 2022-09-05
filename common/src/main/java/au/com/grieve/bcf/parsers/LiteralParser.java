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

package au.com.grieve.bcf.parsers;

import au.com.grieve.bcf.ArgNode;
import au.com.grieve.bcf.BaseCommand;
import au.com.grieve.bcf.CommandContext;
import au.com.grieve.bcf.CommandManager;
import au.com.grieve.bcf.exceptions.ParserInvalidResultException;

import java.util.ArrayList;
import java.util.List;

/**
 * Literal is provided as follows:
 * string1[|string2][|*]
 * <p>
 * Returns a String Type
 * Consumes 1 argument
 * If * is provided then it will accept any input
 * Will use the first matching alias as an alternative for partials
 */
public class LiteralParser extends SingleParser {


    public LiteralParser(CommandManager<BaseCommand, ?> manager, ArgNode argNode, CommandContext context) {
        super(manager, argNode, context);
        defaultParameters.put("suppress", "true");
    }

    @Override
    protected List<String> complete() {
        List<String> result = new ArrayList<>();

        for (String alias : argNode.getName().split("\\|")) {
            if (alias.equals("*")) {
                result.add(getInput());
            } else if (alias.toLowerCase().startsWith(getInput().toLowerCase())) {
                result.add(alias);
            }
        }

        return result;
    }

    @Override
    protected Object result() throws ParserInvalidResultException {
        for (String alias : argNode.getName().split("\\|")) {
            if (alias.equals("*")) {
                return getInput();
            }

            if (alias.equalsIgnoreCase(getInput())) {
                return alias;
            }
        }

        throw new ParserInvalidResultException(this, "Invalid Command");
    }
}
