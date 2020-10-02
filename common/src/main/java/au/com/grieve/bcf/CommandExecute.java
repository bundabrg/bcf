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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandExecute {
    @Getter
    private final Method method;

    @Getter
    private final BaseCommand command;

    @Getter
    private final List<Object> parameters = new ArrayList<>();

    @Getter
    private final CommandContext context;

    public CommandExecute(BaseCommand command, Method method, List<Object> parameters, CommandContext context) {
        this.command = command;
        this.method = method;
        if (parameters != null) {
            this.parameters.addAll(parameters);
        }
        this.context = context;
    }

    public CommandExecute(BaseCommand command, Method method, CommandContext context) {
        this(command, method, null, context);
    }

    /**
     * Execute method, prepending args and filling missing parameters with null
     */
    public Object invoke(Object... args) {
        List<Object> param = new ArrayList<>(Arrays.asList(args));
        param.addAll(parameters);

        // Fill out extra parameters with null
        while (param.size() < method.getParameterCount()) {
            param.add(null);
        }

        try {
            return method.invoke(command, param.toArray());
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            System.err.println(
                    "Error executing Command: " +
                            command.getClass().getName() + "." + method.getName() +
                            "(" + Arrays.stream(method.getParameterTypes()).map(Class::getName).collect(Collectors.joining(", ")) + ")" +
                            " called with (" + param.stream().map(c -> c.getClass().getName()).collect(Collectors.joining(", ")) + ")");

            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return getClass().getName() + "(command=" + command +
                ", method=" + method +
                ", parameters=" + parameters +
                ")";


    }
}
