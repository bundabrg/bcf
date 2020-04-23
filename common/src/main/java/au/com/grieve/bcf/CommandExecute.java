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

import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandExecute {
    @Getter
    final ParserMethod method;

    @Getter
    final List<Object> parameters = new ArrayList<>();

    public CommandExecute(ParserMethod method, List<Object> parameters) {
        this.method = method;
        this.parameters.addAll(parameters);
    }

    public CommandExecute(ParserMethod method) {
        this(method, new ArrayList<>());
    }

    /**
     * Execute method, prepending args and filling missing parameters with null
     */
    public Object invoke(Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        List<Object> param = new ArrayList<>(Arrays.asList(args));
        param.addAll(parameters);

        // Fill out extra parameters with null
        while (param.size() < method.getMethod().getParameterCount()) {
            param.add(null);
        }

        return method.invoke(param);

    }
}
