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

import java.util.Collection;
import java.util.stream.Stream;

public interface CommandErrorCollection {

  /**
   * Add a new error
   *
   * @param error Error to add
   * @param line Current line
   * @param weight weight of error
   */
  void add(CommandError error, ParsedLine line, int weight);

  void addAll(CommandErrorCollection collection);

  /**
   * Retrieve list of errors
   *
   * @return list of errors
   */
  Collection<CommandError> get();

  /**
   * Return the current weight
   *
   * @return weight
   */
  int getWeight();

  /**
   * Return the current line
   *
   * @return Parsed Line
   */
  ParsedLine getLine();

  /**
   * Return formatted string
   *
   * @return Error string
   */
  String format();

  /** Clear errors */
  void clear();

  Stream<CommandError> stream();

  /**
   * Return number of errors
   *
   * @return number of errors
   */
  int size();

  /**
   * Return a copy of ourself
   *
   * @return Clone
   */
  CommandErrorCollection copy();
}
