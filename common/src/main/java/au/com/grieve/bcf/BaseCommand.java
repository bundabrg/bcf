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
import au.com.grieve.bcf.annotations.Default;
import au.com.grieve.bcf.annotations.Error;
import au.com.grieve.bcf.exceptions.ParserInvalidResultException;
import au.com.grieve.bcf.exceptions.ParserRequiredArgumentException;
import au.com.grieve.bcf.exceptions.SwitchNotFoundException;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class BaseCommand {

    @Getter
    final List<BaseCommand> children = new ArrayList<>();

    @Getter
    final Method errorMethod;

    @Getter
    final Method defaultMethod;

    public BaseCommand() {
        Method errorMethod = null;
        Method defaultMethod = null;

        // Check if we have an error Method
        for (Method method : getClass().getDeclaredMethods()) {
            if (errorMethod == null && method.getAnnotation(Error.class) != null) {
                errorMethod = method;
                continue;
            }

            if (defaultMethod == null && method.getAnnotation(Default.class) != null) {
                defaultMethod = method;
            }
        }

        this.errorMethod = errorMethod;
        this.defaultMethod = defaultMethod;
    }

    CommandExecute getErrorExecute(CommandRoot commandRoot, String message, CommandContext context) {
        BaseCommand cmd = this;
        while (cmd.getErrorMethod() == null) {
            cmd = commandRoot.getCommandMap().get(cmd.getClass().getSuperclass());
            if (cmd == null) {
                break;
            }
        }

        if (cmd != null && cmd.getErrorMethod() != null) {
            return new CommandExecute(this, cmd.getErrorMethod(), Collections.singletonList(message), context);
        }

        return null;
    }

    CommandExecute getDefaultExecute(CommandRoot commandRoot, CommandContext context) {
        BaseCommand cmd = this;
        while (cmd.getDefaultMethod() == null) {
            cmd = commandRoot.getCommandMap().get(cmd.getClass().getSuperclass());
            if (cmd == null) {
                break;
            }
        }

        if (cmd != null && cmd.getDefaultMethod() != null) {
            return new CommandExecute(this, cmd.getDefaultMethod(), context);
        }

        return null;
    }

    CommandExecute execute(CommandRoot commandRoot, List<String> input, CommandContext context) {
        List<CommandExecute> commandExecutes = new ArrayList<>();

        // Go through class Args first if they exist
        if (getClass().getAnnotationsByType(Arg.class).length > 0) {
            for (Arg classArgs : getClass().getAnnotationsByType(Arg.class)) {
                List<String> currentInput = new ArrayList<>(input);
                List<ArgNode> currentArgs = ArgNode.parse(String.join(" ", classArgs.value()));
                CommandContext currentContext = context.clone();


                try {
                    parseArg(commandRoot, currentArgs, currentInput, currentContext);
                } catch (ParserRequiredArgumentException e) {
                    continue;
                } catch (SwitchNotFoundException e) {
                    commandExecutes.add(getErrorExecute(commandRoot, "Invalid switch: " + e.getSwitchName(), currentContext));
                    continue;
                } catch (ParserInvalidResultException e) {
                    commandExecutes.add(getErrorExecute(commandRoot, e.getMessage(), currentContext));
                    continue;
                }

                // Process methods
                for (Method method : getClass().getDeclaredMethods()) {
                    commandExecutes.add(executeMethod(method, commandRoot, currentInput, currentContext));
                }

                // Check each child class as well
                for (BaseCommand child : getChildren()) {
                    commandExecutes.add(child.execute(commandRoot, currentInput, currentContext));
                }

            }
        } else {
            List<String> currentInput = new ArrayList<>(input);
            CommandContext currentContext = context.clone();

            // Process methods
            for (Method method : getClass().getDeclaredMethods()) {
                commandExecutes.add(executeMethod(method, commandRoot, currentInput, currentContext));
            }

            // Check each child class as well
            for (BaseCommand child : getChildren()) {
                commandExecutes.add(child.execute(commandRoot, currentInput, currentContext));
            }
        }

        // Return best execute
        CommandExecute best = null;
        for (CommandExecute testExecute : commandExecutes.stream().filter(Objects::nonNull).collect(Collectors.toList())) {
            if (best == null
                    || best.getMethod().getAnnotation(Default.class) != null
                    || best.getContext().getParsers().size() < testExecute.getContext().getParsers().size()) {
                best = testExecute;
            }
        }

        // If we have no best then send to default
        if (best == null) {
            best = getDefaultExecute(commandRoot, context);
        }

        return best;
    }

    /**
     * Execution for methods
     */
    CommandExecute executeMethod(Method method, CommandRoot commandRoot, List<String> input, CommandContext context) {
        List<CommandExecute> commandExecutes = new ArrayList<>();

        for (Arg methodArgs : method.getAnnotationsByType(Arg.class)) {
            List<String> currentInput = new ArrayList<>(input);
            List<ArgNode> currentArgs = ArgNode.parse(String.join(" ", methodArgs.value()));
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
                commandExecutes.add(new CommandExecute(this, method, results, currentContext));
            } catch (ParserRequiredArgumentException ignored) {
            } catch (SwitchNotFoundException e) {
                commandExecutes.add(getErrorExecute(commandRoot, "Invalid switch: " + e.getSwitchName(), currentContext));
            } catch (ParserInvalidResultException e) {
                commandExecutes.add(getErrorExecute(commandRoot, e.getMessage(), currentContext));
            }
        }

        // Return best execute
        CommandExecute best = null;
        for (CommandExecute testExecute : commandExecutes.stream().filter(Objects::nonNull).collect(Collectors.toList())) {
            if (best == null || best.getContext().getParsers().size() < testExecute.getContext().getParsers().size()) {
                best = testExecute;
            }
        }

        return best;
    }

    List<String> complete(CommandRoot commandRoot, List<String> input, CommandContext context) {
        List<String> ret = new ArrayList<>();

        // Go through class Args first
        if (getClass().getAnnotationsByType(Arg.class).length > 0) {
            for (Arg classArgs : getClass().getAnnotationsByType(Arg.class)) {
                List<String> currentInput = new ArrayList<>(input);
                List<ArgNode> currentArgs = ArgNode.parse(String.join(" ", classArgs.value()));
                CommandContext currentContext = context.clone();


                try {
                    parseArg(commandRoot, currentArgs, currentInput, currentContext, false);
                } catch (ParserRequiredArgumentException | ParserInvalidResultException e) {
                    // End of chain so save completions if no more input
                    if (currentInput.size() == 0) {
                        ret.addAll(e.getParser().getCompletions());
                    }
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
                for (Method method : getClass().getDeclaredMethods()) {
                    ret.addAll(completeMethod(method, commandRoot, currentInput, currentContext));
                }

                // Check each child class as well
                for (BaseCommand child : getChildren()) {
                    ret.addAll(child.complete(commandRoot, currentInput, currentContext));
                }
            }
        } else {
            List<String> currentInput = new ArrayList<>(input);
            CommandContext currentContext = context.clone();

            // Process methods
            for (Method method : getClass().getDeclaredMethods()) {
                ret.addAll(completeMethod(method, commandRoot, currentInput, currentContext));
            }

            // Check each child class as well
            for (BaseCommand child : getChildren()) {
                ret.addAll(child.complete(commandRoot, currentInput, currentContext));
            }
        }

        // Remove duplicates and order alphabetically
        ret = new ArrayList<>(new HashSet<>(ret));
        ret.sort((s1, s2) -> s1.toLowerCase().compareTo(s2));

        return ret;
    }

    /**
     * Completion for methods
     */
    List<String> completeMethod(Method method, CommandRoot commandRoot, List<String> input, CommandContext context) {
        List<String> ret = new ArrayList<>();
        for (Arg methodArgs : method.getAnnotationsByType(Arg.class)) {
            List<String> currentInput = new ArrayList<>(input);
            List<ArgNode> currentArgs = ArgNode.parse(String.join(" ", methodArgs.value()));
            CommandContext currentContext = context.clone();

            try {
                parseArg(commandRoot, currentArgs, currentInput, currentContext, false);
            } catch (ParserRequiredArgumentException | ParserInvalidResultException e) {
                // End of chain so save completion if no more input
                if (currentInput.size() == 0) {
                    ret.addAll(e.getParser().getCompletions());

                    if (currentContext.getCurrentParser().getParameter("switch", null) == null) {
                        if (currentInput.stream().allMatch(s -> s.equals("")) && (input.size() == 0 || input.get(input.size() - 1).equals(""))) {
                            // Add switches
                            ret.addAll(currentContext.getSwitches().stream()
                                    .flatMap(s -> Arrays.stream(s.getParameter("switch").split("\\|"))
                                            .limit(1)
                                    )
                                    .map(s -> "-" + s)
                                    .limit(20)
                                    .collect(Collectors.toList())
                            );
                        }
                    }

                }


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
        }
        return ret;
    }

    void parseArg(CommandRoot commandRoot, List<ArgNode> argNodes, List<String> input, CommandContext context) throws ParserInvalidResultException, ParserRequiredArgumentException, SwitchNotFoundException {
        parseArg(commandRoot, argNodes, input, context, true);
    }

    void parseSwitches(CommandRoot commandRoot, List<String> input, CommandContext context, boolean defaults) throws SwitchNotFoundException, ParserRequiredArgumentException, ParserInvalidResultException {
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

            context.getSwitches().remove(parser);

            context.setCurrentParser(parser);

            parser.parse(input, false);
            parser.getResult();
        }
    }

    void parseArg(CommandRoot commandRoot, List<ArgNode> argNodes, List<String> input, CommandContext context, boolean defaults) throws ParserRequiredArgumentException, ParserInvalidResultException, SwitchNotFoundException {
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

                context.setCurrentParser(parser);

                parser.parse(input, defaults);
                parser.getResult();
            }
        }

        // Handle any remaining switches
        parseSwitches(commandRoot, input, context, defaults);
    }

}
