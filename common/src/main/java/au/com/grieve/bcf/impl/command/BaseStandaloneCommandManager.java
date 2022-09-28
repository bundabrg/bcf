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

import au.com.grieve.bcf.Command;
import au.com.grieve.bcf.CommandData;
import au.com.grieve.bcf.CommandErrorCollection;
import au.com.grieve.bcf.CommandRootData;
import au.com.grieve.bcf.CompleteContext;
import au.com.grieve.bcf.CompletionCandidateGroup;
import au.com.grieve.bcf.ErrorContext;
import au.com.grieve.bcf.ParsedLine;
import au.com.grieve.bcf.ParserTree;
import au.com.grieve.bcf.ParserTreeCandidate;
import au.com.grieve.bcf.ParserTreeHandler;
import au.com.grieve.bcf.ParserTreeResult;
import au.com.grieve.bcf.StandaloneCommandManager;
import au.com.grieve.bcf.exception.EndOfLineException;
import au.com.grieve.bcf.impl.completion.DefaultCompletionCandidate;
import au.com.grieve.bcf.impl.completion.StaticCompletionCandidateGroup;
import au.com.grieve.bcf.impl.error.DefaultErrorCollection;
import au.com.grieve.bcf.impl.error.InputExpectedError;
import au.com.grieve.bcf.impl.error.UnknownCommandError;
import au.com.grieve.bcf.impl.line.DefaultParsedLine;
import au.com.grieve.bcf.impl.parsertree.NullNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public class BaseStandaloneCommandManager<DATA> extends BaseCommandManager<DATA>
    implements StandaloneCommandManager<DATA> {
  private final Map<String, CommandRootData<DATA>> commandsByName = new HashMap<>();
  private final Map<String, String> commandAliases = new HashMap<>();

  protected ParserTreeHandler<ErrorContext<DATA>> getBaseErrorHandler() {
    return null;
  }

  protected ParserTreeHandler<CompleteContext<DATA>> getBaseCompleteHandler() {
    return null;
  }

  @Override
  protected void addCommand(Command<DATA> command) {
    CommandData<DATA> commandData = commandDataMap.get(command);
    commandData
        .getCommandRootData()
        .forEach(
            c -> {
              // Handle Empty Command
              if (c.getName() == null || c.getName().isEmpty()) {
                throw new RuntimeException("Unable to add empty or null command");
              }

              // TODO Handle collisions
              if (!commandsByName.containsKey(c.getName())) {
                commandsByName.put(c.getName(), c);

                // Add aliases that don't exist yet
                commandAliases.putAll(
                    Arrays.stream(c.getAliases())
                        .filter(s -> !commandAliases.containsKey(s))
                        .collect(Collectors.toMap(s -> s, s -> c.getName())));

                // Join its root node to the commands root node (should this be done in the command
                // itself?)
                c.getRoot().forEachLeaf(l -> l.then(commandData.getRoot()));
              }
            });
  }

  @Override
  protected void removeCommand(Command<DATA> command) {
    CommandData<DATA> commandData = commandDataMap.get(command);

    commandData
        .getCommandRootData()
        .forEach(
            crd -> {
              List<String> aliases =
                  commandAliases.entrySet().stream()
                      .filter(es -> es.getValue().equals(crd.getName()))
                      .map(Entry::getKey)
                      .collect(Collectors.toList());

              aliases.forEach(commandAliases::remove);
              commandsByName.remove(crd.getName());
            });
  }

  /**
   * Find a command by name
   *
   * @param name Name
   * @return Command that matches name else null
   */
  protected CommandRootData<DATA> findCommand(String name) {
    return commandsByName.getOrDefault(name, commandsByName.get(commandAliases.get(name)));
  }

  public ParserTreeResult<DATA> parse(String line, DATA data) {
    return this.parse(new DefaultParsedLine(line), data);
  }

  protected List<CompletionCandidateGroup> getCommandCompletions(String input) {
    return commandsByName.entrySet().stream()
        .map(
            e -> {
              CompletionCandidateGroup group =
                  new StaticCompletionCandidateGroup(input, e.getValue().getDescription());
              group
                  .getCompletionCandidates()
                  .addAll(
                      Stream.concat(
                              Stream.of(e.getKey()),
                              commandAliases.entrySet().stream()
                                  .filter(e2 -> e2.getValue().equals(e.getKey()))
                                  .filter(e2 -> !commandsByName.containsKey(e2.getKey()))
                                  .map(Entry::getKey))
                          .map(DefaultCompletionCandidate::new)
                          .collect(Collectors.toList()));
              return group;
            })
        .collect(Collectors.toList());
  }

  public ParserTreeResult<DATA> parse(ParsedLine line, DATA data) {
    String commandName;
    try {
      commandName = line.next();
    } catch (EndOfLineException ex) {
      CommandErrorCollection errors = new DefaultErrorCollection();
      errors.add(new InputExpectedError(), line, 0);
      ParserTreeCandidate<ErrorContext<DATA>, DATA> errorCandidate =
          getBaseErrorHandler() != null
              ? new ParserTreeCandidate<>(
                  line,
                  getBaseErrorHandler(),
                  new ArrayList<>(),
                  errors,
                  getCommandCompletions(""),
                  data,
                  0)
              : null;
      ParserTreeCandidate<CompleteContext<DATA>, DATA> completeCandidate =
          getBaseCompleteHandler() != null
              ? new ParserTreeCandidate<>(
                  line,
                  getBaseCompleteHandler(),
                  new ArrayList<>(),
                  errors,
                  getCommandCompletions(""),
                  data,
                  0)
              : null;
      return new ParserTreeResult<>(
          null, errorCandidate, completeCandidate, errors, getCommandCompletions(""));
    }

    CommandRootData<DATA> command = findCommand(commandName);
    if (command == null) {
      CommandErrorCollection errors = new DefaultErrorCollection();
      errors.add(new UnknownCommandError(), line, 0);

      ParserTreeCandidate<ErrorContext<DATA>, DATA> errorCandidate =
          getBaseErrorHandler() != null
              ? new ParserTreeCandidate<>(
                  line,
                  getBaseErrorHandler(),
                  new ArrayList<>(),
                  errors,
                  getCommandCompletions(commandName),
                  data,
                  0)
              : null;
      ParserTreeCandidate<CompleteContext<DATA>, DATA> completeCandidate =
          getBaseCompleteHandler() != null
              ? new ParserTreeCandidate<>(
                  line,
                  getBaseCompleteHandler(),
                  new ArrayList<>(),
                  errors,
                  getCommandCompletions(commandName),
                  data,
                  0)
              : null;
      return new ParserTreeResult<>(
          null, errorCandidate, completeCandidate, errors, getCommandCompletions(commandName));
    }

    ParserTree<DATA> root =
        new NullNode<DATA>()
            .complete(getBaseCompleteHandler())
            .error(getBaseErrorHandler())
            .then(command.getRoot());

    // Prepend any additional input from the command to the input
    if (command.getInput().length() > 0) {
      line.insert(command.getInput());
    }

    // If at EOL add completions for the command
    List<CompletionCandidateGroup> completions = new ArrayList<>();
    if (line.isEol()) {
      completions.addAll(getCommandCompletions(commandName));
    }

    ParserTreeResult<DATA> result = root.parse(line, data);
    result.getCompletions().addAll(completions);
    return result;
  }
}
