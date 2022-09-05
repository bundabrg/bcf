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

package au.com.grieve.bcf;

import au.com.grieve.bcf.exceptions.ParserInvalidResultException;
import au.com.grieve.bcf.exceptions.ParserRequiredArgumentException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public abstract class Parser {
    // Data
    protected final CommandManager<?, ?> manager;
    protected final CommandContext context;
    protected final Map<String, String> defaultParameters = new HashMap<>();

    protected boolean parsed = false;

    protected ArgNode argNode;

    // Cache
    protected Object result;

    public Parser(CommandManager<?, ?> manager, ArgNode argNode, CommandContext context) {
        this.manager = manager;
        this.context = context;
        this.argNode = argNode;
    }

    public abstract List<Candidate> getCompletions();

    public Object getResult() throws ParserInvalidResultException {
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
        return argNode.getParameters().getOrDefault(key, defaultParameters.getOrDefault(key, def));
    }

    // default methods

    protected List<String> complete() {
        return new ArrayList<>();
    }

    // abstract methods
    protected abstract Object result() throws ParserInvalidResultException;

    /**
     * Take input and return the unused data
     */
    public void parse(List<String> input, boolean defaults) throws ParserRequiredArgumentException {
        parsed = true;
    }

    @Override
    public String toString() {
        return getClass().getName() + "(argNode=" + argNode + ", " +
                "context=" + context + ", " +
                "defaultParameters=" + defaultParameters + ", " +
                "parsed=" + parsed + ")";
    }


}
