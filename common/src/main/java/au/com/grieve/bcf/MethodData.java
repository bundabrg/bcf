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


import au.com.grieve.bcf.annotations.Arg;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the data of a Node in the ParserNode Tree
 */
public class MethodData {
    @Getter
    final List<String> args = new ArrayList<>();

    @Getter
    final Method method;

    public MethodData(Method method) {
        // Make sure method is inside a BaseCommand class
        if (!method.getDeclaringClass().isAssignableFrom(BaseCommand.class)) {
            throw new IllegalArgumentException("Method is not in a class assignable from BaseCommand");
        }

        this.method = method;

        Arg[] classArgs = method.getDeclaringClass().getAnnotationsByType(Arg.class);

        for (Arg arg : method.getAnnotationsByType(Arg.class)) {
            if (classArgs.length == 0) {
                args.add(arg.value());
            }

            // Containing class arguments are prefixed to each of our arguments
            args.addAll(Arrays.stream(classArgs)
                    .map(a -> a.value() + " " + arg.value())
                    .collect(Collectors.toList()));
        }
    }

}
