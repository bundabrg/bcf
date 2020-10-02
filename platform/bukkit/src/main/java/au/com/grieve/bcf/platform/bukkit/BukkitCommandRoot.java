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

package au.com.grieve.bcf.platform.bukkit;

import au.com.grieve.bcf.BaseCommand;
import au.com.grieve.bcf.CommandExecute;
import au.com.grieve.bcf.CommandManager;
import au.com.grieve.bcf.CommandRoot;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class BukkitCommandRoot extends CommandRoot {
    BukkitCommandRoot(CommandManager manager, Class<? extends BaseCommand> command) {
        super(manager, command);
    }

    public boolean execute(@NotNull CommandSender sender, @NotNull String alias, String[] args) {
        BukkitCommandContext context = new BukkitCommandContext(sender);
        CommandExecute commandExecute = getCommand().execute(this, Arrays.asList(args), context);
        if (commandExecute != null) {
            commandExecute.invoke(sender);
            return true;
        }
        return false;
    }

    public @NotNull List<String> complete(@NotNull CommandSender sender, @NotNull String alias, String[] args) {
        BukkitCommandContext context = new BukkitCommandContext(sender);
        return getCommand().complete(this, Arrays.asList(args), context);
    }

    public boolean testPermission(@NotNull CommandSender sender) {
        return ((BukkitCommand) getCommand()).testPermission(sender);
    }
}
