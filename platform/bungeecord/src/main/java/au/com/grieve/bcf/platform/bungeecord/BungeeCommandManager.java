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

package au.com.grieve.bcf.platform.bungeecord;

import au.com.grieve.bcf.CommandManager;
import au.com.grieve.bcf.annotations.Command;
import net.md_5.bungee.api.plugin.Plugin;

@SuppressWarnings("unused")
public class BungeeCommandManager extends CommandManager<BungeeCommand, BungeeCommandRoot> {

    private final Plugin plugin;

    public BungeeCommandManager(Plugin plugin) {
        super();
        this.plugin = plugin;

        // Register Default Parsers
//        registerParser("player", PlayerParser.class);
    }

    @Override
    protected BungeeCommandRoot createCommandRoot(BungeeCommand cmd) {
        BungeeCommandRoot cr = new BungeeCommandRoot(this, cmd);

        // Get Name and Aliases
        Command commandAnnotation = cmd.getClass().getAnnotation(Command.class);

        if (commandAnnotation == null) {
            return cr;
        }

        String[] aliases = commandAnnotation.value().split("\\|");
        if (aliases.length == 0) {
            aliases = new String[]{cmd.getClass().getSimpleName().toLowerCase()};
        }

        // Register with Bungee
        BungeeCommandExecutor bungeeCommandExecutor = new BungeeCommandExecutor(aliases[0], cr, aliases);
        plugin.getProxy().getPluginManager().registerCommand(plugin, bungeeCommandExecutor);

        return cr;
    }

}
