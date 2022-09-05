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
import au.com.grieve.bcf.annotations.Default;
import au.com.grieve.bcf.annotations.Error;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;

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

//    public BungeeCommand sendMessage(CommandSender sender, BaseComponent... components) {
//        boolean isMultiline = TextComponent.toLegacyText(components).contains("\n");
//        ComponentBuilder cb = new ComponentBuilder();
//        // If we are console we'll add a newline first to make things neater when multiline output
//        if (isMultiline && !(sender instanceof ProxiedPlayer)) {
//            cb.append("\n");
//        }
//        sender.sendMessage(cb.append(components).create());
//        return this;
//    }

}
