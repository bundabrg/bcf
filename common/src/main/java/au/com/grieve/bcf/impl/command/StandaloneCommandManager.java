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

package au.com.grieve.bcf.impl.command;

import au.com.grieve.bcf.CommandData;
import au.com.grieve.bcf.CommandRootData;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class StandaloneCommandManager<DATA> extends BaseCommandManager<DATA> {
  private final Map<String, CommandRootData<DATA>> commandsByName = new HashMap<>();
  private final Map<String, String> commandAliases = new HashMap<>();

  @Override
  protected void addCommand(CommandData<DATA> commandData) {
    commandData
        .getCommandRootData()
        .forEach(
            c -> {
              // TODO Handle collisions
              if (!commandsByName.containsKey(c.getName())) {
                commandsByName.put(c.getName(), c);

                // Add aliases that don't exist yet
                commandAliases.putAll(
                    Arrays.stream(c.getAliases())
                        .filter(s -> !commandAliases.containsKey(s))
                        .collect(Collectors.toMap(s -> s, s -> c.getName())));
              }
            });
  }

  /**
   * Find a command by name
   *
   * @param name Name
   * @return Command that matches name else null
   */
  //  protected Command<DATA> findCommand(String name) {
  //    return commandsByName.getOrDefault(name, commandsByName.get(commandAliases.get(name)));
  //  }
  //
  //  protected List<CompletionCandidateGroup> complete(ParsedLine line, DATA data) {
  //    // If at the end of the line we do the completion against command names
  //    if (line.isEol() || line.size() == 1) {
  //      // Store groups with the commands so that we can group aliases properly
  //      Map<String, CompletionCandidateGroup> commandGroups = new HashMap<>();
  //      String input = line.isEol() ? "" : line.getCurrentWord();
  //
  //      for (Map.Entry<String, Command<DATA>> item : commandsByName.entrySet()) {
  //        if (!item.getKey().startsWith(input)) {
  //          continue;
  //        }
  //
  //        CompletionCandidateGroup group =
  //            new StaticCompletionCandidateGroup(
  //                input, item.getValue().getCommandData().getDescription());
  //
  //        // Add command name
  //        group.getCompletionCandidates().add(new DefaultCompletionCandidate(item.getKey()));
  //
  //        commandGroups.put(item.getKey(), group);
  //      }
  //
  //      // Add any aliases that match input
  //      for (Map.Entry<String, String> item : commandAliases.entrySet()) {
  //        if (!item.getKey().startsWith(input) || commandsByName.containsKey(item.getKey())) {
  //          continue;
  //        }
  //
  //        // Check if we already have a group, otherwise create one now
  //        CompletionCandidateGroup group =
  //            commandGroups.getOrDefault(
  //                item.getValue(),
  //                new StaticCompletionCandidateGroup(
  //                    input,
  // commandsByName.get(item.getValue()).getCommandData().getDescription()));
  //        commandGroups.put(item.getValue(), group);
  //
  //        group.getCompletionCandidates().add(new DefaultCompletionCandidate(item.getKey()));
  //      }
  //      return new ArrayList<>(commandGroups.values());
  //    }
  //
  //    String commandName;
  //    try {
  //      commandName = line.next();
  //    } catch (EndOfLineException e) {
  //      return new ArrayList<>();
  //    }
  //
  //    Command<DATA> command = findCommand(commandName);
  //    if (command == null) {
  //      return new ArrayList<>();
  //    }
  //    CommandData commandData = command.getCommandData();
  //
  //    // Prepend any additional input from the command to the input
  //    if (commandData.getInput().length() > 0) {
  //      line.insert(commandData.getInput());
  //    }
  //
  //    return command.complete(line, data);
  //  }
  //
  //  protected Executor<Object> execute(ParsedLine line, DATA data) {
  //    String commandName;
  //    try {
  //      commandName = line.next();
  //    } catch (EndOfLineException e) {
  //      return null;
  //    }
  //
  //    Command<DATA> command = findCommand(commandName);
  //    if (command == null) {
  //      return null;
  //    }
  //    CommandData commandData = command.getCommandData();
  //
  //    // Prepend any additional input from the command to the input
  //    if (commandData.getInput().length() > 0) {
  //      line.insert(commandData.getInput());
  //    }
  //
  //    return command.execute(line, data);
  //  }
  //

}
