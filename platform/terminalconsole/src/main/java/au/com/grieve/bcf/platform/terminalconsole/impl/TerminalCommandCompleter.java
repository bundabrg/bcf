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

package au.com.grieve.bcf.platform.terminalconsole.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

public class TerminalCommandCompleter<DATA> implements Completer {
  private final TerminalCommandManager<DATA> manager;

  public TerminalCommandCompleter(TerminalCommandManager<DATA> manager) {
    this.manager = manager;
  }

  @Override
  public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {

    // Convert to jline. We provide all the completions so jline can filter them and allow
    // typo completion as well.
    candidates.addAll(
        manager.parse(line.line(), null).complete().stream()
            .flatMap(
                g -> {
                  int key = g.hashCode();
                  return g.getCompletionCandidates().stream()
                      .map(
                          c ->
                              new Candidate(
                                  c.getValue(),
                                  c.getTitle(),
                                  null,
                                  g.getDescription(),
                                  "",
                                  String.valueOf(key),
                                  true));
                })
            .collect(Collectors.toList()));
  }
}
