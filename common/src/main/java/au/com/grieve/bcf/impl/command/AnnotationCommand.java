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
import au.com.grieve.bcf.ParserTree;
import au.com.grieve.bcf.StringParserClassRegister;
import au.com.grieve.bcf.StringParserCommand;
import au.com.grieve.bcf.annotation.Arg;
import au.com.grieve.bcf.annotation.Command;
import au.com.grieve.bcf.annotation.Default;
import au.com.grieve.bcf.annotation.Error;
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

  @Override
  public CommandData<DATA> buildCommand(StringParserClassRegister<DATA> register) {
    StringParserGenerator<DATA> generator = new StringParserGenerator<>(register);

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

    // Build method nodes
    List<ParserTree<DATA>> methodNodes =
        Arrays.stream(getClass().getMethods())
            .filter(m -> m.isAnnotationPresent(Arg.class))
            .flatMap(
                m ->
                    Arrays.stream(m.getAnnotationsByType(Arg.class))
                        .map(
                            a -> {
                              // Generate the TreeNode
                              ParserTree<DATA> node = generator.from(String.join(" ", a.value()));

                              // Add a method execute at the tree leaves
                              node.forEachLeaf(
                                  n -> {
                                    n.execute(
                                        ctx ->
                                            executeMethod(
                                                m,
                                                Stream.concat(
                                                        Stream.of(ctx), ctx.getResults().stream())
                                                    .collect(Collectors.toList())));
                                  });
                              return node;
                            }))
            .collect(Collectors.toList());

    // Build Class Nodes
    List<ParserTree<DATA>> classNodes =
        ReflectUtils.getAllAnnotationsByType(getClass(), Arg.class).stream()
            .map(a -> generator.from(String.join(" ", a.value())))
            .collect(Collectors.toList());

    // Add to root node
    NullNode<DATA> root = new NullNode<>();
    classNodes.forEach(root::then);

    root.forEachLeaf(
        n -> {
          // Add Special Methods
          if (defaultMethod != null) {
            n.execute(ctx -> executeMethod(defaultMethod, List.of(ctx)));
          }
          if (errorMethod != null) {
            n.error(ctx -> executeMethod(errorMethod, List.of(ctx)));
          }

          // Add Method Args and Children
          getChildren().forEach(n::then);

          methodNodes.forEach(n::then);
        });

    List<CommandRootData<DATA>> commandRootData = new ArrayList<>();

    // If we have any command annotations then build CommandRootData
    Command commandAnnotation = getClass().getAnnotation(Command.class);
    if (commandAnnotation != null) {
      String[] commandArgs = commandAnnotation.value().strip().split(" +", 2);
      String[] commandNames = commandArgs[0].split(("\\|"));
      ParserTree<DATA> commandRoot =
          commandArgs.length == 1
              ? null
              : generator.from(commandArgs[1]).forEachLeaf(n -> n.then(root));

      commandRootData.add(
          new DefaultCommandRootData<>(
              commandNames[0],
              Arrays.stream(commandNames).skip(1).toArray(String[]::new),
              commandAnnotation.description() != null
                      && commandAnnotation.description().length() > 0
                  ? commandAnnotation.description()
                  : null,
              commandRoot,
              commandAnnotation.input()));
    }

    return new DefaultCommandData<>(commandRootData, root);
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
