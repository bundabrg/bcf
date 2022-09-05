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
import au.com.grieve.bcf.Candidate;
import au.com.grieve.bcf.CommandExecute;
import au.com.grieve.bcf.CommandRoot;
import au.com.grieve.bcf.annotations.Permission;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BukkitCommandRoot extends CommandRoot {
    public BukkitCommandRoot(BukkitCommandManager manager, BaseCommand cmd) {
        super(manager, cmd);
    }

    /**
     * Retrieve List of permissions
     */
    public String[] getPermissions(BaseCommand command) {
        return Arrays.stream(command.getClass().getAnnotationsByType(Permission.class))
                .map(Permission::value)
                .toArray(String[]::new);
    }

    public String[] getPermissions() {
        return getPermissions(getCommand());
    }

    /**
     * Return true if class permits permission of sender
     */
    public boolean testPermission(BaseCommand command, CommandSender sender, boolean unknown) {
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
    public CommandExecute execute(BaseCommand command, List<String> input, BukkitCommandContext context) {
        if (testPermission(command, context.getSender(), true)) {
            return super.execute(command, input, context);
        }

        return null;
    }

    @SuppressWarnings("unused")
    protected CommandExecute executeMethod(Method method, BaseCommand command, List<String> input, BukkitCommandContext context) {
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
    public List<Candidate> complete(BaseCommand command, List<String> input, BukkitCommandContext context) {
        if (testPermission(command, context.getSender(), true)) {
            return super.complete(command, input, context);
        }

        return new ArrayList<>();
    }

    @SuppressWarnings("unused")
    protected List<Candidate> completeMethod(Method method, BaseCommand command, List<String> input, BukkitCommandContext context) {
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
