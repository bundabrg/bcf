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

package au.com.grieve.bcf.platform.minecraft.bungeecord.impl.parser;

import au.com.grieve.bcf.CompletionCandidateGroup;
import au.com.grieve.bcf.ParsedLine;
import au.com.grieve.bcf.ParserContext;
import au.com.grieve.bcf.exception.ParserSyntaxException;
import au.com.grieve.bcf.impl.parser.BaseParser;
import au.com.grieve.bcf.platform.minecraft.bungeecord.impl.error.InsufficientPermissionError;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

/**
 * This is a special parser that allows one to prevent access to a specific pathway based on the
 * player's permission.
 *
 * <p>Normally you would use the @Permission annotation which does this using a require tag but is a
 * blunt instrument. If you require fine-grained control you can add this parser to the chain
 */
@Getter
@ToString(callSuper = true)
public class HasPermissionParser extends BaseParser<CommandSender, Void> {
  private final List<String> permission = new ArrayList<>();

  public HasPermissionParser(Map<String, String> parameters) {
    super(parameters);
    permission.addAll(List.of(parameters.getOrDefault("permission", "").split("\\|")));
  }

  public HasPermissionParser(
      String description,
      String defaultValue,
      boolean suppress,
      boolean required,
      String placeholder,
      List<String> switchValue,
      List<String> permission) {
    super(description, defaultValue, suppress, required, placeholder, switchValue);
    this.permission.addAll(permission);
  }

  @Override
  protected Void doParse(ParserContext<CommandSender> context, ParsedLine line)
      throws ParserSyntaxException {

    if (ProxyServer.getInstance().getConfig().equals(context.getData())
        || permission.stream().anyMatch(context.getData()::hasPermission)) {
      return null;
    }

    throw new ParserSyntaxException(line, new InsufficientPermissionError());
  }

  @Override
  protected void doComplete(
      ParserContext<CommandSender> context,
      ParsedLine line,
      List<CompletionCandidateGroup> candidates) {}
}
