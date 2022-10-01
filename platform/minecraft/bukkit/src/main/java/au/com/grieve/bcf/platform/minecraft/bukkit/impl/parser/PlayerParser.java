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

package au.com.grieve.bcf.platform.minecraft.bukkit.impl.parser;

import au.com.grieve.bcf.CompletionCandidateGroup;
import au.com.grieve.bcf.ParsedLine;
import au.com.grieve.bcf.ParserContext;
import au.com.grieve.bcf.exception.EndOfLineException;
import au.com.grieve.bcf.exception.ParserSyntaxException;
import au.com.grieve.bcf.impl.completion.DefaultCompletionCandidate;
import au.com.grieve.bcf.impl.completion.StaticCompletionCandidateGroup;
import au.com.grieve.bcf.impl.parser.BaseParser;
import au.com.grieve.bcf.platform.minecraft.bukkit.impl.error.ConsoleRequiresPlayerNameError;
import au.com.grieve.bcf.platform.minecraft.bukkit.impl.error.PlayerNotFoundError;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

@Getter
@ToString(callSuper = true)
public class PlayerParser extends BaseParser<CommandSender, Player> {
  private final String mode; // TODO enum it

  public PlayerParser(Map<String, String> parameters) {
    super(parameters);
    this.mode = parameters.getOrDefault("mode", "offline");
  }

  public PlayerParser(
      String description,
      String defaultValue,
      boolean suppress,
      boolean required,
      String placeholder,
      List<String> switchValue,
      String mode) {
    super(description, defaultValue, suppress, required, placeholder, switchValue);
    this.mode = mode != null ? mode : "offline";
  }

  @Override
  protected Player doParse(ParserContext<CommandSender> context, ParsedLine line)
      throws EndOfLineException, ParserSyntaxException {
    String input = line.next();

    if (input.equals("%self")) {
      if (context.getData() instanceof ConsoleCommandSender) {
        throw new ParserSyntaxException(line, new ConsoleRequiresPlayerNameError());
      }

      return (Player) context.getData(); // TODO what if we're running as /execute ?
    }

    if (getMode().equals("online")) {
      return Bukkit.getOnlinePlayers().stream()
          .filter(p -> p.getName().equalsIgnoreCase(input))
          .findFirst()
          .orElseThrow(() -> new ParserSyntaxException(line, new PlayerNotFoundError(input)));
    } else {
      return (Player)
          Arrays.stream(Bukkit.getOfflinePlayers())
              .filter(p -> p.getName() != null)
              .filter(p -> p.getName().equalsIgnoreCase(input))
              .findFirst()
              .orElseThrow(() -> new ParserSyntaxException(line, new PlayerNotFoundError(input)));
    }
  }

  @Override
  protected void doComplete(
      ParserContext<CommandSender> context,
      ParsedLine line,
      List<CompletionCandidateGroup> candidates)
      throws EndOfLineException {
    String input = line.next();

    if (getMode().equals("online")) {
      CompletionCandidateGroup group = new StaticCompletionCandidateGroup(input, getDescription());
      group
          .getCompletionCandidates()
          .addAll(
              Bukkit.getOnlinePlayers().stream()
                  .map(HumanEntity::getName)
                  .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                  .limit(20)
                  .map(DefaultCompletionCandidate::new)
                  .collect(Collectors.toList()));
      candidates.add(group);
    } else {
      CompletionCandidateGroup group = new StaticCompletionCandidateGroup(input, getDescription());
      group
          .getCompletionCandidates()
          .addAll(
              Arrays.stream(Bukkit.getOfflinePlayers())
                  .map(OfflinePlayer::getName)
                  .filter(Objects::nonNull)
                  .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
                  .limit(20)
                  .map(DefaultCompletionCandidate::new)
                  .collect(Collectors.toList()));
      candidates.add(group);
    }
  }
}
