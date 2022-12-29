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
import au.com.grieve.bcf.CompletionCandidateGroup;
import au.com.grieve.bcf.NoParserCommand;
import au.com.grieve.bcf.ParsedLine;
import au.com.grieve.bcf.Parser;
import au.com.grieve.bcf.ParserTree;
import au.com.grieve.bcf.ParserTreeContext;
import au.com.grieve.bcf.ParserTreeResult;
import au.com.grieve.bcf.impl.command.BaseCommandRootData.BaseCommandRootDataBuilder;
import au.com.grieve.bcf.impl.error.DefaultErrorCollection;
import au.com.grieve.bcf.impl.parser.StringParser;
import au.com.grieve.bcf.impl.parser.StringParser.Mode;
import au.com.grieve.bcf.impl.parsertree.NullNode;
import au.com.grieve.bcf.impl.parsertree.ParserNode;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/** Allows one to provide a simple command using just an execute and complete method */
@Getter
public abstract class SimpleCommand<DATA> extends BaseCommand<DATA>
    implements NoParserCommand<DATA> {
  private final String name;
  private final String[] aliases;
  private final String description;

  public SimpleCommand(String name, String description, String... aliases) {
    this.name = name;
    this.aliases = aliases;
    this.description = description;
  }

  public SimpleCommand(String name, String description) {
    this(name, description, new String[0]);
  }

  public SimpleCommand(String name) {
    this(name, null);
  }

  /**
   * Executes the command
   *
   * @param data Data Variable
   * @param line The parsed line executed
   */
  public abstract void execute(DATA data, @NotNull ParsedLine line);

  /**
   * Executed on tab completion for this command, returning a list of options the player can tab
   * through.
   *
   * @param data Data variable
   * @param line The parsed line executed
   * @throws IllegalArgumentException if sender, alias, or args is null
   */
  public void complete(
      DATA data, @NotNull ParsedLine line, List<CompletionCandidateGroup> completions) {}

  @Override
  public CommandData<DATA> buildCommand() {

    // Build Root Node
    @SuppressWarnings("unchecked")
    ParserTree<DATA> rootNode =
        new ParserNode<>(
                (Parser<DATA, ?>)
                    new StringParser(null, null, false, true, false, null, null, null, Mode.GREEDY))
            .execute(ctx -> execute(ctx.getData(), ctx.getLine()))
            .complete(ctx -> complete(ctx.getData(), ctx.getLine(), ctx.getCompletions()))
            .fallback(this::handleChildren);

    return new DefaultCommandData<>(
        List.of(
            new BaseCommandRootDataBuilder<DATA>()
                .root(new NullNode<>())
                .description(description)
                .name(name)
                .aliases(aliases)
                .build()),
        rootNode);
  }

  protected ParserTreeResult<DATA> handleChildren(ParserTreeContext<DATA> ctx) {
    if (children.size() > 0) {
      return children.parse(ctx);
    }
    return new ParserTreeResult<>(null, null, new DefaultErrorCollection(), new ArrayList<>());
  }
}
