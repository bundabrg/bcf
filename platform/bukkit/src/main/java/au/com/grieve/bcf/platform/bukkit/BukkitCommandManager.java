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

package au.com.grieve.bcf.platform.bukkit;

import au.com.grieve.bcf.BaseCommand;
import au.com.grieve.bcf.CommandManager;
import au.com.grieve.bcf.annotations.Command;
import au.com.grieve.bcf.platform.bukkit.parsers.PlayerParser;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

@SuppressWarnings("unused")
public class BukkitCommandManager extends CommandManager<
        BukkitCommand, BukkitCommandRoot
        > {

    private final JavaPlugin plugin;
    private final CommandMap commandMap;

    public BukkitCommandManager(JavaPlugin plugin) {
        super();
        this.plugin = plugin;
        this.commandMap = hookCommandMap();

        // Register Default Parsers
        registerParser("player", PlayerParser.class);
    }

    /**
     * Hook into the Bukkit Command Map
     */
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

    @Override
    protected BukkitCommandRoot createCommandRoot(BaseCommand cmd) {
        BukkitCommandRoot cr = new BukkitCommandRoot(this, cmd);

        // Get Name and Aliases
        Command commandAnnotation = cmd.getClass().getAnnotation(Command.class);

        if (commandAnnotation == null) {
            return cr;
        }

        String[] aliases = commandAnnotation.value().split("\\|");
        if (aliases.length == 0) {
            aliases = new String[]{cmd.getClass().getSimpleName().toLowerCase()};
        }

        // Register with Bukkit
        BukkitCommandExecutor bukkitCommandExecutor = new BukkitCommandExecutor(cr, aliases[0]);
        bukkitCommandExecutor.setAliases(Arrays.asList(aliases));
        commandMap.register(aliases[0], plugin.getName().toLowerCase(), bukkitCommandExecutor);

        return cr;
    }


}
