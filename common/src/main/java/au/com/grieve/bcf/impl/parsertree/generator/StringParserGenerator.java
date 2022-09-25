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

package au.com.grieve.bcf.impl.parsertree.generator;

import au.com.grieve.bcf.ParserTree;
import au.com.grieve.bcf.StringParserClassRegister;
import au.com.grieve.bcf.impl.parsertree.NullNode;
import au.com.grieve.bcf.impl.parsertree.ParserNode;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class StringParserGenerator<DATA> {

  private final StringParserClassRegister<DATA> register;

  public StringParserGenerator(StringParserClassRegister<DATA> register) {
    this.register = register;
  }

  protected ParserNode<DATA> createNode(String name, Map<String, String> parameters) {
    // If name doesn't start with @ then treat it as a literal
    if (!name.startsWith("@")) {
      parameters.put("options", name);
      if (!parameters.containsKey("suppress")) {
        parameters.put("suppress", "true");
      }
      name = "literal";
    } else {
      name = name.substring(1);
    }
    return new ParserNode<>(register.createParser(name, parameters));
  }

  /**
   * Generate tree node(s) from the string argument(s)
   *
   * @param input Input arguments
   * @return the generated tree else null
   */
  public ParserTree<DATA> from(String input) {
    ParserTree<DATA> root = new NullNode<>();
    ParserTree<DATA> current = root;

    try (StringReader reader = new StringReader(input)) {
      State state = State.NAME;
      Map<String, String> parameters = new HashMap<>();
      StringBuilder name = new StringBuilder();
      StringBuilder key = new StringBuilder();
      StringBuilder value = new StringBuilder();
      char quote = ' ';

      while (true) {
        int i = reader.read();
        if (i < 0) {
          if (state == State.NAME && name.length() > 0) {
            ParserNode<DATA> node = createNode(name.toString(), new HashMap<>());
            current.then(node);
          }
          break;
        }
        char c = (char) i;

        switch (state) {
          case NAME:
            switch (" (".indexOf(c)) {
              case 0: // Next Argument
                if (name.length() > 0) {
                  ParserNode<DATA> node = createNode(name.toString(), new HashMap<>());
                  current.then(node);
                  current = node;
                  name = new StringBuilder();
                }
                break;
              case 1:
                state = State.PARAM_KEY;
                parameters = new HashMap<>();
                key = new StringBuilder();
                break;
              default:
                name.append(c);
            }
            continue;
          case PARAM_KEY:
            if ("=".indexOf(c) == 0) {
              state = State.PARAM_VALUE;
              value = new StringBuilder();
            } else {
              key.append(c);
            }
            continue;
          case PARAM_VALUE:
            switch (",)\"'".indexOf(c)) {
              case 0:
                parameters.put(key.toString().trim(), value.toString().trim());
                key = new StringBuilder();
                state = State.PARAM_KEY;
                break;
              case 1:
                parameters.put(key.toString().trim(), value.toString().trim());
                ParserNode<DATA> node = createNode(name.toString(), parameters);
                current.then(node);
                current = node;
                name = new StringBuilder();
                state = State.PARAM_END;
                break;
              case 2:
              case 3:
                if (value.length() == 0) {
                  quote = c;
                  state = State.PARAM_VALUE_QUOTE;
                }
                break;
              default:
                value.append(c);
            }
            continue;
          case PARAM_VALUE_QUOTE:
            switch ("\"'\\".indexOf(c)) {
              case 0:
              case 1:
                if (c == quote) {
                  parameters.put(key.toString().trim(), value.toString().trim());
                  key = new StringBuilder();
                  state = State.PARAM_VALUE_QUOTE_END;
                } else {
                  value.append(c);
                }
                break;
              case 2:
                i = reader.read();
                if (i < 0) {
                  break;
                }
                value.append((char) i);
                break;
              default:
                value.append(c);
            }
            continue;
          case PARAM_VALUE_QUOTE_END:
            switch (",)".indexOf(c)) {
              case 0:
                state = State.PARAM_KEY;
                break;
              case 1:
                ParserNode<DATA> node = createNode(name.toString(), parameters);
                current.then(node);
                current = node;
                name = new StringBuilder();
                state = State.PARAM_END;
                break;
            }
            continue;
          case PARAM_END:
            if (" ".indexOf(c) == 0) {
              state = State.NAME;
            }
            // continue;
        }
      }
    } catch (IOException ignored) {

    }

    return root;
  }

  private enum State {
    NAME,
    PARAM_KEY,
    PARAM_VALUE,
    PARAM_VALUE_QUOTE,
    PARAM_VALUE_QUOTE_END,
    PARAM_END
  }
}
