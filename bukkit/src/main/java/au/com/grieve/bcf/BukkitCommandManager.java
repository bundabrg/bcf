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
import au.com.grieve.bcf.annotations.Command;
import au.com.grieve.bcf.annotations.Default;
import au.com.grieve.bcf.annotations.Error;
import au.com.grieve.bcf.parsers.PlayerParser;
import au.com.grieve.bcf.utils.ReflectUtils;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BukkitCommandManager extends CommandManager {

    private final JavaPlugin plugin;
    private final CommandMap commandMap;

    public BukkitCommandManager(JavaPlugin plugin) {
        super();
        this.plugin = plugin;
        this.commandMap = hookCommandMap();

        // Register Default Parsers
        registerParser("player", PlayerParser.class);
    }

    private CommandMap hookCommandMap() {
        CommandMap commandMap;
        Server server = Bukkit.getServer();
        Method getCommandMap;
        try {
            getCommandMap = server.getClass().getDeclaredMethod("getCommandMap");
            getCommandMap.setAccessible(true);
            commandMap = (CommandMap) getCommandMap.invoke(server);
            Field knownCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommands.setAccessible(true);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            throw new RuntimeException("Cannot Hook CommandMap", e);
        }

        return commandMap;
    }

    public void registerCommand(BaseCommand cmd) {
        // Lookup all parents
        List<Class<?>> parents = Stream
                .concat(
                        Stream.of(cmd.getClass()),
                        Stream.of(ReflectUtils.getAllSuperClasses(cmd.getClass())))
                .filter(BaseCommand.class::isAssignableFrom)
                .filter(c -> c != BaseCommand.class)
                .collect(Collectors.toList());

        // Get Full Parent Arg
        Collections.reverse(parents);
        String parentArg = parents.stream()
                .map(c -> c.getAnnotation(Arg.class))
                .filter(Objects::nonNull)
                .map(Arg::value)
                .collect(Collectors.joining(" "));

        // Get Root Command, if any
        RootCommand rootCommand = null;

        if (parents.size() > 0) {
            Class<?> rootClass = parents.get(0);
            Command commandAnnotation = rootClass.getAnnotation(Command.class);
            if (commandAnnotation != null) {
                String rootString = commandAnnotation.value();
                String[] aliases = rootString.split("\\|");

                if (aliases.length == 0) {
                    aliases = new String[]{cmd.getClass().getSimpleName().toLowerCase()};
                }

                if (!commands.containsKey(aliases[0])) {
                    BukkitRootCommand bukkitRootCommand = new BukkitRootCommand(this, aliases[0]);
                    bukkitRootCommand.setAliases(Arrays.asList(aliases));
                    commandMap.register(aliases[0], plugin.getName().toLowerCase(), bukkitRootCommand);

                    commands.put(aliases[0], bukkitRootCommand);
                    rootCommand = bukkitRootCommand;
                } else {
                    rootCommand = commands.get(aliases[0]);
                }
            }
        }

        // Get Root Node, otherwise create a new one
        ParserNode rootNode;
        if (rootCommand == null) {
            rootNode = new ParserNode();
        } else {
            rootNode = rootCommand.getNode();
        }

        // Add each method to rootNode
        for (Method m : cmd.getClass().getDeclaredMethods()) {
            Arg argAnnotation = m.getAnnotation(Arg.class);
            Default defaultAnnotation = m.getAnnotation(Default.class);
            Error errorAnnotation = m.getAnnotation(Error.class);

            String path = parentArg;

            if (argAnnotation != null && argAnnotation.value().trim().length() > 0) {
                path = path + " " + argAnnotation.value().trim();
            }

            for (ParserNode node : rootNode.create(path)) {
                if (argAnnotation != null) {
                    node.setExecute(new ParserMethod(cmd, m));
                }

                if (defaultAnnotation != null) {
                    node.setDefault(new ParserMethod(cmd, m));
                }

                if (errorAnnotation != null) {
                    node.setError(new ParserMethod(cmd, m));
                }
            }
        }
    }

}
