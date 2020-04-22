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

import au.com.grieve.bcf.exceptions.ParserInvalidResultException;
import au.com.grieve.bcf.exceptions.ParserNoResultException;
import au.com.grieve.bcf.exceptions.ParserRequiredArgumentException;
import au.com.grieve.bcf.parsers.*;
import com.google.common.collect.Lists;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public abstract class CommandManager {

    protected final Map<String, RootCommand> commands = new HashMap<>();
    protected final Map<String, Class<? extends Parser>> parsers = new HashMap<>();

    public CommandManager() {
        // Register Default Parsers
        registerParser("string", StringParser.class);
        registerParser("int", IntegerParser.class);
        registerParser("double", DoubleParser.class);
    }

    @SuppressWarnings("unused")
    public abstract void registerCommand(BaseCommand cmd);

    public void registerParser(String name, Class<? extends Parser> parser) {
        this.parsers.put("@" + name, parser);
    }

    @SuppressWarnings("unused")
    public void unregisterParser(String name) {
        this.parsers.remove(name);
    }

    /**
     * Execute Node using arguments and optionally adding parameters to method
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean execute(ParserNode root, String args, ParserContext context, List<Object> param) {
        List<Parser> result = new ArrayList<>();
        result.add(new NullParser(this, root, context));
        for (Parser p : resolve(root, args, context)) {
            if (!p.isParsed()) {
                try {
                    p.parse(null);
                } catch (ParserRequiredArgumentException e) {
                    break;
                }
            }

            result.add(p);

            try {
                p.getResult();
            } catch (ParserInvalidResultException e) {
                // An error has occurred. Look backwards for a node that provides an error handler
                for (Parser pb : Lists.reverse(result)) {
                    ParserMethod m = pb.getNode().getError();
                    if (m != null) {
                        List<Object> objs = new ArrayList<>(param);
                        objs.add(e.getMessage());
                        try {
                            m.invoke(objs);
                        } catch (IllegalAccessException | InvocationTargetException e2) {
                            e2.printStackTrace();
                        }
                        return true;
                    }
                }
                break;
            } catch (ParserNoResultException e) {
                e.printStackTrace();
                break;
            }


        }

        if (result.size() > 0) {
            ParserMethod method = result.get(result.size() - 1).getNode().getExecute();

            if (method != null) {
                List<Object> objs = new ArrayList<>(param);
                for (Parser p : result) {
                    try {
                        Object o = p.getResult();
                        if (!p.getParameter("suppress", "false").equals("true")) {
                            objs.add(o);
                        }
                    } catch (ParserInvalidResultException | ParserNoResultException e) {
                        e.printStackTrace();
                        return false;
                    }
                }

                // Fill out extra parameters with null
                while (objs.size() < method.getMethod().getParameterCount()) {
                    objs.add(null);
                }

                try {
                    method.invoke(objs.toArray());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else {
                // A deadend occurred. Look backwards for a node that provides a default handler
                for (Parser pb : Lists.reverse(result)) {
                    ParserMethod m = pb.getNode().getDefault();
                    if (m != null) {
                        try {
                            m.invoke(param);
                        } catch (IllegalAccessException | InvocationTargetException e2) {
                            e2.printStackTrace();
                        }
                        return true;
                    }
                }
            }
        }

        return true;
    }


    /**
     * Return parsers for a switch
     */
    public List<Parser> switchResolve(ParserNode node, String args, ParserContext context) {

        while (args != null && args.startsWith("-")) {
            String[] argSplit = args.split(" ", 2);

            // Complete it if last argument
            if (argSplit.length < 2) {
                return new ArrayList<>();
            }

            args = argSplit[1];

            // Look for parser
            Parser switchParser = context.getSwitches().stream()
                    .flatMap(s -> Arrays.stream(s.getNode().getData().getParameters().get("switch").split("\\|"))
                            .filter(sw -> sw.equals(argSplit[0].substring(1)))
                            .limit(1)
                            .map(sw -> s)
                    )
                    .findFirst()
                    .orElse(null);

            if (switchParser == null) {
                return new ArrayList<>();
            }

            try {
                args = switchParser.parse(args);
            } catch (ParserRequiredArgumentException e) {
                return new ArrayList<>();
            }

//            // Make sure its a valid result
//            try {
//                switchParser.getResult();
//            } catch (ParserInvalidResultException e) {
//                return new ArrayList<>();
//            }

            context.getSwitches().remove(switchParser);
        }

        return new ArrayList<>(resolve(node, args, context));
    }

    /**
     * Return a list of Parsers
     */
    public List<Parser> resolve(ParserNode node, String args, ParserContext context) {
        List<Parser> result = new ArrayList<>();

        if (node == null) {
            return result;
        }

        if (node.getData() != null) {
            ParserNodeData data = node.getData();

            Class<? extends Parser> parserClass = parsers.getOrDefault(data.getName(), LiteralParser.class);

            Parser parser;
            try {
                parser = parserClass.getConstructor(CommandManager.class, ParserNode.class, ParserContext.class)
                        .newInstance(this, node, context);
            } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }

            // Take care of switches first in case this argument is also a switch
            if (data.getParameters().containsKey("switch")) {
                result.add(parser);
                context.getSwitches().add(parser);

            } else {
                if (args != null && args.startsWith("-")) {
                    result.addAll(switchResolve(node, args, context));
                    return result;
                }

                try {
                    args = parser.parse(args);
                } catch (ParserRequiredArgumentException e) {
                    return new ArrayList<>();
                }

                result.add(parser);
                context.getParsers().add(parser);

                // Make sure its a valid result else return early
                try {
                    parser.getResult();
                } catch (ParserInvalidResultException e) {
                    return result;
                } catch (ParserNoResultException e) {
                    return new ArrayList<>();
                }
            }
        }

        if (node.getChildren().size() == 0) {
            if (args != null && args.startsWith("-")) {
                result.addAll(switchResolve(null, args, context));
                return result;
            }
        }

        // Recurse into children
        List<Parser> best = new ArrayList<>();
        for (ParserNode child : node.getChildren()) {
            // Make a copy of the context
            ParserContext childContext;
            try {
                childContext = (ParserContext) context.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                break;
            }

            List<Parser> check = resolve(child, args, childContext);
            if (check.size() > best.size()) {
                best = check;
            }
        }

        result.addAll(best);

        return result;
    }

    /**
     * Return completions for a switch
     */
    public List<String> switchComplete(ParserNode node, String args, ParserContext context) {
        while (args != null && args.startsWith("-")) {
            String[] argSplit = args.split(" ", 2);

            // Complete it if last argument
            if (argSplit.length == 1) {
                return context.getSwitches().stream()
                        .flatMap(s -> Arrays.stream(s.getNode().getData().getParameters().get("switch").split("\\|"))
                                .filter(sw -> sw.startsWith(argSplit[0].substring(1)))
                                .limit(1)
                        )
                        .map(s -> "-" + s)
                        .limit(20)
                        .collect(Collectors.toList());
            }

            args = argSplit[1];

            // Look for parser
            Parser switchParser = context.getSwitches().stream()
                    .flatMap(s -> Arrays.stream(s.getNode().getData().getParameters().get("switch").split("\\|"))
                            .filter(sw -> sw.equals(argSplit[0].substring(1)))
                            .limit(1)
                            .map(sw -> s)
                    )
                    .findFirst()
                    .orElse(null);

            if (switchParser == null) {
                return new ArrayList<>();
            }

            try {
                args = switchParser.parse(args);
            } catch (ParserRequiredArgumentException e) {
                return new ArrayList<>();
            }

            if (args == null) {
                return switchParser.getCompletions();
            }

            context.getSwitches().remove(switchParser);
        }

        return complete(node, args, context);
    }

    public List<String> getComplete(ParserNode node, String args, ParserContext context) {
        return complete(node, args, context);
    }

    /**
     * Return a list completions
     */
    public List<String> complete(ParserNode node, String args, ParserContext context) {
        List<String> result = new ArrayList<>();

        if (node == null) {
            return result;
        }

        if (!node.isRoot() && node.getData() != null) {
            ParserNodeData data = node.getData();

            Class<? extends Parser> parserClass = parsers.getOrDefault(data.getName(), LiteralParser.class);

            Parser parser;
            try {
                parser = parserClass.getConstructor(CommandManager.class, ParserNode.class, ParserContext.class)
                        .newInstance(this, node, context);
            } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }

            // Take care of switches first in case this argument is also a switch
            if (data.getParameters().containsKey("switch")) {
                context.getSwitches().add(parser);

            } else {
                if (args != null && args.startsWith("-")) {
                    return switchComplete(node, args, context);
                }

                try {
                    args = parser.parse(args);
                } catch (ParserRequiredArgumentException e) {
                    return new ArrayList<>();
                }

                if (args != null) {
                    // Make sure its a valid result for non leaf nodes
                    try {
                        parser.getResult();
                    } catch (ParserInvalidResultException | ParserNoResultException e) {
                        return new ArrayList<>();
                    }
                } else {
                    result.addAll(parser.getCompletions());
                }
            }
        }

        // Recurse into children
        if (node.getChildren().size() == 0) {
            if (args != null && args.startsWith("-")) {
                return switchComplete(null, args, context);
            }
        }

        for (ParserNode child : node.getChildren()) {
            // Make a copy of the context
            ParserContext childContext;
            try {
                childContext = (ParserContext) context.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                break;
            }

            result.addAll(complete(child, args, childContext));
        }

        return result;
    }
}
