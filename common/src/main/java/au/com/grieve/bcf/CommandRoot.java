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

import au.com.grieve.bcf.utils.ReflectUtils;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class CommandRoot {
    @Getter
    private final BaseCommand command;

    @Getter
    private final CommandManager manager;

    //final List<BaseCommand> subCommands = new ArrayList<>();

    @Getter
    protected final Map<Class<?>, BaseCommand> commandMap = new HashMap<>();


    public CommandRoot(CommandManager manager, Class<? extends BaseCommand> cmd) {
        this.manager = manager;
        BaseCommand command = null;
        try {
            command = cmd.getConstructor().newInstance();
            commandMap.put(cmd, command);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        this.command = command;

    }

    public void addSubCommand(Class<? extends BaseCommand> cmd) {
        // Lookup all parent classes till it reaches our command
        if (!commandMap.containsKey(cmd)) {
            try {
                commandMap.put(cmd, cmd.getConstructor().newInstance());
            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
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
                    commandMap.put(cls, (BaseCommand) cls.getConstructor().newInstance());
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


}
