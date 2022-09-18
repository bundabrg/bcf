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

package au.com.grieve.bcf.impl.framework.base;

import au.com.grieve.bcf.Command;
import au.com.grieve.bcf.CommandData;
import au.com.grieve.bcf.CommandManager;
import au.com.grieve.bcf.CompletionCandidateGroup;
import au.com.grieve.bcf.CompletionContext;
import au.com.grieve.bcf.ExecutionCandidate;
import au.com.grieve.bcf.ExecutionContext;
import au.com.grieve.bcf.ParsedLine;
import au.com.grieve.bcf.Parser;
import au.com.grieve.bcf.exception.EndOfLineException;
import au.com.grieve.bcf.impl.completion.DefaultCompletionCandidate;
import au.com.grieve.bcf.impl.completion.StaticCompletionCandidateGroup;
import au.com.grieve.bcf.impl.line.DefaultParsedLine;
import au.com.grieve.bcf.impl.parser.DoubleParser;
import au.com.grieve.bcf.impl.parser.FloatParser;
import au.com.grieve.bcf.impl.parser.IntegerParser;
import au.com.grieve.bcf.impl.parser.StringParser;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class BaseCommandManager<DATA> implements CommandManager<DATA> {
  private final Map<String, Command<DATA>> commandsByName = new HashMap<>();
  private final Map<String, String> commandAliases = new HashMap<>();
  private final Map<Class<?>, Set<Command<DATA>>> commandInstances = new HashMap<>();
  private final Map<String, Class<? extends Parser<?>>> parsers = new HashMap<>();

  public BaseCommandManager() {
    registerDefaultParsers();
  }

  /** Register default parsers */
  protected void registerDefaultParsers() {
    registerParser("literal", StringParser.class);
    registerParser("string", StringParser.class);
    registerParser("int", IntegerParser.class);
    registerParser("float", FloatParser.class);
    registerParser("double", DoubleParser.class);
  }

  /**
   * Register a parser with a name
   *
   * @param name name of parser
   * @param parserClass Parser Class
   */
  public void registerParser(String name, Class<? extends Parser<?>> parserClass) {
    this.parsers.put(name, parserClass);
  }

  /**
   * Add command instance When child commands are added later we use this to know where to add the
   * children to
   *
   * @param command Command to add
   */
  protected void addInstance(Command<DATA> command) {
    Set<Command<DATA>> instances =
        this.commandInstances.computeIfAbsent(command.getClass(), k -> new HashSet<>());
    instances.add(command);
  }

  /**
   * Register a root command`
   *
   * @param command Command to register
   */
  @Override
  public void registerCommand(Command<DATA> command) {
    if (command.getCommandData() == null || command.getCommandData().getName().strip().equals("")) {
      throw new RuntimeException(
          "Invalid Root Command. Perhaps this is supposed to be a child command?");
    }

    addCommand(command);
    addInstance(command);
  }

  /**
   * Add new command Can be overridden to allow hooking into other command managers
   *
   * @param command Command to add
   */
  protected void addCommand(Command<DATA> command) {
    CommandData<DATA> commandData = command.getCommandData();

    // TODO Handle collisions
    if (!commandsByName.containsKey(commandData.getName())) {
      commandsByName.put(commandData.getName(), command);

      // Add aliases that don't exist yet
      commandAliases.putAll(
          Arrays.stream(commandData.getAliases())
              .filter(s -> !commandAliases.containsKey(s))
              .collect(Collectors.toMap(s -> s, s -> commandData.getName())));
    }
  }

  /**
   * Find a command by name
   *
   * @param name Name
   * @return Command that matches name else null
   */
  protected Command<DATA> findCommand(String name) {
    return commandsByName.getOrDefault(name, commandsByName.get(commandAliases.get(name)));
  }

  /**
   * Register subcommand
   *
   * @param parent Parent command class
   * @param command Command to register
   */
  @Override
  public void registerCommand(Class<? extends Command<DATA>> parent, Command<DATA> command) {
    Set<Command<DATA>> instances = this.commandInstances.get(parent);

    // If it is a root command we add it as a command as well
    if (command.getCommandData() != null) {
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
  public ExecutionCandidate execute(String line, DATA data) {
    return execute(new DefaultParsedLine(line), data);
  }

  @Override
  public void complete(ParsedLine line, List<CompletionCandidateGroup> candidates) {
    // If at the end of the line we do the completion against command names
    if (line.isEol() || line.size() == 1) {
      // Store groups with the commands so that we can group aliases properly
      Map<String, CompletionCandidateGroup> commandGroups = new HashMap<>();
      String input = line.isEol() ? "" : line.getCurrentWord();

      for (Map.Entry<String, Command<DATA>> item : commandsByName.entrySet()) {
        if (!item.getKey().startsWith(input)) {
          continue;
        }

        CompletionCandidateGroup group =
            new StaticCompletionCandidateGroup(item.getValue().getCommandData().getDescription());

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
        CompletionCandidateGroup group =
            commandGroups.getOrDefault(
                item.getValue(),
                new StaticCompletionCandidateGroup(
                    commandsByName.get(item.getValue()).getCommandData().getDescription()));
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

    Command<DATA> command = findCommand(commandName);
    if (command == null) {
      return;
    }
    CommandData<DATA> commandData = command.getCommandData();

    // Prepend any additional input from the command to the input
    if (commandData.getInput().length() > 0) {
      line.insert(commandData.getInput());
    }

    CompletionContext<DATA> context = new BaseCompletionContext<>();
    context.getParserClasses().putAll(parsers);
    command.complete(line, candidates, context);
  }

  @Override
  public ExecutionCandidate execute(ParsedLine line, DATA data) {
    String commandName;
    try {
      commandName = line.next();
    } catch (EndOfLineException e) {
      return null;
    }

    Command<DATA> command = findCommand(commandName);
    if (command == null) {
      return null;
    }
    CommandData<DATA> commandData = command.getCommandData();

    // Prepend any additional input from the command to the input
    if (commandData.getInput().length() > 0) {
      line.insert(commandData.getInput());
    }

    ExecutionContext<DATA> context = new BaseExecutionContext<>(data);
    context.getParserClasses().putAll(parsers);

    return command.execute(line, context);
  }
}
