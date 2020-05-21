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

import au.com.grieve.bcf.annotations.Command;
import au.com.grieve.bcf.parsers.DoubleParser;
import au.com.grieve.bcf.parsers.FloatParser;
import au.com.grieve.bcf.parsers.IntegerParser;
import au.com.grieve.bcf.parsers.LiteralParser;
import au.com.grieve.bcf.parsers.StringParser;
import au.com.grieve.bcf.utils.ReflectUtils;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class CommandManager {

    @Getter
    protected final Map<Class<? extends BaseCommand>, CommandRoot> commandRoots = new HashMap<>();

    @Getter
    protected final Map<String, Class<? extends Parser>> parsers = new HashMap<>();

    public CommandManager() {
        // Register Default Parsers
        registerParser("string", StringParser.class);
        registerParser("int", IntegerParser.class);
        registerParser("double", DoubleParser.class);
        registerParser("float", FloatParser.class);
    }

    public void registerCommand(Class<? extends BaseCommand> cmd) {
        // Lookup all parent classes
        List<Class<?>> parents = Stream.of(ReflectUtils.getAllSuperClasses(cmd))
                .filter(BaseCommand.class::isAssignableFrom)
                .filter(c -> c != BaseCommand.class)
                .collect(Collectors.toList());
        Collections.reverse(parents);

        // If our class has a command, we are a CommandRoot
        if (cmd.getAnnotation(Command.class) != null) {
            commandRoots.put(cmd, createCommandRoot(cmd));
        }

        // Find all commandRoots and add us to them as a sub command
        for (Class<?> cls : parents) {
            CommandRoot c = commandRoots.getOrDefault(cls, null);

            if (c != null) {
                c.addSubCommand(cmd);
            }
        }
    }

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
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    CommandRoot createCommandRoot(Class<? extends BaseCommand> cmd) {
        return new CommandRoot(this, cmd);
    }

    public void registerParser(String name, Class<? extends Parser> parser) {
        this.parsers.put(name, parser);
    }

    @SuppressWarnings("unused")
    public void unregisterParser(String name) {
        this.parsers.remove(name);
    }



}
