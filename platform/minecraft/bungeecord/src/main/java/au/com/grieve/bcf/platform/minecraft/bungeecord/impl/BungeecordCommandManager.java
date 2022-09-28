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

package au.com.grieve.bcf.platform.minecraft.bungeecord.impl;

import au.com.grieve.bcf.Command;
import au.com.grieve.bcf.CommandData;
import au.com.grieve.bcf.impl.command.BaseCommandManager;
import au.com.grieve.bcf.platform.minecraft.bungeecord.impl.command.BungeecordCommandRootData;
import au.com.grieve.bcf.platform.minecraft.bungeecord.impl.parser.HasPermissionParser;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

/** Uses the built-in Bungeecord executor to add commands */
@Getter
public class BungeecordCommandManager extends BaseCommandManager<CommandSender> {
  private final Plugin plugin;

  private final Map<Command<CommandSender>, BungeecordCommandShim> commandShimMap = new HashMap<>();

  public BungeecordCommandManager(Plugin plugin) {
    this.plugin = plugin;
  }

  @Override
  protected void registerDefaultParsers() {
    super.registerDefaultParsers();
    registerParser("has_permission", HasPermissionParser.class);
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
              BungeecordCommandShim shim = new BungeecordCommandShim((BungeecordCommandRootData) c);

              commandShimMap.put(command, shim);

              // Register with Bungee
              plugin.getProxy().getPluginManager().registerCommand(plugin, shim);
            });
  }

  @Override
  protected void removeCommand(Command<CommandSender> command) {
    CommandData<CommandSender> commandData = commandDataMap.get(command);
    if (commandData == null) {
      return;
    }

    commandData
        .getCommandRootData()
        .forEach(
            crd -> {
              plugin.getLogger().info("Unregistering command: " + crd.getName());
              plugin.getProxy().getPluginManager().unregisterCommand(commandShimMap.get(command));
              commandShimMap.remove(command);
            });
  }
}
