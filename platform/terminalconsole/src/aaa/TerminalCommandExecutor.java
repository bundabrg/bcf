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

package au.com.grieve.bcf.platform.terminalconsole;

import au.com.grieve.bcf.core.CommandContext;
import au.com.grieve.bcf.core.CommandExecute;
import au.com.grieve.bcf.platform.terminalconsole.mapper.Command;
import org.jline.reader.Candidate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TerminalCommandExecutor extends Command {

    private final TerminalCommandRoot commandRoot;

    public TerminalCommandExecutor(TerminalCommandRoot commandRoot, String name, String description) {
        super(name, description);
        this.commandRoot = commandRoot;

    }

    @Override
    public boolean execute(String cmd, String[] args) {
        CommandContext context = new CommandContext();
        CommandExecute commandExecute = commandRoot.execute(Arrays.asList(args), context);
        if (commandExecute != null) {
            commandExecute.invoke();
            return true;
        }
        return false;
    }

    @Override
    public List<Candidate> complete(String cmd, String[] args) {
        CommandContext context = new CommandContext();
        return commandRoot.complete(Arrays.asList(args), context).stream()
                .map(c -> new Candidate(c.getValue(), c.getTitle(), null, c.getDescription(), null, c.getKey(), true))
                .collect(Collectors.toList());
    }
}
