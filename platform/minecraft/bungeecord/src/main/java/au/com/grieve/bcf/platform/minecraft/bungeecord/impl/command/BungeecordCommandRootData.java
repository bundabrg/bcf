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

package au.com.grieve.bcf.platform.minecraft.bungeecord.impl.command;

import au.com.grieve.bcf.ParserTree;
import au.com.grieve.bcf.impl.command.BaseCommandRootData;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.CommandSender;

@Getter
@ToString
public class BungeecordCommandRootData extends BaseCommandRootData<CommandSender> {

  private final String[] permissions;

  @Builder(builderMethodName = "bungeecordBuilder")
  public BungeecordCommandRootData(
      String name,
      String[] aliases,
      String description,
      ParserTree<CommandSender> root,
      String input,
      String[] permissions) {
    super(name, aliases, description, root, input);
    this.permissions = permissions;
  }

  public static class BungeecordCommandRootDataBuilder
      implements CommandRootDataBuilder<CommandSender> {}
}
