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

import java.util.List;

/** Command - All commands inherit from this class */
public interface Command<DATA> {

  /**
   * Provide completion candidates for the input
   *
   * @param line The input
   * @param candidates List of candidates
   * @param context Context
   */
  void complete(
      ParsedLine line, List<CompletionCandidateGroup> candidates, CompletionContext<DATA> context);

  /**
   * Return the best execution candidate for the parsed input
   *
   * @param line The input
   * @param context Context
   * @return best execution method
   */
  ExecutionCandidate execute(ParsedLine line, ExecutionContext<DATA> context);

  /**
   * Add a child command to this one
   *
   * @param childCommand Child Command
   */
  void addChild(Command<DATA> childCommand);

  /**
   * Return the data for Command
   *
   * @return data for command
   */
  CommandData<DATA> getCommandData();
}
