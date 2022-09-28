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

package au.com.grieve.bcf.platform.bungeecord.impl.command;

import au.com.grieve.bcf.CommandRootData;
import au.com.grieve.bcf.CommandRootData.CommandRootDataBuilder;
import au.com.grieve.bcf.annotation.Command;
import au.com.grieve.bcf.impl.command.AnnotationCommand;
import au.com.grieve.bcf.impl.parsertree.generator.StringParserGenerator;
import au.com.grieve.bcf.platform.bungeecord.annotation.Permission;
import au.com.grieve.bcf.platform.bungeecord.impl.command.BungeecordCommandRootData.BungeecordCommandRootDataBuilder;
import java.util.Arrays;
import net.md_5.bungee.api.CommandSender;

public class BungeecordAnnotationCommand extends AnnotationCommand<CommandSender> {

  @Override
  protected CommandRootDataBuilder<CommandSender> buildCommandRootData(
      CommandRootDataBuilder<CommandSender> builder,
      StringParserGenerator<CommandSender> generator,
      Command commandAnnotation) {

    // Add permissions on root
    ((BungeecordCommandRootData.BungeecordCommandRootDataBuilder) builder)
        .permissions(
            Arrays.stream(getClass().getAnnotationsByType(Permission.class))
                .map(Permission::value)
                .toArray(String[]::new));

    return super.buildCommandRootData(builder, generator, commandAnnotation);
  }

  @Override
  protected CommandRootData<CommandSender> buildCommandRootData(
      StringParserGenerator<CommandSender> generator, Command commandAnnotation) {

    BungeecordCommandRootDataBuilder builder = BungeecordCommandRootData.bungeecordBuilder();

    buildCommandRootData(builder, generator, commandAnnotation);
    return builder.build();
  }
}
