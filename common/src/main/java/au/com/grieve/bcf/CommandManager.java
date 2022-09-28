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

public interface CommandManager<DATA> {

  /**
   * Register a command
   *
   * @param command Command to register
   */
  void registerCommand(Command<DATA> command);

  /**
   * Register a Sub-Command to all existing instances of a parent class
   *
   * @param parent Parent command class
   * @param command Command to register
   */
  void registerCommand(Class<? extends Command<DATA>> parent, Command<DATA> command);

  /**
   * Unregister a command
   *
   * @param command Command to unregister
   */
  void unregisterCommand(Command<DATA> command);

  /**
   * Unregister a command using its class
   *
   * @param command Command class to unregister
   */
  void unregisterCommand(Class<? extends Command<DATA>> command);
}
