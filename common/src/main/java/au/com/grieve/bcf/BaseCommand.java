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
import au.com.grieve.bcf.exceptions.SwitchNotFoundException;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseCommand {

    @Getter
    final List<BaseCommand> children = new ArrayList<>();

    CommandExecute execute(CommandRoot commandRoot, List<String> input, CommandContext context) {
        // Go through class Args first if they exist
        if (getClass().getAnnotationsByType(Arg.class).length > 0) {
            for (Arg classArgs : getClass().getAnnotationsByType(Arg.class)) {
                List<String> currentInput = new ArrayList<>(input);
                List<ArgNode> currentArgs = ArgNode.parse(classArgs.value());
                CommandContext currentContext = context.clone();


                try {
                    parseArg(commandRoot, currentArgs, currentInput, currentContext);
                } catch (ParserRequiredArgumentException | ParserNoResultException | ParserInvalidResultException | SwitchNotFoundException e) {
                    continue;
                }

                // Process methods
                CommandExecute ret = executeMethods(commandRoot, currentInput, currentContext);
                if (ret != null) {
                    return ret;
                }
            }
        } else {
            List<String> currentInput = new ArrayList<>(input);
            CommandContext currentContext = context.clone();

            // Process methods
            CommandExecute ret = executeMethods(commandRoot, currentInput, currentContext);
            if (ret != null) {
                return ret;
            }
        }

        return null;
    }

    /**
     * Execution for methods
     */
    CommandExecute executeMethods(CommandRoot commandRoot, List<String> input, CommandContext context) {
        for (Method method : getClass().getDeclaredMethods()) {
            for (Arg methodArgs : method.getAnnotationsByType(Arg.class)) {
                List<String> currentInput = new ArrayList<>(input);
                List<ArgNode> currentArgs = ArgNode.parse(methodArgs.value());
                CommandContext currentContext = context.clone();

                try {
                    parseArg(commandRoot, currentArgs, currentInput, currentContext);

                    if (currentInput.size() > 0) {
                        continue;
                    }


                    // No more input so see if we can parse all parsers and get their results
                    List<Object> results = new ArrayList<>();
                    for (Parser parser : currentContext.getParsers()) {
                        if (!parser.isParsed()) {
                            parser.parse(null, true);
                        }
                        if (!parser.getParameter("suppress", "false").equals("true")) {
                            results.add(parser.getResult());
                        }
                    }
                    return new CommandExecute(this, method, results);
                } catch (ParserRequiredArgumentException | ParserNoResultException | ParserInvalidResultException | SwitchNotFoundException ignored) {
                }
            }
        }
        return null;
    }

    List<String> complete(CommandRoot commandRoot, List<String> input, CommandContext context) {
        List<String> ret = new ArrayList<>();

        // Go through class Args first
        if (getClass().getAnnotationsByType(Arg.class).length > 0) {
            for (Arg classArgs : getClass().getAnnotationsByType(Arg.class)) {
                List<String> currentInput = new ArrayList<>(input);
                List<ArgNode> currentArgs = ArgNode.parse(classArgs.value());
                CommandContext currentContext = context.clone();


                try {
                    parseArg(commandRoot, currentArgs, currentInput, currentContext, false);
                } catch (ParserRequiredArgumentException | ParserNoResultException | ParserInvalidResultException e) {
                    // End of chain so save completions
                    ret.addAll(e.getParser().getCompletions());
                    continue;
                } catch (SwitchNotFoundException e) {
                    // List switch options
                    ret.addAll(currentContext.getSwitches().stream()
                            .flatMap(s -> Arrays.stream(s.getParameter("switch").split("\\|"))
                                    .filter(sw -> sw.toLowerCase().startsWith(e.getSwitchName().toLowerCase()))
                                    .limit(1)
                            )
                            .map(s -> "-" + s)
                            .limit(20)
                            .collect(Collectors.toList())
                    );
                    continue;
                }

                // Process methods
                ret.addAll(completeMethods(commandRoot, currentInput, currentContext));
            }
        } else {
            List<String> currentInput = new ArrayList<>(input);
            CommandContext currentContext = context.clone();

            // Process methods
            ret.addAll(completeMethods(commandRoot, currentInput, currentContext));
        }
        return ret;
    }

    /**
     * Completion for methods
     */
    List<String> completeMethods(CommandRoot commandRoot, List<String> input, CommandContext context) {
        List<String> ret = new ArrayList<>();
        for (Method method : getClass().getDeclaredMethods()) {
            for (Arg methodArgs : method.getAnnotationsByType(Arg.class)) {
                List<String> currentInput = new ArrayList<>(input);
                List<ArgNode> currentArgs = ArgNode.parse(methodArgs.value());
                CommandContext currentContext = context.clone();

                try {
                    parseArg(commandRoot, currentArgs, currentInput, currentContext, false);
                } catch (ParserRequiredArgumentException | ParserNoResultException | ParserInvalidResultException e) {
                    // End of chain so save completions
                    ret.addAll(e.getParser().getCompletions());
                } catch (SwitchNotFoundException e) {
                    // List switch options
                    ret.addAll(currentContext.getSwitches().stream()
                            .flatMap(s -> Arrays.stream(s.getParameter("switch").split("\\|"))
                                    .filter(sw -> sw.toLowerCase().startsWith(e.getSwitchName().toLowerCase()))
                                    .limit(1)
                            )
                            .map(s -> "-" + s)
                            .limit(20)
                            .collect(Collectors.toList())
                    );
                }
            }
        }
        return ret;
    }

    void parseArg(CommandRoot commandRoot, List<ArgNode> argNodes, List<String> input, CommandContext context) throws ParserNoResultException, ParserInvalidResultException, ParserRequiredArgumentException, SwitchNotFoundException {
        parseArg(commandRoot, argNodes, input, context, true);
    }

    void parseSwitches(CommandRoot commandRoot, List<String> input, CommandContext context, boolean defaults) throws SwitchNotFoundException, ParserRequiredArgumentException, ParserInvalidResultException, ParserNoResultException {
        while (input.size() > 0 && input.get(0).startsWith("-")) {
            String name = input.remove(0).substring(1);
            Parser parser = context.getSwitches().stream()
                    .flatMap(s -> Arrays.stream(s.getParameter("switch").split("\\|"))
                            .filter(sw -> sw.toLowerCase().equals(name.toLowerCase()))
                            .limit(1)
                            .map(sw -> s)
                    )
                    .findFirst()
                    .orElse(null);


            if (parser == null) {
                throw new SwitchNotFoundException(name);
            }

            parser.parse(input, defaults);
            parser.getResult();
        }
    }

    void parseArg(CommandRoot commandRoot, List<ArgNode> argNodes, List<String> input, CommandContext context, boolean defaults) throws ParserRequiredArgumentException, ParserInvalidResultException, ParserNoResultException, SwitchNotFoundException {
        while (argNodes.size() > 0) {
            ArgNode node = argNodes.remove(0);


            Parser parser = commandRoot.getParser(node, context);
            if (parser == null) {
                break;
            }

            context.getParsers().add(parser);

            // Take care of switches first
            if (node.getParameters().containsKey("switch")) {
                context.getSwitches().add(parser);
            } else {
                // Handle switches
                parseSwitches(commandRoot, input, context, defaults);

                parser.parse(input, defaults);
                parser.getResult();
            }
        }

        // Handle any remaining switches
        parseSwitches(commandRoot, input, context, defaults);
    }

}
