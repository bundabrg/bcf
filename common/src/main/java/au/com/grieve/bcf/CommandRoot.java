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

import au.com.grieve.bcf.utils.ReflectUtils;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandRoot<T extends BaseCommand> {
    @Getter
    protected final Map<Class<? extends T>, T> commandMap = new HashMap<>();
    @Getter
    private final T command;

    //final List<BaseCommand> subCommands = new ArrayList<>();
    @Getter
    private final CommandManager<T> manager;


    public CommandRoot(CommandManager<T> manager, Class<? extends T> cmd) {
        this.manager = manager;
        T command = null;
        try {
            command = cmd.getConstructor().newInstance();
            commandMap.put(cmd, command);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            e.printStackTrace();
        }
        this.command = command;

    }

    public void addSubCommand(Class<? extends T> cmd) {
        // Lookup all parent classes till it reaches our command
        if (!commandMap.containsKey(cmd)) {
            try {
                commandMap.put(cmd, cmd.getConstructor().newInstance());
            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException |
                     IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        BaseCommand current = commandMap.get(cmd);

        for (Class<?> cls : ReflectUtils.getAllSuperClasses(cmd)) {
            if (!BaseCommand.class.isAssignableFrom(cls)) {
                break;
            }

            if (!commandMap.containsKey(cls)) {
                try {
                    //noinspection unchecked
                    commandMap.put((Class<? extends T>) cls, (T) cls.getConstructor().newInstance());
                } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                    break;
                }
            }
            BaseCommand c = commandMap.get(cls);
            if (!c.getChildren().contains(current)) {
                c.getChildren().add(current);
            }
            current = c;

            if (cls == command.getClass()) {
                break;
            }
        }
    }

    protected Parser getParser(ArgNode argNode, CommandContext context) {
        return manager.getParser(argNode, context);

    }

    public CommandExecute execute(List<String> input, CommandContext context) {
        return command.execute(this, input, context);
    }

    public List<String> complete(List<String> input, CommandContext context) {
        return command.complete(this, input, context);
    }


}
