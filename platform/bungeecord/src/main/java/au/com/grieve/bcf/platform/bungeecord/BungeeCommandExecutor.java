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

import au.com.grieve.bcf.CommandExecute;
import au.com.grieve.bcf.CommandRoot;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Arrays;

public class BungeeCommandExecutor extends Command implements TabExecutor {

    final CommandRoot<BungeeCommand> commandRoot;

    public BungeeCommandExecutor(String name, CommandRoot<BungeeCommand> commandRoot, String... aliases) {
        super(name, null, aliases);
        this.commandRoot = commandRoot;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (hasPermission(sender)) {
            BungeeCommandContext context = new BungeeCommandContext(sender);
            CommandExecute commandExecute = commandRoot.execute(Arrays.asList(args), context);
            if (commandExecute != null) {
                commandExecute.invoke(sender);
            }
        }
    }


    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        BungeeCommandContext context = new BungeeCommandContext(sender);
        return commandRoot.complete(Arrays.asList(args), context);
    }
}
