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

package au.com.grieve.bcf.platform.bukkit.impl.parsertree;

import au.com.grieve.bcf.ParserTreeContext;
import au.com.grieve.bcf.ParserTreeResult;
import au.com.grieve.bcf.impl.parsertree.NullNode;
import au.com.grieve.bcf.platform.bukkit.impl.error.InsufficientPermissionError;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

/** Check permissions */
public class PermissionNode extends NullNode<CommandSender> {

  protected List<String> permissions = new ArrayList<>();

  /**
   * Add permission to check
   *
   * @param permissions Permission to check
   * @return Ourselves
   */
  public PermissionNode permission(String... permissions) {
    return this.permission(List.of(permissions));
  }

  /**
   * Add permission to check
   *
   * @param permissions Permission to check
   * @return Ourselves
   */
  public PermissionNode permission(List<String> permissions) {
    this.permissions.addAll(permissions);
    return this;
  }

  @Override
  public @NotNull ParserTreeResult<CommandSender> parse(ParserTreeContext<CommandSender> context) {
    CommandSender sender = context.getData();
    if (sender instanceof ConsoleCommandSender
        || permissions.stream().anyMatch(sender::hasPermission)) {
      return super.parse(context);
    }
    ParserTreeResult<CommandSender> ret = ParserTreeResult.EMPTY_RESULT();
    ret.getErrors().add(new InsufficientPermissionError(), context.getLine(), context.getWeight());
    return ret;
  }
}
