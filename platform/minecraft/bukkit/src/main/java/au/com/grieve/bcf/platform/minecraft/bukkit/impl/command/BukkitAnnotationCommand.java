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

package au.com.grieve.bcf.platform.minecraft.bukkit.impl.command;

import au.com.grieve.bcf.CommandRootData;
import au.com.grieve.bcf.CommandRootData.CommandRootDataBuilder;
import au.com.grieve.bcf.ParserTree;
import au.com.grieve.bcf.annotation.Command;
import au.com.grieve.bcf.impl.command.AnnotationCommand;
import au.com.grieve.bcf.impl.parsertree.generator.StringParserGenerator;
import au.com.grieve.bcf.platform.minecraft.bukkit.annotation.Permission;
import au.com.grieve.bcf.platform.minecraft.bukkit.impl.command.BukkitCommandRootData.BukkitCommandRootDataBuilder;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.CommandSender;

public class BukkitAnnotationCommand extends AnnotationCommand<CommandSender> {

  @Override
  protected CommandRootDataBuilder<CommandSender> buildCommandRootData(
      CommandRootDataBuilder<CommandSender> builder,
      StringParserGenerator<CommandSender> generator,
      Command commandAnnotation) {

    // Add permissions on root
    ((BukkitCommandRootData.BukkitCommandRootDataBuilder) builder)
        .permissions(
            Arrays.stream(getClass().getAnnotationsByType(Permission.class))
                .map(Permission::value)
                .toArray(String[]::new));

    return super.buildCommandRootData(builder, generator, commandAnnotation);
  }

  @Override
  protected CommandRootData<CommandSender> buildCommandRootData(
      StringParserGenerator<CommandSender> generator, Command commandAnnotation) {

    BukkitCommandRootDataBuilder builder = BukkitCommandRootData.bukkitBuilder();

    buildCommandRootData(builder, generator, commandAnnotation);
    return builder.build();
  }

  @Override
  protected List<ParserTree<CommandSender>> buildMethodNodes(
      Method method, StringParserGenerator<CommandSender> generator) {
    List<ParserTree<CommandSender>> nodes = super.buildMethodNodes(method, generator);

    // Add permissions if any - TODO what to do if require already exists?
    Permission[] permissions = method.getAnnotationsByType(Permission.class);
    if (permissions.length > 0) {
      nodes.forEach(
          n ->
              n.requires(
                  ctx ->
                      ctx.getData().isOp()
                          || Boolean.TRUE.equals(
                              Arrays.stream(permissions)
                                  .filter(p -> ctx.getData().hasPermission(p.value()))
                                  .map(p -> true)
                                  .findFirst()
                                  .orElse(false))));
    }

    return nodes;
  }

  @Override
  protected List<ParserTree<CommandSender>> buildClassNodes(
      StringParserGenerator<CommandSender> generator) {
    List<ParserTree<CommandSender>> nodes = super.buildClassNodes(generator);

    // Add permissions if any - TODO what to do if require already exists?
    Permission[] permissions = getClass().getAnnotationsByType(Permission.class);
    if (permissions.length > 0) {
      nodes.forEach(
          n ->
              n.requires(
                  ctx ->
                      ctx.getData().isOp()
                          || Boolean.TRUE.equals(
                              Arrays.stream(permissions)
                                  .filter(p -> ctx.getData().hasPermission(p.value()))
                                  .map(p -> true)
                                  .findFirst()
                                  .orElse(null))));
    }

    return nodes;
  }
}
