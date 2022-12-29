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
import au.com.grieve.bcf.CommandManager;
import au.com.grieve.bcf.Parser;
import au.com.grieve.bcf.StringParserClassRegister;
import au.com.grieve.bcf.StringParserCommand;
import au.com.grieve.bcf.impl.parser.DoubleParser;
import au.com.grieve.bcf.impl.parser.FloatParser;
import au.com.grieve.bcf.impl.parser.IntegerParser;
import au.com.grieve.bcf.impl.parser.StringParser;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BaseCommandManager provides many useful defaults
 *
 * @param <DATA>
 */
public abstract class BaseCommandManager<DATA>
    implements CommandManager<DATA>, StringParserClassRegister<DATA> {

  // List of instances of a particular command class.
  protected final Map<Class<?>, List<Command<DATA>>> classCommandMap = new HashMap<>();

  // The CommandData for each command
  protected final Map<Command<DATA>, CommandData<DATA>> commandDataMap = new HashMap<>();

  // Hold registered parsers to string names
  protected final Map<String, Class<? extends Parser<DATA, ?>>> parserClassMap = new HashMap<>();

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
  public void registerParser(String name, Class<? extends Parser<?, ?>> parserClass) {
    //noinspection unchecked
    this.parserClassMap.put(name, (Class<? extends Parser<DATA, ?>>) parserClass);
  }

  /** Create a new parser based upon the name. */
  public Parser<DATA, ?> createParser(String name, Map<String, String> parameters) {
    Class<? extends Parser<DATA, ?>> parserClass = parserClassMap.get(name);
    if (parserClass == null) {
      throw new RuntimeException("Unknown parser: " + name);
    }

    try {
      return parserClass.getConstructor(Map.class).newInstance(parameters);
    } catch (InstantiationException
        | NoSuchMethodException
        | InvocationTargetException
        | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  protected CommandData<DATA> buildCommandData(Command<DATA> command) {
    if (command instanceof StringParserCommand) {
      //noinspection unchecked
      return ((StringParserCommand<DATA>) command).buildCommand(this);
    }
    throw new RuntimeException("Unsupported Command Type");
  }

  /**
   * Register a root command`
   *
   * @param command Command to register
   */
  @Override
  public void registerCommand(Command<DATA> command) {
    CommandData<DATA> commandData = buildCommandData(command);
    if (commandData.getCommandRootData().size() == 0) {
      throw new RuntimeException(
          "Unable to add non-root command. Did you mean to add as a child of another command?");
    }

    commandDataMap.put(command, commandData);
    addCommand(command);

    classCommandMap.computeIfAbsent(command.getClass(), k -> new ArrayList<>()).add(command);
  }

  @Override
  public void unregisterCommand(Command<DATA> command) {
    if (!classCommandMap.containsKey(command.getClass())) {
      return;
    }
    classCommandMap.get(command.getClass()).remove(command);
    if (classCommandMap.get(command.getClass()).size() == 0) {
      classCommandMap.remove(command.getClass());
    }

    removeCommand(command);
    commandDataMap.remove(command);
  }

  @Override
  public void unregisterCommand(Class<? extends Command<DATA>> commandClass) {
    if (!classCommandMap.containsKey(commandClass)) {
      return;
    }

    List<Command<DATA>> commands = new ArrayList<>(classCommandMap.get(commandClass));
    commands.forEach(this::unregisterCommand);
  }

  protected abstract void addCommand(Command<DATA> command);

  protected abstract void removeCommand(Command<DATA> command);

  /**
   * Register subcommand
   *
   * @param parent Parent command class
   * @param command Command to register
   */
  @Override
  public void registerCommand(Class<? extends Command<DATA>> parent, Command<DATA> command) {
    CommandData<DATA> commandData = buildCommandData(command);

    // Check if we know about parent
    List<Command<DATA>> parents = classCommandMap.get(parent);
    if (parents == null) {
      throw new RuntimeException("Unable to find parent class. Have they registered yet?");
    }

    commandDataMap.put(command, commandData);

    // If a root command, add it as a command
    if (commandData.getCommandRootData().size() > 0) {
      addCommand(command);
    }

    // Add as a child to command
    parents.forEach(p -> p.then(commandData.getRoot()));
  }
}
