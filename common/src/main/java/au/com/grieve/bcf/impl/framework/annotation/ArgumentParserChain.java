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

package au.com.grieve.bcf.impl.framework.annotation;

import au.com.grieve.bcf.CompletionCandidateGroup;
import au.com.grieve.bcf.ParsedLine;
import au.com.grieve.bcf.Parser;
import au.com.grieve.bcf.ParserChain;
import au.com.grieve.bcf.exception.EndOfLineException;
import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@ToString
public class ArgumentParserChain implements ParserChain {
    private enum State {
        NAME,
        PARAM_KEY,
        PARAM_VALUE,
        PARAM_VALUE_QUOTE,
        PARAM_VALUE_QUOTE_END,
        PARAM_END
    }

    private final Map<String, Class<? extends Parser<?>>> parserClasses;
    private final List<Parser<?>> parsers = new ArrayList<>();

    public ArgumentParserChain(Map<String, Class<? extends Parser<?>>> parserClasses, String input) {
        this.parserClasses = parserClasses;
        this.parsers.addAll(parseArgumentString(input));
    }

    @Override
    public void parse(ParsedLine line, List<Object> output) throws EndOfLineException, IllegalArgumentException {
        for(Parser<?> p : parsers) {
            Object result;
            try {
                result = p.parse(line);
            } catch (EndOfLineException e) {
                // Handle defaults
                if (p.getParameters().getOrDefault("required", "true").equals("false") ||
                        p.getParameters().containsKey("default")) {
                    result = p.getParameters().get("default");
                } else {
                    throw e;
                }
            }

            // Handle suppress
            if (p.getParameters().getOrDefault("suppress", "false").equals("false")) {
                output.add(result);
            }
        }
    }

    @Override
    public void complete(ParsedLine line, List<CompletionCandidateGroup> candidateGroups) throws EndOfLineException {
        for(Parser<?> p : parsers) {
            ParsedLine lineCopy = line.copy();

            try {
                p.parse(line);
            } catch (IllegalArgumentException | EndOfLineException e) {
                List<CompletionCandidateGroup> groups = new ArrayList<>();
                try {
                    p.complete(line, groups);
                    if (groups.size() != 0) {
                        candidateGroups.addAll(groups);
                    }
                } catch (IllegalArgumentException ignored) {
                }

                return;
            }
            if (line.isEol()) {
                try {
                    p.complete(lineCopy, candidateGroups);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    /**
     * Create a new parser based upon the name.
     *
     * @param name Name of parser
     * @param parameters Parameters to parser
     * @return Parser
     */
    protected Parser<?> createParser(String name, Map<String, String> parameters) {
        // If it does not start with @, then a parser called 'literal' parser will be used with options being the name
        if (!name.startsWith("@")) {
            parameters.put("options", name);
            parameters.put("suppress", parameters.getOrDefault("suppress", "true"));
            name="literal";
        } else {
            name = name.substring(1);
        }

        Class<? extends Parser<?>> parserClass = this.parserClasses.get(name);
        if (parserClass == null) {
            throw new RuntimeException("Unknown parser: " + name);
        }


        try {
            return parserClass.getConstructor(Map.class)
                    .newInstance(parameters);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse input into a list of Parsers
     * @param input Input
     * @return List of Parsers
     */
    protected List<Parser<?>> parseArgumentString(String input) {
        List<Parser<?>> result = new ArrayList<>();

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
                        result.add(createParser(name.toString(), new HashMap<>()));
                    }
                    break;
                }
                char c = (char) i;

                switch (state) {
                    case NAME:
                        switch (" (".indexOf(c)) {
                            case 0: // Next Argument
                                if (name.length() > 0) {
                                    result.add(createParser(name.toString(), new HashMap<>()));
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
                                result.add(createParser(name.toString(), parameters));
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
//                                value.append(c);
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
                                result.add(createParser(name.toString(), parameters));
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



        return result;
    }
}
