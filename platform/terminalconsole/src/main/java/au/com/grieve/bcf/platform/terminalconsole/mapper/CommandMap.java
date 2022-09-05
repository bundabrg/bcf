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

package au.com.grieve.bcf.platform.terminalconsole.mapper;

import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.Candidate;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@ToString
public class CommandMap {
    private final Map<String, Command> commands = new HashMap<>();
    private final Map<String, String> commandAlias = new HashMap<>();


    @SuppressWarnings("UnusedReturnValue")
    public boolean register(String label, Command command, String[] aliases) {
        if (commands.containsKey(label)) {
            return false;
        }
        commands.put(label, command);

        // Add aliases that don't exist yet
        commandAlias.putAll(Arrays.stream(aliases)
                .filter(s -> !commandAlias.containsKey(s))
                .collect(Collectors.toMap(s -> s, s -> label)));

        return true;
    }

    private Command findCommand(String name) {
        if (commands.containsKey(name)) {
            return commands.get(name);
        }

        if (commandAlias.containsKey(name) && commands.containsKey(commandAlias.get(name))) {
            return commands.get(commandAlias.get(name));
        }

        return null;
    }

    public boolean execute(String input) {
        String[] args = input.split(" ", 2);
        if (args[0].startsWith("/")) {
            args[0] = args[0].substring(1);
        }

        Command command = findCommand(args[0]);
        if (command == null) {
            return false;
        }

        return command.execute(args[0], args.length > 1 ? args[1].split(" ") : new String[0]);
    }

    public @NotNull List<Candidate> complete(String input) {
        String[] args = input.split(" ", 2);
        String prefix = args[0].startsWith("/") ? "/" : "";

        if (args[0].startsWith("/")) {
            args[0] = args[0].substring(1);
        }

        // If still in first argument we return completion ourselves
        if (args.length == 1) {
            return commands.keySet().stream()
                    .filter(v -> v.startsWith(args[0]))
                    .map(v -> prefix + v)
                    .map(v -> new Candidate(v, v, null, commands.get(v).getDescription(), null, null, true))
                    .collect(Collectors.toList());
        }

        Command command = findCommand(args[0]);

        if (command == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(command.complete(args[0], args[1].split(" ", -1)));
    }
}
