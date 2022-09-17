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

package au.com.grieve.bcf.impl.framework.annotation;

import au.com.grieve.bcf.*;
import au.com.grieve.bcf.exception.EndOfLineException;
import au.com.grieve.bcf.framework.annotation.annotations.Command;
import au.com.grieve.bcf.framework.annotation.annotations.Description;
import au.com.grieve.bcf.impl.completion.DefaultCompletionCandidate;
import au.com.grieve.bcf.impl.completion.StaticCompletionCandidateGroup;
import au.com.grieve.bcf.impl.line.DefaultParsedLine;
import au.com.grieve.bcf.impl.parser.DoubleParser;
import au.com.grieve.bcf.impl.parser.FloatParser;
import au.com.grieve.bcf.impl.parser.IntegerParser;
import au.com.grieve.bcf.impl.parser.StringParser;
import lombok.Builder;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class AnnotationCommandManager implements CommandManager<AnnotationCommand> {
    private final Map<String, CommandConfig> commandsByName = new HashMap<>();
    private final Map<String, String> commandAliases = new HashMap<>();
    private final Map<Class<? extends AnnotationCommand>, Set<AnnotationCommand>> commmandInstances = new HashMap<>();
    private final Map<String, Class<? extends Parser<?>>> parsers = new HashMap<>();

    @Builder
    protected static class CommandConfig {
        AnnotationCommand command;
        String description;
        ArgumentParserChain prefixParserChain;
        String input;
    }

    public AnnotationCommandManager() {
        registerDefaultParsers();
    }

    /**
     * Register default parsers
     */
    protected void registerDefaultParsers() {
        registerParser("literal", StringParser.class);
        registerParser("string", StringParser.class);
        registerParser("int", IntegerParser.class);
        registerParser("float", FloatParser.class);
        registerParser("double", DoubleParser.class);
    }

    /**
     * Register a parser with a name
     * @param name name of parser
     * @param parserClass Parser Class
     */
    public void registerParser(String name, Class<? extends Parser<?>> parserClass) {
        this.parsers.put(name, parserClass);
    }

    /**
     * Add command instance
     * When child commands are added later we use this to know where to add the children to
     * @param command Command to add
     */
    protected void addInstance(AnnotationCommand command) {
        Set<AnnotationCommand> instances = this.commmandInstances.computeIfAbsent(command.getClass(), k -> new HashSet<>());
        instances.add(command);
    }

    /**
     * Register a root command
     * @param command Command to register
     */
    @Override
    public void registerCommand(AnnotationCommand command) {
        if (!command.getClass().isAnnotationPresent(Command.class)) {
            throw new RuntimeException("Missing required @Command");
        }

        if (command.getClass().getAnnotation(Command.class).value().strip().equals("")) {
            throw new RuntimeException("Empty @Command");
        }

        addCommand(command);
        addInstance(command);
    }

    /**
     * Add new command
     * Can be overridden to allow hooking into other command managers
     * @param command Command to add
     */
    protected void addCommand(AnnotationCommand command) {
        Command commandData = command.getClass().getAnnotation(Command.class);
        String[] commandValue = commandData.value().strip().split(" ",2);

        List<String> commandNames = List.of(
                commandValue[0].split("\\|")
        );

        if (!commandsByName.containsKey(commandNames.get(0))) {
            commandsByName.put(commandNames.get(0), CommandConfig.builder()
                    .command(command)
                    .prefixParserChain(commandValue.length > 1 ? new ArgumentParserChain(getParsers(), commandValue[1]) : null)
                    .input(commandData.input())
                    .description(command.getClass().isAnnotationPresent(Description.class) ?
                            command.getClass().getAnnotation(Description.class).value() :
                            null
                    )
                    .build());
        }

        // Add aliases that don't exist yet
        commandAliases.putAll(commandNames.stream()
                .skip(1)
                .filter(s -> !commandAliases.containsKey(s))
                .collect(Collectors.toMap(s -> s, s -> commandNames.get(0)))
        );
    }


    /**
     * Find a command by name
     * @param name Name
     * @return Command that matches name else null
     */
    protected CommandConfig findCommand(String name) {
        return commandsByName.getOrDefault(name, commandsByName.get(commandAliases.get(name)));
    }

    /**
     * Register subcommand
     * @param parent Parent command class
     * @param command Command to register
     */
    @Override
    public void registerCommand(Class<? extends AnnotationCommand> parent, AnnotationCommand command) {
        Set<AnnotationCommand> instances = this.commmandInstances.get(parent);

        if (command.getClass().isAnnotationPresent(Command.class)) {
            addCommand(command);
        }

        if (instances != null) {
            addInstance(command);
            instances.forEach(c -> c.addChild(command));
        }
    }

    @Override
    public void complete(String line, List<CompletionCandidateGroup> candidates) {
        complete(new DefaultParsedLine(line), candidates);
    }

    @Override
    public ExecutionCandidate execute(String line) {
       return execute(new DefaultParsedLine(line));
    }

    @Override
    public void complete(ParsedLine line, List<CompletionCandidateGroup> candidates) {
        // If at the end of the line we do the completion against command names
        if (line.isEol() || line.size() == 1) {
            // Store groups with the commands so that we can group aliases properly
            Map<String, CompletionCandidateGroup> commandGroups = new HashMap<>();
            String input = line.isEol() ? "" : line.getCurrentWord();

            for (Map.Entry<String, CommandConfig> item : commandsByName.entrySet()) {
                if (!item.getKey().startsWith(input)) {
                    continue;
                }
                CompletionCandidateGroup group = new StaticCompletionCandidateGroup(item.getValue().description);

                // Add command name
                group.getCompletionCandidates().add(new DefaultCompletionCandidate(item.getKey()));

                commandGroups.put(item.getKey(), group);
            }

            // Add any aliases that match input
            for (Map.Entry<String, String> item : commandAliases.entrySet()) {
                if (!item.getKey().startsWith(input) || commandsByName.containsKey(item.getKey())) {
                    continue;
                }

                // Check if we already have a group, otherwise create one now
                CompletionCandidateGroup group = commandGroups.getOrDefault(
                        item.getValue(),
                        new StaticCompletionCandidateGroup(commandsByName.get(item.getValue()).description)
                );
                commandGroups.put(item.getValue(), group);

                group.getCompletionCandidates().add(new DefaultCompletionCandidate(item.getKey()));
            }
            candidates.addAll(commandGroups.values());
            return;
        }

        String commandName;
        try {
            commandName = line.next();
        } catch (EndOfLineException e) {
            return;
        }

        CommandConfig commandConfig = findCommand(commandName);
        if (commandConfig == null) {
            return;
        }

        // Prepend any additional input from the command to the input
        if (commandConfig.input.length() > 0) {
            line.insert(commandConfig.input);
        }

        AnnotationCompletionContext context = AnnotationCompletionContext.builder()
                .prefixParserChain(commandConfig.prefixParserChain)
                .parserClasses(getParsers())
                .build();

        commandConfig.command.complete(line, candidates, context);
    }

    @Override
    public ExecutionCandidate execute(ParsedLine line) {
        String commandName;
        try {
            commandName = line.next();
        } catch (EndOfLineException e) {
            return null;
        }

        CommandConfig commandConfig = findCommand(commandName);
        if (commandConfig == null) {
            return null;
        }

        // Prepend any additional input from the command to the input
        if (commandConfig.input.length() > 0) {
            line.insert(commandConfig.input);
        }

        AnnotationExecuteContext context = AnnotationExecuteContext.builder()
                .prefixParserChain(commandConfig.prefixParserChain)
                .parserClasses(getParsers())
                .build();

        return commandConfig.command.execute(line, context);

    }
}
