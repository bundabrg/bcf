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
import au.com.grieve.bcf.CommandManager;
import au.com.grieve.bcf.CommandRoot;
import au.com.grieve.bcf.annotations.Permission;
import net.md_5.bungee.api.CommandSender;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BungeeCommandRoot extends CommandRoot<BungeeCommand> {
    public BungeeCommandRoot(CommandManager<BungeeCommand, ?> manager, BungeeCommand cmd) {
        super(manager, cmd);
    }

    /**
     * Retrieve List of permissions
     */
    public String[] getPermissions(BungeeCommand command) {
        return Arrays.stream(command.getClass().getAnnotationsByType(Permission.class))
                .map(Permission::value)
                .toArray(String[]::new);
    }

    @SuppressWarnings("unused")
    public String[] getPermissions() {
        return getPermissions(getCommand());
    }

    /**
     * Return true if class permits permission of sender
     */
    public boolean testPermission(BungeeCommand command, CommandSender sender, boolean unknown) {
        String[] permissions = getPermissions(command);

        if (permissions.length > 0) {
            // Check Sender has any permissions
            for (String permission : getPermissions(command)) {
                if (sender.hasPermission(permission)) {
                    return true;
                }
            }
        } else {
            return unknown;
        }

        return false;
    }

    @SuppressWarnings("unused")
    public CommandExecute execute(BungeeCommand command, List<String> input, BungeeCommandContext context) {
        if (testPermission(command, context.getSender(), false)) {
            return super.execute(command, input, context);
        }
        return null;
    }

    @SuppressWarnings("unused")
    protected CommandExecute executeMethod(Method method, BungeeCommand command, List<String> input, BungeeCommandContext context) {
        Permission[] permissions = method.getAnnotationsByType(Permission.class);

        if (permissions.length > 0) {
            // Check Sender has any permissions
            for (Permission permission : permissions) {
                if (context.getSender().hasPermission(permission.value())) {
                    return super.executeMethod(method, command, input, context);
                }
            }

            return null;
        }

        return super.executeMethod(method, command, input, context);

    }

    @SuppressWarnings("unused")
    public List<String> complete(BungeeCommand command, List<String> input, BungeeCommandContext context) {
        if (testPermission(command, context.getSender(), false)) {
            return super.complete(command, input, context);
        }
        return new ArrayList<>();
    }

    @SuppressWarnings("unused")
    protected List<String> completeMethod(Method method, BungeeCommand command, List<String> input, BungeeCommandContext context) {
        Permission[] permissions = method.getAnnotationsByType(Permission.class);

        if (permissions.length > 0) {
            // Check Sender has any permissions
            for (Permission permission : permissions) {
                if (context.getSender().hasPermission(permission.value())) {
                    return super.completeMethod(method, command, input, context);
                }
            }

            return new ArrayList<>();
        }

        return super.completeMethod(method, command, input, context);
    }
}
