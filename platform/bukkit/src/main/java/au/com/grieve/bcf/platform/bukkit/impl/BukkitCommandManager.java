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

package au.com.grieve.bcf.platform.bukkit.impl;

import au.com.grieve.bcf.Command;
import au.com.grieve.bcf.CommandData;
import au.com.grieve.bcf.impl.command.BaseCommandManager;
import au.com.grieve.bcf.platform.bukkit.impl.command.BukkitCommandRootData;
import au.com.grieve.bcf.platform.bukkit.impl.parser.HasPermissionParser;
import au.com.grieve.bcf.platform.bukkit.impl.parser.PlayerParser;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;

/** Uses the built-in CommandMap to implement commands */
@Getter
public class BukkitCommandManager extends BaseCommandManager<CommandSender> {
  private final JavaPlugin plugin;
  private final CommandMap commandMap;

  public BukkitCommandManager(JavaPlugin plugin) {
    this.plugin = plugin;
    this.commandMap = hookCommandMap();
  }

  @Override
  protected void registerDefaultParsers() {
    super.registerDefaultParsers();
    registerParser("player", PlayerParser.class);
    registerParser("has_permission", HasPermissionParser.class);
  }

  /** Hook into the Bukkit Command Map */
  protected CommandMap hookCommandMap() {
    CommandMap commandMap;
    Server server = Bukkit.getServer();
    Method getCommandMap;
    try {
      getCommandMap = server.getClass().getDeclaredMethod("getCommandMap");
      getCommandMap.setAccessible(true);
      commandMap = (CommandMap) getCommandMap.invoke(server);
      Field knownCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");
      knownCommands.setAccessible(true);
    } catch (NoSuchMethodException
        | IllegalAccessException
        | InvocationTargetException
        | NoSuchFieldException e) {
      throw new RuntimeException("Cannot Hook CommandMap", e);
    }

    return commandMap;
  }

  @Override
  protected void addCommand(Command<CommandSender> command) {
    CommandData<CommandSender> commandData = commandDataMap.get(command);
    commandData
        .getCommandRootData()
        .forEach(
            c -> {
              // Handle Empty Command
              if (c.getName() == null || c.getName().isEmpty()) {
                throw new RuntimeException("Unable to add empty or null command");
              }

              // Join its root node to the commands root node (should this be done in the command
              // itself?)
              c.getRoot().forEachLeaf(l -> l.then(commandData.getRoot()));

              plugin.getLogger().info("Registering command: " + c.getName());
              BukkitCommandShim shim = new BukkitCommandShim((BukkitCommandRootData) c);
              commandMap.register(c.getName(), plugin.getName().toLowerCase(), shim);
            });
  }

  @Override
  protected void removeCommand(Command<CommandSender> command) {
    // Bukkit CommandMap does not support unregistering a command
    CommandData<CommandSender> commandData = commandDataMap.get(command);

    commandData
        .getCommandRootData()
        .forEach(
            crd ->
                plugin
                    .getLogger()
                    .warning("Unable to properly unregister command: " + crd.getName()));
  }
}
