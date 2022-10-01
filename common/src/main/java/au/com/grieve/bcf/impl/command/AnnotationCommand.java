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

package au.com.grieve.bcf.impl.command;

import au.com.grieve.bcf.CommandData;
import au.com.grieve.bcf.CommandRootData;
import au.com.grieve.bcf.CommandRootData.CommandRootDataBuilder;
import au.com.grieve.bcf.ParserTree;
import au.com.grieve.bcf.ParserTreeContext;
import au.com.grieve.bcf.ParserTreeResult;
import au.com.grieve.bcf.StringParserClassRegister;
import au.com.grieve.bcf.StringParserCommand;
import au.com.grieve.bcf.annotation.Arg;
import au.com.grieve.bcf.annotation.Command;
import au.com.grieve.bcf.annotation.Default;
import au.com.grieve.bcf.annotation.Error;
import au.com.grieve.bcf.impl.command.BaseCommandRootData.BaseCommandRootDataBuilder;
import au.com.grieve.bcf.impl.error.DefaultErrorCollection;
import au.com.grieve.bcf.impl.parsertree.NullNode;
import au.com.grieve.bcf.impl.parsertree.generator.StringParserGenerator;
import au.com.grieve.bcf.utils.ReflectUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

/**
 * Used by the AnnotationCommandManager as a Base Command class Commands are defined by annotations
 * on methods
 */
@Getter
public class AnnotationCommand<DATA> extends BaseCommand<DATA>
    implements StringParserCommand<DATA> {

  protected List<ParserTree<DATA>> buildMethodNodes(
      Method method, StringParserGenerator<DATA> generator) {
    return Arrays.stream(method.getAnnotationsByType(Arg.class))
        .map(
            a -> {
              // Generate the TreeNode
              ParserTree<DATA> node = generator.from(String.join(" ", a.value()));

              // Add a method execute at the tree leaves
              node.forEachLeaf(
                  n ->
                      n.execute(
                          ctx ->
                              executeMethod(
                                  method,
                                  Stream.concat(
                                          ctx.getData() != null
                                              ? Stream.of(ctx.getData())
                                              : Stream.empty(),
                                          ctx.getResults().stream())
                                      .collect(Collectors.toList()))));
              return node;
            })
        .collect(Collectors.toList());
  }

  protected List<ParserTree<DATA>> buildMethodNodes(StringParserGenerator<DATA> generator) {
    return Arrays.stream(getClass().getMethods())
        .filter(m -> m.isAnnotationPresent(Arg.class))
        .flatMap(m -> buildMethodNodes(m, generator).stream())
        .collect(Collectors.toList());
  }

  protected List<ParserTree<DATA>> buildClassNodes(StringParserGenerator<DATA> generator) {
    return ReflectUtils.getAllAnnotationsByType(getClass(), Arg.class).stream()
        .map(a -> generator.from(String.join(" ", a.value())))
        .collect(Collectors.toList());
  }

  protected ParserTree<DATA> buildRootNode(StringParserGenerator<DATA> generator) {
    // Check for special methods
    Method defaultMethod =
        Arrays.stream(getClass().getMethods())
            .filter(m -> m.isAnnotationPresent(Default.class))
            .findFirst()
            .orElse(null);

    Method errorMethod =
        Arrays.stream(getClass().getMethods())
            .filter(m -> m.isAnnotationPresent(Error.class))
            .findFirst()
            .orElse(null);

    List<ParserTree<DATA>> methodNodes = buildMethodNodes(generator);
    List<ParserTree<DATA>> classNodes = buildClassNodes(generator);

    NullNode<DATA> root = new NullNode<>();
    classNodes.forEach(root::then);

    root.forEachLeaf(
        n -> {
          // Add Special Methods
          if (defaultMethod != null) {
            n.execute(
                ctx ->
                    executeMethod(
                        defaultMethod,
                        (ctx.getData() != null ? Stream.of(ctx.getData()) : Stream.empty())
                            .collect(Collectors.toList())));
          }
          if (errorMethod != null) {
            n.error(ctx -> executeMethod(errorMethod, List.of(ctx)));
          }
          n.fallback(this::handleChildren);

          // Add Method Args
          methodNodes.forEach(n::then);
        });
    return root;
  }

  protected List<CommandRootData<DATA>> buildCommandRootDataList(
      StringParserGenerator<DATA> generator) {
    List<CommandRootData<DATA>> commandRootData = new ArrayList<>();
    Arrays.stream(getClass().getAnnotationsByType(Command.class))
        .forEach(a -> commandRootData.add(buildCommandRootData(generator, a)));

    return commandRootData;
  }

  protected CommandRootDataBuilder<DATA> buildCommandRootData(
      CommandRootDataBuilder<DATA> builder,
      StringParserGenerator<DATA> generator,
      Command commandAnnotation) {
    String[] commandArgs = commandAnnotation.value().strip().split(" +", 2);
    String[] commandNames = commandArgs[0].split(("\\|"));
    ParserTree<DATA> commandRoot =
        commandArgs.length == 1 ? new NullNode<>() : generator.from(commandArgs[1]);

    return builder
        .name(commandNames[0])
        .aliases(Arrays.stream(commandNames).skip(1).toArray(String[]::new))
        .description(
            commandAnnotation.description() != null && commandAnnotation.description().length() > 0
                ? commandAnnotation.description()
                : null)
        .root(commandRoot)
        .input(commandAnnotation.input());
  }

  protected CommandRootData<DATA> buildCommandRootData(
      StringParserGenerator<DATA> generator, Command commandAnnotation) {

    BaseCommandRootDataBuilder<DATA> builder = BaseCommandRootData.builder();

    buildCommandRootData(builder, generator, commandAnnotation);
    return builder.build();
  }

  @Override
  public CommandData<DATA> buildCommand(StringParserClassRegister<DATA> register) {
    StringParserGenerator<DATA> generator = new StringParserGenerator<>(register);

    // Build Root Node
    ParserTree<DATA> rootNode = buildRootNode(generator);
    List<CommandRootData<DATA>> commandRootData = buildCommandRootDataList(generator);

    return new DefaultCommandData<>(commandRootData, rootNode);
  }

  protected ParserTreeResult<DATA> handleChildren(ParserTreeContext<DATA> ctx) {
    if (children.size() > 0) {
      return children.parse(ctx);
    }
    return new ParserTreeResult<>(
        null, null, null, new DefaultErrorCollection(), new ArrayList<>());
  }

  protected void executeMethod(Method method, List<Object> parameters) {
    List<Object> allArgs = new ArrayList<>(parameters);

    // Fill out extra parameters with null
    while (allArgs.size() < method.getParameterCount()) {
      allArgs.add(null);
    }

    try {
      method.invoke(this, allArgs.toArray());
    } catch (InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {
      throw new RuntimeException(
          "Failed to execute command: "
              + getClass().getName()
              + "#"
              + method.getName()
              + "("
              + allArgs.stream().map(Object::toString).collect(Collectors.joining(", "))
              + ")",
          e);
    }
  }
}
