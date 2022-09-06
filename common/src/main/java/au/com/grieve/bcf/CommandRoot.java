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

import au.com.grieve.bcf.annotations.Arg;
import au.com.grieve.bcf.annotations.Default;
import au.com.grieve.bcf.annotations.Error;
import au.com.grieve.bcf.exceptions.ParserInvalidResultException;
import au.com.grieve.bcf.exceptions.ParserRequiredArgumentException;
import au.com.grieve.bcf.exceptions.SwitchNotFoundException;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class CommandRoot {
    private final BaseCommand command;

    private final CommandManager<?, ?> manager;

    public CommandRoot(CommandManager<?, ?> manager, BaseCommand cmd) {
        this.manager = manager;
        this.command = cmd;
    }

    protected Parser getParser(ArgNode argNode, CommandContext context) {
        return manager.getParser(argNode, context);

    }

    public CommandExecute execute(List<String> input, CommandContext context) {
        return execute(command, input, context);
    }

    public List<Candidate> complete(List<String> input, CommandContext context) {
        return complete(command, input, context);
    }

    protected CommandExecute getErrorExecute(BaseCommand command, String message, CommandContext context) {
        for (BaseCommand cmd :
                Stream.concat(
                        Stream.of(command),
                        context.getCommandStack().stream()
                ).collect(Collectors.toList())
        ) {
            if (cmd.getErrorMethod() != null) {
                return new CommandExecute(cmd, cmd.getErrorMethod(), Collections.singletonList(message), context);
            }
        }
        return null;
    }

    protected CommandExecute getDefaultExecute(BaseCommand command, CommandContext context) {
        for (BaseCommand cmd :
                Stream.concat(
                        Stream.of(command),
                        context.getCommandStack().stream()
                ).collect(Collectors.toList())
        ) {
            if (cmd.getDefaultMethod() != null) {
                return new CommandExecute(cmd, cmd.getDefaultMethod(), context);
            }
        }
        return null;
    }

    public CommandExecute execute(BaseCommand command, List<String> input, CommandContext context) {
        List<CommandExecute> commandExecutes = new ArrayList<>();

        // Go through class Args first if they exist, as long as they are not on our commandRoot class (to allow @Commands to override @Args)
        if (command.getClass().getAnnotationsByType(Arg.class).length > 0) {
            for (Arg classArgs : command.getClass().getAnnotationsByType(Arg.class)) {
                List<String> currentInput = new ArrayList<>(input);
                List<ArgNode> currentArgs = ArgNode.parse(String.join(" ", classArgs.value()));
                CommandContext currentContext = context.copy();

                try {
                    parseArg(currentArgs, currentInput, currentContext);
                } catch (ParserRequiredArgumentException e) {
                    continue;
                } catch (SwitchNotFoundException e) {
                    commandExecutes.add(getErrorExecute(command, "Invalid switch: " + e.getSwitchName(), currentContext));
                    continue;
                } catch (ParserInvalidResultException e) {
                    commandExecutes.add(getErrorExecute(command, e.getMessage(), currentContext));
                    continue;
                }

                // Process methods
                for (Method method : command.getClass().getDeclaredMethods()) {
                    commandExecutes.add(executeMethod(method, command, currentInput, currentContext));
                }

                // Check each child class as well
                CommandManager.CommandConfig<?> cc = manager.getCommands().get(command.getClass());
                currentContext.getCommandStack().push(command);

                if (cc != null) {
                    for (BaseCommand child : cc.getChildren()) {
                        commandExecutes.add(execute(child, currentInput, currentContext));
                    }
                }
            }
        } else {
            List<String> currentInput = new ArrayList<>(input);
            CommandContext currentContext = context.copy();

            // Process methods
            for (Method method : command.getClass().getDeclaredMethods()) {
                commandExecutes.add(executeMethod(method, command, currentInput, currentContext));
            }

            // Check each child class as well
            CommandManager.CommandConfig<?> cc = manager.getCommands().get(command.getClass());
            currentContext.getCommandStack().push(command);

            if (cc != null) {
                for (BaseCommand child : cc.getChildren()) {
                    commandExecutes.add(execute(child, currentInput, currentContext));
                }
            }
        }

        // Return best execute
        CommandExecute best = null;
        for (CommandExecute testExecute : commandExecutes.stream().filter(Objects::nonNull).collect(Collectors.toList())) {
            // Always replace best when its null
            if (best == null) {
                best = testExecute;
                continue;
            }

            // A longer chain always replaces a shorter chain
            if (best.getContext().getParsers().size() < testExecute.getContext().getParsers().size()) {
                best = testExecute;
                continue;
            }

            // Equal chains rely on priority
            if (best.getContext().getParsers().size() == testExecute.getContext().getParsers().size()) {
                if (testExecute.getMethod().isAnnotationPresent(Default.class)) {
                    continue;
                }

                if (testExecute.getMethod().isAnnotationPresent(Error.class)) {
                    if (!best.getMethod().isAnnotationPresent(Default.class)) {
                        continue;
                    }
                }

                best = testExecute;
            }
        }

        // If we have no best then send to default
        if (best == null) {
            best = getDefaultExecute(command, context);
        }

        return best;
    }

    /**
     * Execution for methods
     */
    protected CommandExecute executeMethod(Method method, BaseCommand command, List<String> input, CommandContext context) {
        List<CommandExecute> commandExecutes = new ArrayList<>();

        for (Arg methodArgs : method.getAnnotationsByType(Arg.class)) {
            List<String> currentInput = new ArrayList<>(input);
            List<ArgNode> currentArgs = ArgNode.parse(String.join(" ", methodArgs.value()));
            CommandContext currentContext = context.copy();

            try {
                parseArg(currentArgs, currentInput, currentContext);

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
                commandExecutes.add(new CommandExecute(command, method, results, currentContext));
            } catch (ParserRequiredArgumentException ignored) {
            } catch (SwitchNotFoundException e) {
                commandExecutes.add(getErrorExecute(command, "Invalid switch: " + e.getSwitchName(), currentContext));
            } catch (ParserInvalidResultException e) {
                commandExecutes.add(getErrorExecute(command, e.getMessage(), currentContext));
            }
        }

        // Return best execute
        CommandExecute best = null;
        for (CommandExecute testExecute : commandExecutes.stream().filter(Objects::nonNull).collect(Collectors.toList())) {
            // Always replace best when its null
            if (best == null) {
                best = testExecute;
                continue;
            }

            // A longer chain always replaces a shorter chain
            if (best.getContext().getParsers().size() < testExecute.getContext().getParsers().size()) {
                best = testExecute;
                continue;
            }

            // Equal chains rely on priority
            if (best.getContext().getParsers().size() == testExecute.getContext().getParsers().size()) {
                if (testExecute.getMethod().isAnnotationPresent(Default.class)) {
                    continue;
                }

                if (testExecute.getMethod().isAnnotationPresent(Error.class)) {
                    if (!best.getMethod().isAnnotationPresent(Default.class)) {
                        continue;
                    }
                }

                best = testExecute;
            }
        }

        return best;
    }

    public List<Candidate> complete(BaseCommand command, List<String> input, CommandContext context) {
        List<Candidate> ret = new ArrayList<>();

        // Go through class Args first as long as it's not our commandroot command to allow @Command to override @Args
        if (command.getClass().getAnnotationsByType(Arg.class).length > 0) {
            for (Arg classArgs : command.getClass().getAnnotationsByType(Arg.class)) {
                List<String> currentInput = new ArrayList<>(input);
                List<ArgNode> currentArgs = ArgNode.parse(String.join(" ", classArgs.value()));
                CommandContext currentContext = context.copy();


                try {
                    parseArg(currentArgs, currentInput, currentContext, false);
                } catch (ParserRequiredArgumentException | ParserInvalidResultException e) {
                    // End of chain so save completions if no more input
                    if (currentInput.size() == 0) {
                        ret.addAll(e.getParser().getCompletions());
                    }
                    continue;
                } catch (SwitchNotFoundException e) {
                    // List switch options
                    ret.addAll(currentContext.getSwitches().stream()
                            .flatMap(sw -> Arrays.stream(sw.getParameter("switch").split("\\|"))
                                    .filter(s -> s.toLowerCase().startsWith(e.getSwitchName().toLowerCase()))
                                    .map(s -> new Object() {
                                        final String name = s;
                                        final Parser parser = sw;
                                    })
                                    .limit(1)
                            )
                            .limit(20)
                            .map(so -> new Candidate("-" + so.name, "-" + so.name, so.parser.getParameter("description", null), null))
                            .collect(Collectors.toList())
                    );
                    continue;
                }

                // Process methods
                for (Method method : command.getClass().getDeclaredMethods()) {
                    ret.addAll(completeMethod(method, command, currentInput, currentContext));
                }

                // Check each child class as well
                CommandManager.CommandConfig<?> cc = manager.getCommands().get(command.getClass());
                currentContext.getCommandStack().push(command);

                if (cc != null) {
                    for (BaseCommand child : cc.getChildren()) {
                        ret.addAll(complete(child, currentInput, currentContext));
                    }
                }
            }
        } else {
            List<String> currentInput = new ArrayList<>(input);
            CommandContext currentContext = context.copy();

            // Process methods
            for (Method method : command.getClass().getDeclaredMethods()) {
                ret.addAll(completeMethod(method, command, currentInput, currentContext));
            }

            // Check each child class as well
            CommandManager.CommandConfig<?> cc = manager.getCommands().get(command.getClass());
            currentContext.getCommandStack().push(command);

            if (cc != null) {
                for (BaseCommand child : cc.getChildren()) {
                    ret.addAll(complete(child, currentInput, currentContext));
                }
            }
        }

        // Remove duplicates and order alphabetically
        ret = new ArrayList<>(new HashSet<>(ret));
        ret.sort(Comparator.comparing(s -> s.getTitle().toLowerCase()));

        return ret;
    }

    /**
     * Completion for methods
     */
    protected List<Candidate> completeMethod(Method method, BaseCommand command, List<String> input, CommandContext context) {
        List<Candidate> ret = new ArrayList<>();
        for (Arg methodArgs : method.getAnnotationsByType(Arg.class)) {
            List<String> currentInput = new ArrayList<>(input);
            List<ArgNode> currentArgs = ArgNode.parse(String.join(" ", methodArgs.value()));
            CommandContext currentContext = context.copy();

            try {
                parseArg(currentArgs, currentInput, currentContext, false);
            } catch (ParserRequiredArgumentException | ParserInvalidResultException e) {
                // End of chain so save completion if no more input
                if (currentInput.size() == 0) {
                    ret.addAll(e.getParser().getCompletions());

                    if (currentContext.getCurrentParser().getParameter("switch", null) == null) {
                        if (currentInput.stream().allMatch(s -> s.equals("")) && (input.size() == 0 || input.get(input.size() - 1).equals(""))) {
                            // Add switches
                            ret.addAll(currentContext.getSwitches().stream()
                                    .flatMap(sw -> Arrays.stream(sw.getParameter("switch").split("\\|"))
                                            .map(s -> new Object() {
                                                final String name = s;
                                                final Parser parser = sw;
                                            })
                                            .limit(1)
                                    )
                                    .limit(20)
                                    .map(so -> new Candidate("-" + so.name, "-" + so.name, so.parser.getParameter("description", null), null))
                                    .collect(Collectors.toList())
                            );
                        }
                    }

                }


            } catch (SwitchNotFoundException e) {
                // List switch options
                ret.addAll(currentContext.getSwitches().stream()
                        .flatMap(sw -> Arrays.stream(sw.getParameter("switch").split("\\|"))
                                .filter(s -> s.toLowerCase().startsWith(e.getSwitchName().toLowerCase()))
                                .map(s -> new Object() {
                                    final String name = s;
                                    final Parser parser = sw;
                                })
                                .limit(1)
                        )
                        .limit(20)
                        .map(so -> new Candidate("-" + so.name, "-" + so.name, so.parser.getParameter("description", null), null))
                        .collect(Collectors.toList())
                );
            }
        }
        return ret;
    }

    protected void parseArg(List<ArgNode> argNodes, List<String> input, CommandContext context) throws ParserInvalidResultException, ParserRequiredArgumentException, SwitchNotFoundException {
        parseArg(argNodes, input, context, true);
    }

    protected void parseSwitches(List<String> input, CommandContext context, boolean defaults) throws SwitchNotFoundException, ParserRequiredArgumentException, ParserInvalidResultException {
        while (input.size() > 0 && input.get(0).startsWith("-")) {
            String name = input.remove(0).substring(1);
            Parser parser = context.getSwitches().stream()
                    .flatMap(s -> Arrays.stream(s.getParameter("switch").split("\\|"))
                            .filter(sw -> sw.equalsIgnoreCase(name))
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

    protected void parseArg(List<ArgNode> argNodes, List<String> input, CommandContext context, boolean defaults) throws ParserRequiredArgumentException, ParserInvalidResultException, SwitchNotFoundException {
        while (argNodes.size() > 0) {
            ArgNode node = argNodes.remove(0);


            Parser parser = getParser(node, context);
            if (parser == null) {
                break;
            }

            context.getParsers().add(parser);

            // Take care of switches first
            if (node.getParameters().containsKey("switch")) {
                context.getSwitches().add(parser);
            } else {
                // Handle switches
                parseSwitches(input, context, defaults);

                context.setCurrentParser(parser);

                parser.parse(input, defaults);
                parser.getResult();
            }
        }

        // Handle any remaining switches
        parseSwitches(input, context, defaults);
    }


}
