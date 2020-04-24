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
import au.com.grieve.bcf.exceptions.ParserInvalidResultException;
import au.com.grieve.bcf.exceptions.ParserNoResultException;
import au.com.grieve.bcf.exceptions.ParserRequiredArgumentException;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseCommand {

    @Getter
    final List<BaseCommand> children = new ArrayList<>();


    List<String> complete(CommandRoot commandRoot, List<String> input, CommandContext context) {
        List<String> ret = new ArrayList<>();
        System.err.println("Initial Input: " + input);

        // Go through class Args first
        for (Arg classArgs : getClass().getAnnotationsByType(Arg.class)) {
            List<String> currentInput = new ArrayList<>(input);
            List<ArgNode> currentArgs = ArgNode.parse(classArgs.value());


            try {
                parseArg(commandRoot, currentArgs, currentInput, context, false);
            } catch (ParserRequiredArgumentException | ParserNoResultException | ParserInvalidResultException e) {
                System.err.println("End of chain: input:" + currentInput + ", nodes:" + currentArgs);
                break;
            }
            System.err.println("In chain: input:" + currentInput + ", nodes:" + currentArgs);
        }
        return ret;
    }

    Method[] getMethods(CommandRoot commandRoot, List<String> input, CommandContext context) {
        return getClass().getDeclaredMethods();
    }

    void parseArg(CommandRoot commandRoot, List<ArgNode> argNodes, List<String> input, CommandContext context) throws ParserNoResultException, ParserInvalidResultException, ParserRequiredArgumentException {
        parseArg(commandRoot, argNodes, input, context, true);
    }

    void parseArg(CommandRoot commandRoot, List<ArgNode> argNodes, List<String> input, CommandContext context, boolean defaults) throws ParserRequiredArgumentException, ParserInvalidResultException, ParserNoResultException {
        for (ArgNode node : argNodes) {
            Parser parser = commandRoot.getParser(node, context);
            if (parser == null) {
                break;
            }

            // Take care of switches first
            if (node.getParameters().containsKey("switch")) {
                context.getSwitches().add(parser);
            } else {
                if (input.size() > 0 && input.get(0).startsWith("-")) {
                    // return switch complete
                    System.err.println("Switch stuff");
                    break;
                }

                parser.parse(input, defaults);
                parser.getResult();
            }
        }
    }

}
