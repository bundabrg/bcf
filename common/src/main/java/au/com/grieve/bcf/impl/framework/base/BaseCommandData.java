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

package au.com.grieve.bcf.impl.framework.base;

import au.com.grieve.bcf.CommandData;
import au.com.grieve.bcf.ParserChain;
import lombok.Getter;

/** Data about a root command */
@Getter
public class BaseCommandData implements CommandData {
  // Name of the command
  private final String name;

  // Aliases of command, if any
  private final String[] aliases;

  // Description of the command
  private final String description;

  // A parserChain to prepend to any children
  private final ParserChain parserChain;

  // Input to prepend to any supplied input
  private final String input;

  public BaseCommandData(
      String name, String[] aliases, String description, ParserChain parserChain, String input) {

    this.name = name;
    this.aliases = aliases;
    this.description = description;
    this.parserChain = parserChain;
    this.input = input;
  }
}
