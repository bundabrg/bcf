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

package au.com.grieve.bcf;

import au.com.grieve.bcf.exceptions.ParserInvalidResultException;
import au.com.grieve.bcf.exceptions.ParserNoResultException;
import au.com.grieve.bcf.exceptions.ParserRequiredArgumentException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Parser {
    // Data
    @Getter
    protected final CommandManager manager;
    @Getter
    protected final ParserNode node;
    @Getter
    protected final ParserContext context;
    @Getter
    protected final Map<String, String> defaultParameters = new HashMap<>();

    @Getter
    protected boolean parsed = false;

    // Cache
    protected List<String> completions;
    protected Object result;

    public Parser(CommandManager manager, ParserNode node, ParserContext context) {
        this.manager = manager;
        this.node = node;
        this.context = context;
    }

    public List<String> getCompletions() {
        if (completions == null) {
            completions = complete();
        }
        return completions;
    }

    public Object getResult() throws ParserInvalidResultException, ParserNoResultException {
        if (result == null) {
            result = result();
        }

        return result;
    }

    @SuppressWarnings("unused")
    public String getParameter(String key) {
        return getParameter(key, null);
    }

    public String getParameter(String key, String def) {
        if (node.getData() != null) {
            return node.getData().getParameters().getOrDefault(key, defaultParameters.getOrDefault(key, def));
        }

        return defaultParameters.getOrDefault(key, def);
    }

    // default methods

    protected List<String> complete() {
        return new ArrayList<>();
    }

    // abstract methods
    protected abstract Object result() throws ParserInvalidResultException, ParserNoResultException;

    /**
     * Take input and return the unused data
     */
    public String parse(String input) throws ParserRequiredArgumentException {
        parsed = true;
        return input;
    }

    @Override
    public String toString() {
        return getClass().getName() + "(node=" + node + ", " +
                "context=" + context + ", " +
                "defaultParameters=" + defaultParameters + ", " +
                "parsed=" + parsed + ")";
    }


}
