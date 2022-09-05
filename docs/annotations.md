In Java an annotation is something that can be added onto a Class, Method, Variable or even a method parameter.

Most annotations we use can be added to the Class itself, the Method, or both and they are called its `Target`.  Generally an annotation on a class
will affect all the methods inside the class as well as any in any derived classes. 

Most annotation can also be used multiple times. For example you can add `@Arg` more than one to the same method
and when completing/executing a command each `@Arg` will be checked in turn.  Each annotation has its own rules
to define what multiples of itself does.


## Common

These annotations are available for all execution environments.

### @Arg

**Target**: `Class`, `Method`

**Value**: `String`

**Multiple**: allowed


Provides a list of `Parsers` that will consume input and can provide command completions for partial input. Each parser 
starts with an `@` prefixed to its name along with optional parameters defined inside braces after the parser definition.
Any bare strings are treated with as `LiteralParser` and will accept its own name(s) without providing any
output to the invoked method.

If a command is executed then the winning `@Arg` will then invoke the method with fully resolved objects, each
parser consuming the input and optionally returning an object to be provided as a parameter to the method.

When consuming input, the `@Arg` on a class (if any) will first process the input, with the remaining input then going to
any defined on its `Methods`. Any Classes that extend the class will also receive the remaining input in which case they
go through the same steps.

Multiple `@Arg` will be checked in turn and can be thought of as multiple choices.

More details about Parsers will be provided in [Parsers](parsers.md)

!!! example
    ```java
    @Command("mycmd")
    class MainCommand extends BukkitCommand {
        @Arg("cmd1")
        public void cmd1(CommandSender sender) {
            ...
        }
    }
    
    @Arg("sub1")
    @Arg("sub2")
    class SubCommand extends MainCommand {
        @Arg("cmd2")
        @Arg("cmd3")
        public void cmd2(CommandSender sender) {
            ...
        }
    }
    ```
    This shows two classes, one extending the other, and multiple @Arg at several points.
    
    The following are all valid commands:
    
    * `/mycmd cmd1`
    * `/mycmd sub1 cmd2`
    * `/mycmd sub1 cmd3`
    * `/mycmd sub2 cmd2`
    * `/mycmd sub2 cmd3`


### @Command

**Target**: `Class`

**Value**: `String`

**Multiple**: allowed

Defines a top level command and if possible will be registered with the execution environment. Aliases to the same
command can be added by separating the value with `|`. This can be used to create top-level commands that jump straight to a deeply nested
sub command.

Multiple `@Commands` will register multiple top level commands.

!!! example
    ```java
    @Command("mycmd|mc")
    class MainCommand extends BukkitCommand {
        @Arg("cmd1")
        public void cmd1(CommandSender sender) {
            ...
        }
    }
    
    @Command("qc")
    class SubCommand extends MainCommand {
        @Arg("cmd3")
        public void cmd2(CommandSender sender) {
            ...
        }
    }
    ```
    Here 2 top level commands are registered. `/mycmd` and `/qc`. An alias `/mc` is also available for `/mycmd`.
    
    Valid commands are:
    
    * `/mycmd cmd1`
    * `/mc cmd1`
    * `/mycmd cmd3`
    * `/qc cmd3`

### @Default

**Target**: `Method`

**Value**: `None`

**Multiple**: not allowed

Signifies what method to call when needing to invoke a `Default` method for a Class and any of its child classes (unless
overridden in that class with its own `@Default`)

When processing input if we do not find any winning method for a class then a `Default` method will be executed. If
none are found in a class then its parent class will be searched until it ends up with the built in `Default` that shows
a "Invalid Command" message.

This can be used to provide help for unknown commands or can be used to redefine what is shown on no match.

The method must not take any additional parameters apart from those provided by default.

!!! example
    ```java
    @Command("mycmd")
    class MainCommand extends BukkitCommand {
        @Arg("cmd1")
        public void cmd1(CommandSender sender) {
            ...
        }
        
        @Default
        public void onDefault(CommandSender sender) {
            sender.spigot().sendMessage(
                    new ComponentBuilder("Some help").color(ChatColor.YELLOW).create()
            );
        }
    
    }
    
    @Arg("sub")
    class SubCommand extends MainCommand {
        @Arg("cmd2")
        public void cmd2(CommandSender sender) {
            ...
        }
    
        @Default
        public void onDefault(CommandSender sender) {
            sender.spigot().sendMessage(
                    new ComponentBuilder("Sub Help").color(ChatColor.RED).create()
            );
        }
    }
    ```
    When attempting to execute the invalid command: `/mycmd sub unknown` a message "Sub Help" in red will
    be shown.
    
    When attempting to execute the invalid command: `/mycmd unknown` a message "Some help" in yellow will
    be shown.

### @Description

**Target**: `Class`

**Value**: `None`

**Multiple**: not allowed

When used on a class decorated by `@Command` it will add a description that will become visible in platforms that
support it.

!!! example
    ```java
    @Command("mycmd")
    @Description("This performs something")
    class MainCommand extends BukkitCommand {
        @Arg("cmd1")
        public void cmd1(CommandSender sender) {
            ...
        }
    }
    ```



### @Error

**Target**: `Method`

**Value**: `None`

**Multiple**: not allowed

Signifies what method to call when needing to invoke an `Error` method for any errors in a Class and any of its child classes (unless
overridden in that class with its own `@Error`)

When processing input if an error is encountered it will be passed to a `Error` method. If none are found in a class then its
parent will be searched until it ends with the built in `Error` that will show the error in red.

This can be used to customize how errors are handled.

The method must take a string argument containing the error message.

!!! example
    ```java
    @Command("mycmd")
    class MainCommand extends BukkitCommand {
        @Arg("cmd1 @int(max=3)")
        public void cmd1(CommandSender sender) {
            ...
        }
        
        @Error
        public void onError(CommandSender sender, String message) {
            sender.spigot().sendMessage(
                    new ComponentBuilder(message).color(ChatColor.YELLOW).create()
            );
        }
    
    }
    
    @Arg("sub")
    class SubCommand extends MainCommand {
        @Arg("cmd2 @int(max=3)")
        public void cmd2(CommandSender sender) {
            ...
        }
    
        @Error
        public void onError(CommandSender sender, String message) {
            sender.spigot().sendMessage(
                    new ComponentBuilder(message).color(ChatColor.GREEN).create()
            );
        }
    }
    ```
    When attempting to execute the command: `/mycmd sub cmd2 10` an error message "Number must be smaller or equal to 3" 
    will be shown in green.
    
    When attempting to execute the invalid command: `/mycmd cmd1 10` a message "Number must be smaller or equal to 3" in yellow will
    be shown.
    
## Bukkit/Bungeecord

These annotations are available for Bukkit and Bungeecord environments.

### @Permission

**Target**: `Class`, `Method`

**Value**: `String`

**Multiple**: allowed

Provides the permissions the command sender must have for a class or method to be considered.

When multiple `@Permission` is found on the same Target then it will accept any of those permissions. This can be thought of as
an `OR` condition.

!!! example
    ```java
    @Command("mycmd")
    class MainCommand extends BukkitCommand {
        @Arg("cmd1")
        public void cmd1(CommandSender sender) {
            ...
        }
        
        @Permission("mycmd.perm1")
        @Arg("cmd2")
            public void cmd1(CommandSender sender) {
                ...
            } 
    }
    
    @Permission("mycmd.perm1")
    @Permission("mycmd.perm2")
    class SubCommand extends MainCommand {
        @Arg("cmd3")
        public void cmd2(CommandSender sender) {
            ...
        }
    
        @Arg("cmd4")
        @Permission("mycmd.perm2")
        public void cmd2(CommandSender sender) {
            ...
        }
    }
    ```
    A user with `mycmd.perm1` will be able to access the following commands:
    
    * `/mycmd cmd1`
    * `/mycmd cmd2`
    * `/mycmd cmd3`
    
    A user with `mycmd.perm2` will be able to access the followinig commands:
    
    * `/mycmd cmd1`
    * `/mycmd cmd3`
    * `/mycmd cmd4` 