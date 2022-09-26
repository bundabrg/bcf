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

package au.com.grieve.bcf.impl.parserchain;

import au.com.grieve.bcf.CompletionCandidateGroup;
import au.com.grieve.bcf.CompletionContext;
import au.com.grieve.bcf.ExecutionContext;
import au.com.grieve.bcf.ParsedLine;
import au.com.grieve.bcf.Parser;
import au.com.grieve.bcf.ParserChain;
import au.com.grieve.bcf.Result;
import au.com.grieve.bcf.exception.EndOfLineException;
import au.com.grieve.bcf.exception.ParserChainException;
import au.com.grieve.bcf.exception.ParserSyntaxException;
import au.com.grieve.bcf.impl.completion.DefaultCompletionCandidate;
import au.com.grieve.bcf.impl.completion.ParserCompletionCandidateGroup;
import au.com.grieve.bcf.impl.error.InputExpectedError;
import au.com.grieve.bcf.impl.framework.base.BaseCompletionContext;
import au.com.grieve.bcf.impl.result.ParserResult;
import au.com.grieve.bcf.impl.result.SwitchParserResult;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class StringParserChain implements ParserChain {
  private final List<ParserConfig> parserConfigs = new ArrayList<>();

  public StringParserChain(String arguments) {
    parserConfigs.addAll(parseArgumentString(arguments));
  }

  /**
   * Try to parse against a switch
   *
   * @param context Current context
   */
  protected void parseSwitches(ExecutionContext context, List<Result> output)
      throws EndOfLineException, ParserSyntaxException {
    while (context.getParsedLine().getCurrentWord().startsWith("-")) {

      String input = context.getParsedLine().getCurrentWord().substring(1);

      // Look backwards through output for a non-completed result that matches our switch
      SwitchParserResult result =
          Stream.concat(context.getResult().stream(), output.stream())
              .collect(
                  Collectors.collectingAndThen(
                      Collectors.toList(),
                      l -> {
                        Collections.reverse(l);
                        return l;
                      }))
              .stream()
              .filter(r -> r instanceof SwitchParserResult)
              .map(r -> (SwitchParserResult) r)
              .filter(r -> !r.isComplete())
              .filter(r -> r.getParser().getParameters().containsKey("switch"))
              .filter(
                  r ->
                      List.of(r.getParser().getParameters().get("switch").split("\\|"))
                          .contains(input))
              .findFirst()
              .orElse(null);

      if (result == null) {
        break;
      }
      context.setWeight(context.getWeight()+1);

      context.getParsedLine().next();

      result.setValue(result.getParser().parse(context, context.getParsedLine()));
      context.setWeight(context.getWeight()+1);
    }
  }

  @Override
  public void parse(ExecutionContext context, List<Result> results) throws ParserChainException {
    List<Parser<?>> parsers =
        parserConfigs.stream()
            .map(c -> createParser(c, context.getParserClasses()))
            .collect(Collectors.toList());

    try {

      for (Parser<?> p : parsers) {
        // If parser is a switch we add it for later evaluation
        if (p.getParameters().containsKey("switch")) {
          Result result = new SwitchParserResult(p);
          results.add(result);
          continue;
        }

        parseSwitches(context, results);

        Object result = p.parse(context, context.getParsedLine());
        context.setWeight(context.getWeight() + 1);

        results.add(new ParserResult(p, result));
      }

      parseSwitches(context, results);
    } catch (EndOfLineException e) {
      throw new ParserChainException(
          context.getParsedLine(), new InputExpectedError(), context.getWeight()+1);
    } catch (ParserSyntaxException e) {
      throw new ParserChainException(e.getLine(), e.getError(), context.getWeight()+1);
    }
  }

  protected void completeSwitches(
      CompletionContext context, List<CompletionCandidateGroup> candidateGroups)
      throws EndOfLineException, ParserSyntaxException {
    while (context.getParsedLine().getCurrentWord().startsWith("-")) {
      String input = context.getParsedLine().getCurrentWord().substring(1);

      // Look backwards through candidates for a non-completed result that matches our switch
      Parser<?> parser =
          ((BaseCompletionContext) context)
              .getSwitches().stream()
                  .collect(
                      Collectors.collectingAndThen(
                          Collectors.toList(),
                          l -> {
                            Collections.reverse(l);
                            return l;
                          }))
                  .stream()
                  .filter(
                      r -> List.of(r.getParameters().get("switch").split("\\|")).contains(input))
                  .findFirst()
                  .orElse(null);

      if (parser == null) {
        break;
      }

      context.getParsedLine().next();

      ParsedLine lineCopy = context.getParsedLine().copy();

      try {
        context.setWeight(context.getWeight() + 1);
        parser.parse(context, context.getParsedLine());
        ((BaseCompletionContext) context).getSwitches().remove(parser);
      } catch (ParserSyntaxException e) {
        // Try to complete that last bit
        List<CompletionCandidateGroup> groups = new ArrayList<>();
        parser.complete(context, lineCopy, groups);

        // If we are now at EOL we can use those candidates
        if (lineCopy.isEol()) {
          candidateGroups.addAll(groups);
        }
        throw e;
      } catch (EndOfLineException e) {
        // Try to complete that last bit
        parser.complete(context, lineCopy, candidateGroups);
        throw e;
      }

      // If we are at end of line try complete it
      if (context.getParsedLine().isEol()) {
        parser.complete(context, lineCopy, candidateGroups);
        return;
      }
    }

    // If we are at the end of the line, add any switches
    if (context.getParsedLine().size() <= 1) {
      for (Parser<?> p : ((BaseCompletionContext) context).getSwitches()) {
        CompletionCandidateGroup group =
            new ParserCompletionCandidateGroup(p, context.getParsedLine().getCurrentWord());
        group
            .getCompletionCandidates()
            .addAll(
                Stream.of(p.getParameters().get("switch").split("\\|"))
                    .map(s -> new DefaultCompletionCandidate("-" + s))
                    .collect(Collectors.toList()));
        candidateGroups.add(group);
      }
    }
  }

  @Override
  public void complete(CompletionContext context, List<CompletionCandidateGroup> candidateGroups)
      throws ParserChainException {

    try {

      List<Parser<?>> parsers =
          parserConfigs.stream()
              .map(c -> createParser(c, context.getParserClasses()))
              .collect(Collectors.toList());
      for (Parser<?> p : parsers) {
        // If parser is a switch we add it for later evaluation
        if (p.getParameters().containsKey("switch")) {
          ((BaseCompletionContext) context).getSwitches().add(p);
          continue;
        }

        completeSwitches(context, candidateGroups);

        ParsedLine lineCopy = context.getParsedLine().copy();

        try {
          context.setWeight(context.getWeight() + 1);
          p.parse(context, context.getParsedLine());
        } catch (ParserSyntaxException e) {
          // Try to complete that last bit
          List<CompletionCandidateGroup> groups = new ArrayList<>();
          p.complete(context, lineCopy, groups);

          // If we are now at EOL we can use those candidates
          if (lineCopy.isEol()) {
            candidateGroups.addAll(groups);
          }
          throw e;
        } catch (EndOfLineException e) {
          // Try to complete that last bit
          p.complete(context, lineCopy, candidateGroups);
          throw e;
        }

        // If we are at end of line try complete it
        if (context.getParsedLine().isEol()) {
          p.complete(context, lineCopy, candidateGroups);
          return;
        }
      }

      completeSwitches(context, candidateGroups);
    } catch (EndOfLineException e) {
      throw new ParserChainException(
          context.getParsedLine(), new InputExpectedError(), context.getWeight() - 1);
    } catch (ParserSyntaxException e) {
      throw new ParserChainException(e.getLine(), e.getError(), context.getWeight());
    }
  }

  /** Create a new parser based upon the name. */
  protected Parser<?> createParser(
      ParserConfig parserConfig, Map<String, Class<? extends Parser<?>>> parserClasses) {
    Class<? extends Parser<?>> parserClass = parserClasses.get(parserConfig.getName());
    if (parserClass == null) {
      throw new RuntimeException("Unknown parser: " + parserConfig.getName());
    }

    try {
      return parserClass.getConstructor(Map.class).newInstance(parserConfig.getParameters());
    } catch (InstantiationException
        | NoSuchMethodException
        | InvocationTargetException
        | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Parse input into a list of Parsers
   *
   * @param input Input
   * @return List of Parsers
   */
  protected List<ParserConfig> parseArgumentString(String input) {
    List<ParserConfig> result = new ArrayList<>();

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
            result.add(new ParserConfig(name.toString(), new HashMap<>()));
          }
          break;
        }
        char c = (char) i;

        switch (state) {
          case NAME:
            switch (" (".indexOf(c)) {
              case 0: // Next Argument
                if (name.length() > 0) {
                  result.add(new ParserConfig(name.toString(), new HashMap<>()));
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
                result.add(new ParserConfig(name.toString(), parameters));
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
                result.add(new ParserConfig(name.toString(), parameters));
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

  private enum State {
    NAME,
    PARAM_KEY,
    PARAM_VALUE,
    PARAM_VALUE_QUOTE,
    PARAM_VALUE_QUOTE_END,
    PARAM_END
  }

  @Getter
  @ToString
  protected static class ParserConfig {
    private final String name;
    private final Map<String, String> parameters = new HashMap<>();

    public ParserConfig(String name, Map<String, String> parameters) {
      this.parameters.putAll(parameters);

      // If it does not start with @, then a parser called 'literal' parser will be used with
      // options being the name
      if (!name.startsWith("@")) {
        this.parameters.put("options", name);
        this.parameters.put("suppress", this.parameters.getOrDefault("suppress", "true"));
        this.name = "literal";
      } else {
        this.name = name.substring(1);
      }
    }
  }
}