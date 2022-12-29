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

package au.com.grieve.bcf;

public interface CommandRootData<DATA> {
  /**
   * Get the name of the command
   *
   * @return command name
   */
  String getName();

  /**
   * Get any aliases of the command
   *
   * @return aliases to command
   */
  String[] getAliases();

  /**
   * Get the description of the command
   *
   * @return command description
   */
  String getDescription();

  /**
   * Get Command RootNode
   *
   * @return root node
   */
  ParserTree<DATA> getRoot();

  /**
   * Get input to prepend to supplied input
   *
   * @return input to prepend
   */
  String getInput();

  interface CommandRootDataBuilder<DATA> {
    CommandRootDataBuilder<DATA> name(String name);

    CommandRootDataBuilder<DATA> aliases(String[] aliases);

    CommandRootDataBuilder<DATA> description(String description);

    CommandRootDataBuilder<DATA> root(ParserTree<DATA> root);

    CommandRootDataBuilder<DATA> input(String input);
  }
}
