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

import au.com.grieve.bcf.BaseCommand;
import au.com.grieve.bcf.CommandExecute;
import au.com.grieve.bcf.CommandRoot;
import au.com.grieve.bcf.annotations.Default;
import au.com.grieve.bcf.annotations.Error;
import au.com.grieve.bcf.annotations.Permission;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BungeeCommand extends BaseCommand {

    // Default Error
    @SuppressWarnings("unused")
    @Error
    public void onError(CommandSender sender, String message) {
        sender.sendMessage(
                new ComponentBuilder(message).color(ChatColor.RED).create()
        );
    }

    // Default Default
    @SuppressWarnings("unused")
    @Default
    public void onDefault(CommandSender sender) {
        sender.sendMessage(
                new ComponentBuilder("Invalid Command").color(ChatColor.RED).create()
        );
    }

    /**
     * Return true if class permits permission of sender
     */
    public boolean testPermission(CommandSender sender) {
        Permission[] permissions = getClass().getAnnotationsByType(Permission.class);
        if (permissions.length == 0) {
            return true;
        }

        // Check Sender has any permissions
        for (Permission permission : permissions) {
            if (sender.hasPermission(permission.value())) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    public CommandExecute execute(CommandRoot<?> commandRoot, List<String> input, BungeeCommandContext context) {
        if (!testPermission(context.getSender())) {
            return null;
        }

        return super.execute(commandRoot, input, context);
    }

    @SuppressWarnings("unused")
    protected CommandExecute executeMethod(Method method, CommandRoot<?> commandRoot, List<String> input, BungeeCommandContext context) {
        Permission[] permissions = method.getAnnotationsByType(Permission.class);

        if (permissions.length > 0) {
            // Check Sender has any permissions
            for (Permission permission : permissions) {
                if (context.getSender().hasPermission(permission.value())) {
                    return super.executeMethod(method, commandRoot, input, context);
                }
            }

            return null;
        }

        return super.executeMethod(method, commandRoot, input, context);

    }

    @SuppressWarnings("unused")
    public List<String> complete(CommandRoot<?> commandRoot, List<String> input, BungeeCommandContext context) {

        Permission[] permissions = getClass().getAnnotationsByType(Permission.class);

        if (permissions.length > 0) {
            // Check Sender has any permissions
            for (Permission permission : permissions) {
                if (context.getSender().hasPermission(permission.value())) {
                    return super.complete(commandRoot, input, context);
                }
            }

            return new ArrayList<>();
        }

        return super.complete(commandRoot, input, context);
    }

    @SuppressWarnings("unused")
    protected List<String> completeMethod(Method method, CommandRoot<?> commandRoot, List<String> input, BungeeCommandContext context) {
        Permission[] permissions = method.getAnnotationsByType(Permission.class);

        if (permissions.length > 0) {
            // Check Sender has any permissions
            for (Permission permission : permissions) {
                if (context.getSender().hasPermission(permission.value())) {
                    return super.completeMethod(method, commandRoot, input, context);
                }
            }

            return new ArrayList<>();
        }

        return super.completeMethod(method, commandRoot, input, context);
    }

    public BungeeCommand sendMessage(CommandSender sender, BaseComponent... components) {
        boolean isMultiline = TextComponent.toLegacyText(components).contains("\n");
        ComponentBuilder cb = new ComponentBuilder();
        // If we are console we'll add a newline first to make things neater when multiline output
        if (isMultiline && !(sender instanceof ProxiedPlayer)) {
            cb.append("\n");
        }
        sender.sendMessage(cb.append(components).create());
        return this;
    }

}
