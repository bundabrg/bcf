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

package au.com.grieve.bcf.platform.bukkit.impl;

import au.com.grieve.bcf.CommandRootData;
import au.com.grieve.bcf.CompletionCandidate;
import au.com.grieve.bcf.CompletionCandidateGroup;
import au.com.grieve.bcf.ErrorContext;
import au.com.grieve.bcf.ParsedLine;
import au.com.grieve.bcf.ParserTree;
import au.com.grieve.bcf.ParserTreeResult;
import au.com.grieve.bcf.impl.line.DefaultParsedLine;
import au.com.grieve.bcf.impl.parsertree.NullNode;
import au.com.grieve.bcf.platform.bukkit.impl.command.BukkitCommandRootData;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class BukkitCommandShim extends Command {

  private final CommandRootData<CommandSender> commandRootData;

  public BukkitCommandShim(BukkitCommandRootData commandRootData) {
    super(
        commandRootData.getName(),
        commandRootData.getDescription(),
        "/" + commandRootData.getName(),
        Arrays.asList(commandRootData.getAliases()));
    setPermission(String.join(";", commandRootData.getPermissions()));
    this.commandRootData = commandRootData;
  }

  protected void errorHandler(ErrorContext<CommandSender> ctx) {
    ctx.getData()
        .spigot()
        .sendMessage(new ComponentBuilder(ctx.getErrors().format()).color(ChatColor.RED).create());
  }

  protected ParserTreeResult<CommandSender> parse(ParsedLine line, CommandSender sender) {
    ParserTree<CommandSender> root =
        new NullNode<CommandSender>().error(this::errorHandler).then(commandRootData.getRoot());

    // Prepend any additional input from the command to the input
    if (commandRootData.getInput().length() > 0) {
      line.insert(commandRootData.getInput());
    }

    return root.parse(line, sender);
  }

  @Override
  public boolean execute(@NotNull CommandSender sender, @NotNull String alias, String[] args) {
    ParsedLine line = new DefaultParsedLine(args, "/" + alias);
    parse(line, sender).execute();
    return true;
  }

  @Override
  public @NotNull List<String> tabComplete(
      @NotNull CommandSender sender, @NotNull String alias, String[] args)
      throws IllegalArgumentException {

    ParsedLine line = new DefaultParsedLine(args, "/" + alias);

    return parse(line, sender).complete().stream()
        .map(CompletionCandidateGroup::getMatchingCompletionCandidates)
        .filter(c -> c.size() > 0)
        .flatMap(Collection::stream)
        .map(CompletionCandidate::getValue)
        .collect(Collectors.toList());
  }
}
