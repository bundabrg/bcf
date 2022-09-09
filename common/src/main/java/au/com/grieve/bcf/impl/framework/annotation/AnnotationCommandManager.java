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

import au.com.grieve.bcf.CommandManager;
import au.com.grieve.bcf.Parser;
import au.com.grieve.bcf.framework.annotation.annotations.Command;
import lombok.Getter;

import java.util.*;

@Getter
public class AnnotationCommandManager implements CommandManager<AnnotationCommand> {
    private final List<AnnotationCommand> commands = new ArrayList<>();
    private final Map<Class<? extends AnnotationCommand>, Set<AnnotationCommand>> commmandInstances = new HashMap<>();
    private final Map<String, Class<? extends Parser<?>>> parsers = new HashMap<>();

    private boolean hasCommand(AnnotationCommand command) {
        return command.getClass().getAnnotation(Command.class) != null;
    }

    /**
     * Register a parser with a name
     * @param name name of parser
     * @param parserClass Parser Class
     */
    public void registerParser(String name, Class<? extends Parser<?>> parserClass) {
        this.parsers.put(name, parserClass);
    }

    /**
     * Add command instance
     *
     * When child commands are added later we use this to know where to add the children to
     * @param command Command to add
     */
    private void addInstance(AnnotationCommand command) {
        Set<AnnotationCommand> instances = this.commmandInstances.computeIfAbsent(command.getClass(), k -> new HashSet<>());
        instances.add(command);
    }

    /**
     * Register a root command
     * @param command Command to register
     */
    @Override
    public void registerCommand(AnnotationCommand command) {
        if (!hasCommand(command)) {
            throw new RuntimeException("Missing required @Command");
        }

        this.commands.add(command);
        addInstance(command);
    }

    /**
     * Register subcommand
     * @param parent Parent command class
     * @param command Command to register
     */
    @Override
    public void registerCommand(Class<? extends AnnotationCommand> parent, AnnotationCommand command) {
        Set<AnnotationCommand> instances = this.commmandInstances.get(parent);
        if (instances != null) {
            addInstance(command);
            instances.forEach(c -> c.addChild(command));
        }
    }

}
