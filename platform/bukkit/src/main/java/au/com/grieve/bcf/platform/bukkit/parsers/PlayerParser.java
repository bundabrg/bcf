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

package au.com.grieve.bcf.platform.bukkit.parsers;

import au.com.grieve.bcf.ArgNode;
import au.com.grieve.bcf.CommandContext;
import au.com.grieve.bcf.CommandManager;
import au.com.grieve.bcf.exceptions.ParserInvalidResultException;
import au.com.grieve.bcf.parsers.SingleParser;
import au.com.grieve.bcf.platform.bukkit.BukkitCommandContext;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Name of a player
 * <p>
 * Parameters:
 * mode:
 * any - (default) Any player
 * online - Only online players
 */
public class PlayerParser extends SingleParser {

    public PlayerParser(CommandManager<?, ?> manager, ArgNode argNode, CommandContext context) {
        super(manager, argNode, context);
    }

    @Override
    protected Object result() throws ParserInvalidResultException {
        switch (getParameter("mode", "offline")) {
            case "online":
                if (getInput().equals("%self")) {
                    CommandSender sender = ((BukkitCommandContext) context).getSender();
                    if (sender instanceof ConsoleCommandSender) {
                        throw new ParserInvalidResultException(this, "When console a player name is required");
                    }
                    return sender;
                }

                return Bukkit.getOnlinePlayers().stream()
                        .filter(p -> p.getName().equalsIgnoreCase(getInput()))
                        .findFirst()
                        .orElseThrow(() -> new ParserInvalidResultException(this, "No such player can be found online"));
            case "offline":
                if (getInput().equals("%self")) {
                    CommandSender sender = ((BukkitCommandContext) context).getSender();
                    if (sender instanceof ConsoleCommandSender) {
                        throw new ParserInvalidResultException(this, "When console a player name is required");
                    }

                    return Bukkit.getOfflinePlayer(((Player) ((BukkitCommandContext) context).getSender()).getUniqueId());
                }

                return Arrays.stream(Bukkit.getOfflinePlayers())
                        .filter(p -> p.getName() != null)
                        .filter(p -> Objects.equals(p.getName().toLowerCase(), getInput().toLowerCase()))
                        .findFirst()
                        .orElseThrow(() -> new ParserInvalidResultException(this, "No such player can be found"));
        }

        throw new ParserInvalidResultException(this, "Invalid mode: " + getParameter("mode"));
    }

    @Override
    protected List<String> complete() {
        switch (getParameter("mode", "offline")) {
            case "online":
                return Bukkit.getOnlinePlayers().stream()
                        .map(HumanEntity::getName)
                        .filter(s -> s.toLowerCase().startsWith(getInput().toLowerCase()))
                        .limit(20)
                        .collect(Collectors.toList());
            case "offline":
                return Arrays.stream(Bukkit.getOfflinePlayers())
                        .map(OfflinePlayer::getName).filter(Objects::nonNull)
                        .filter(s -> s.toLowerCase().startsWith(getInput().toLowerCase()))
                        .limit(20)
                        .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
