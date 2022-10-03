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

package au.com.grieve.bcf.impl.error;

import au.com.grieve.bcf.CommandError;
import au.com.grieve.bcf.CommandErrorCollection;
import au.com.grieve.bcf.ParsedLine;
import au.com.grieve.bcf.impl.line.DefaultParsedLine;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.ToString;

/** Holds ParserErrors that are equal to or heavier as those stored */
@ToString
public class DefaultErrorCollection implements CommandErrorCollection {
  private final Map<Class<? extends CommandError>, CommandError> errorMap = new HashMap<>();
  @Getter private ParsedLine line = new DefaultParsedLine("");
  @Getter private int weight = 0;

  /**
   * Add an error that is the same or heavier weight
   *
   * @param error Error to add
   */
  @Override
  public void add(CommandError error, ParsedLine line, int weight) {
    if (weight < this.weight) {
      return;
    }

    // Heavier weight clears all the lighter errors
    if (weight > this.weight) {
      errorMap.clear();
    }
    this.weight = weight;
    this.line = line.copy();

    // Merge error in
    if (!errorMap.containsKey(error.getClass())) {
      errorMap.put(error.getClass(), error);
    } else {
      errorMap.get(error.getClass()).merge(error);
    }
  }

  @Override
  public void addAll(CommandErrorCollection collection) {
    collection.get().forEach(e -> add(e, collection.getLine(), collection.getWeight()));
  }

  @Override
  public Collection<CommandError> get() {
    return errorMap.values();
  }

  @Override
  public String format() {
    return "Error: "
        + get().stream().map(CommandError::toString).collect(Collectors.joining("; or "))
        + " at: '"
        + line.getPrefix()
        + String.join(" ", line.getWords().subList(0, line.getWordIndex()))
        + "'<--[HERE]";
  }

  @Override
  public int size() {
    return errorMap.size();
  }

  @Override
  public void clear() {
    errorMap.clear();
  }

  @Override
  public Stream<CommandError> stream() {
    return errorMap.values().stream();
  }

  @Override
  public CommandErrorCollection copy() {
    DefaultErrorCollection clone = new DefaultErrorCollection();
    clone.errorMap.putAll(errorMap);
    clone.weight = weight;
    clone.line = line.copy();
    return clone;
  }
}
