/*
 * Copyright (c) 2020-2020 Brendan Grieve (bundabrg) - MIT License
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

import lombok.Getter;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ArgNode {
    @Getter
    final String name;
    @Getter
    final Map<String, String> parameters = new HashMap<>();


    public ArgNode(String name) {
        this.name = name;
    }

    public ArgNode(String name, Map<String, String> parameters) {
        this(name);
        this.parameters.putAll(parameters);
    }

    public static List<ArgNode> parse(String input) {
        return parse(new StringReader(input));
    }

    /**
     * Parse a string and return new Data Nodes
     */
    public static List<ArgNode> parse(StringReader reader) {
        List<ArgNode> result = new ArrayList<>();

        State state = State.NAME;
        StringBuilder name = new StringBuilder();
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        Map<String, String> parameters = new HashMap<>();

        int i;
        char quote = ' ';

        do {
            try {
                i = reader.read();
            } catch (IOException e) {
                break;
            }

            if (i < 0) {
                break;
            }

            char c = (char) i;

            switch (state) {
                case NAME:
                    switch (" (".indexOf(c)) {
                        case 0:
                            if (name.length() > 0) {
                                result.add(new ArgNode(name.toString()));
                                name = new StringBuilder();
                                break;
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
                    break;
                case PARAM_KEY:
                    //noinspection SwitchStatementWithTooFewBranches
                    switch ("=".indexOf(c)) {
                        case 0:
                            state = State.PARAM_VALUE;
                            value = new StringBuilder();
                            break;
                        default:
                            key.append(c);
                    }
                    break;
                case PARAM_VALUE:
                    switch (",)\"'".indexOf(c)) {
                        case 0:
                            parameters.put(key.toString().trim(), value.toString().trim());
                            key = new StringBuilder();
                            state = State.PARAM_KEY;
                            break;
                        case 1:
                            parameters.put(key.toString().trim(), value.toString().trim());
                            result.add(new ArgNode(name.toString(), parameters));
                            name = new StringBuilder();
                            state = State.PARAM_END;
                            break;
                        case 2:
                        case 3:
                            if (value.length() == 0) {
                                quote = c;
                                state = State.PARAM_VALUE_QUOTE;
                                break;
                            }
                            break;
                        default:
                            value.append(c);
                    }
                    break;
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
                            value.append(c);
                            try {
                                i = reader.read();
                            } catch (IOException e) {
                                break;
                            }
                            if (i < 0) {
                                break;
                            }
                            value.append((char) i);
                            break;
                        default:
                            value.append(c);
                    }
                    break;
                case PARAM_VALUE_QUOTE_END:
                    switch (",)".indexOf(c)) {
                        case 0:
                            state = State.PARAM_KEY;
                            break;
                        case 1:
                            result.add(new ArgNode(name.toString(), parameters));
                            name = new StringBuilder();
                            state = State.PARAM_END;
                            break;
                    }
                    break;
                case PARAM_END:
                    //noinspection SwitchStatementWithTooFewBranches
                    switch (" ".indexOf(c)) {
                        case 0:
                            state = State.NAME;
                            break;
                    }
                    break;
            }

        } while (true);

        if (state == State.NAME && name.length() > 0) {
            result.add(new ArgNode(name.toString()));
        }

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ArgNode)) {
            return false;
        }

        ArgNode data = (ArgNode) obj;

        return data.getName().equals(name);
    }

    @Override
    public String toString() {
        return name + "(" + parameters.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ")) + ")";
    }

    enum State {
        NAME,
        PARAM_KEY,
        PARAM_VALUE,
        PARAM_VALUE_QUOTE,
        PARAM_VALUE_QUOTE_END,
        PARAM_END
    }

}
