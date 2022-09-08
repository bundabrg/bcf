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

package au.com.grieve.bcf.platform.terminalconsole;

import au.com.grieve.bcf.core.BaseCommand;
import au.com.grieve.bcf.core.CommandManager;
import au.com.grieve.bcf.framework.annotation.annotations.Command;
import au.com.grieve.bcf.framework.annotation.annotations.Description;

public class TerminalCommandManager extends CommandManager<TerminalCommand, TerminalCommandRoot> {

    private final TerminalConsole console;

    public TerminalCommandManager(TerminalConsole console) {
        super();
        this.console = console;
    }

    @Override
    protected TerminalCommandRoot createCommandRoot(BaseCommand cmd) {
        TerminalCommandRoot cr = new TerminalCommandRoot(this, cmd);

        // Get Name and Aliases
        Command commandAnnotation = cmd.getClass().getAnnotation(Command.class);

        if (commandAnnotation == null) {
            return cr;
        }

        String description = cmd.getClass().isAnnotationPresent(Description.class) ?
                cmd.getClass().getAnnotation(Description.class).value() :
                null;

        String[] aliases = commandAnnotation.value().split("\\|");
        if (aliases.length == 0) {
            aliases = new String[]{cmd.getClass().getSimpleName().toLowerCase()};
        }

        // Register with Bukkit
        TerminalCommandExecutor terminalCommandExecutor = new TerminalCommandExecutor(cr, aliases[0], description);
        console.getCommandMap().register(aliases[0], terminalCommandExecutor, aliases);

        return cr;
    }


}
