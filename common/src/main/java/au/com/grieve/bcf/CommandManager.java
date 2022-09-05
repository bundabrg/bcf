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

import au.com.grieve.bcf.annotations.Command;
import au.com.grieve.bcf.parsers.*;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public abstract class CommandManager<
        BC extends BaseCommand,
        RT extends CommandRoot
        > {

    protected final Map<Class<? extends BaseCommand>, CommandConfig<RT>> commands = new HashMap<>();
    protected final Map<String, Class<? extends Parser>> parsers = new HashMap<>();

    public CommandManager() {
        // Register Default Parsers
        registerParser("string", StringParser.class);
        registerParser("int", IntegerParser.class);
        registerParser("double", DoubleParser.class);
        registerParser("float", FloatParser.class);
    }

    @SuppressWarnings("unused")
    public void registerCommand(BC cmd) {
        // As a root command cmd needs to have a @Command annotation
        if (cmd.getClass().getAnnotation(Command.class) == null) {
            throw new RuntimeException("Missing required @Command");
        }

        CommandConfig<RT> commandConfig = commands.getOrDefault(cmd.getClass(), new CommandConfig<>());

        commandConfig.setCommandRoot(createCommandRoot(cmd));

        commandConfig.getInstances().add(cmd);
        commands.put(cmd.getClass(), commandConfig);
    }

    @SuppressWarnings("unused")
    public void registerSubCommand(Class<? extends BC> parentClass, BC cmd) {
        // Make sure parentClass is registered
        CommandConfig<RT> parentCommandConfig = commands.get(parentClass);

        if (parentCommandConfig == null) {
            throw new RuntimeException("Parent class is not registered");
        }

        parentCommandConfig.getChildren().add(cmd);

        // If cmd has @Command, it is a CommandRoot
        CommandConfig<RT> commandConfig = commands.getOrDefault(cmd.getClass(), new CommandConfig<>());
        if (cmd.getClass().getAnnotation(Command.class) != null) {
            commandConfig.setCommandRoot(createCommandRoot(cmd));
        }

        commandConfig.getInstances().add(cmd);
        commands.put(cmd.getClass(), commandConfig);
    }

    protected abstract RT createCommandRoot(BaseCommand cmd);

    public Parser getParser(ArgNode argNode, CommandContext context) {
        Class<? extends Parser> cls;
        if (argNode.getName().startsWith("@")) {
            cls = getParsers().getOrDefault(argNode.getName().substring(1), LiteralParser.class);
        } else {
            cls = LiteralParser.class;
        }

        try {
            return cls
                    .getConstructor(CommandManager.class, ArgNode.class, CommandContext.class)
                    .newInstance(this, argNode, context);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void registerParser(String name, Class<? extends Parser> parser) {
        this.parsers.put(name, parser);
    }

    @SuppressWarnings("unused")
    public void unregisterParser(String name) {
        this.parsers.remove(name);
    }

    @Getter
    protected static class CommandConfig<RT> {
        private final List<BaseCommand> instances = new ArrayList<>();
        private final List<BaseCommand> children = new ArrayList<>();
        @Setter
        private RT commandRoot;
    }


}
