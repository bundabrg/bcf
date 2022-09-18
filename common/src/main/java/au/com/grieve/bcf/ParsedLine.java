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

import au.com.grieve.bcf.exception.EndOfLineException;
import java.util.List;

public interface ParsedLine {
  /**
   * Return the full unparsed line
   *
   * @return Unparsed Line
   */
  String getLine();

  /**
   * Get list of words in line
   *
   * @return list of words
   */
  List<String> getWords();

  /**
   * Get any prefix to the line
   *
   * @return prefix
   */
  String getPrefix();

  /**
   * Get the current word being parsed
   *
   * @return current word
   */
  String getCurrentWord();

  /**
   * Get the index of current word in list of words
   *
   * @return current word index
   */
  int getWordIndex();

  /** Update word index */
  void setWordIndex(int newIndex);

  /**
   * Return true if at the end of the line
   *
   * @return true if end of line
   */
  boolean isEol();

  /**
   * Returns the size of the remaining elements
   *
   * @return size of remaining elements
   */
  int size();

  /**
   * Return current word and move word index to next word
   *
   * @return current word
   */
  String next() throws EndOfLineException;

  /**
   * Return a copy of ourself
   *
   * @return Copy
   */
  ParsedLine copy();

  void insert(String input);
}
