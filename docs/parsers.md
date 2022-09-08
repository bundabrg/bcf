A `Parser` is something with a name that can consume 0 or more words from the input and can produce 0 or 1 objects
to be passed as a parameter to a method.  It can also use partial input to provide command completion.

Parsers are provided in a space separated `@Arg` annotation string to the Class and/or Method.

The format of the parser definition in the `@Arg` string is:

```
@parsername(key=value, ...) @nextparser...
```

Where:

* `@parsername` - the name of the parser to user. If it does not start with `@` then it will be treated as a `Literal` Parser.
* `(key=value, ...)` - optional parameters can be passed to the parser to define its behaviour. If no parameters are required
then the braces can be left off as well.

## Common

These parsers are available for all execution environments

### Common Parameters

There are some parameters that are common to most Parsers and will be listed here.

#### default

Provide a default value if none is provided through input. Note that any input at all will stop the
default being provided and invalid input will correctly show an error.

!!! example
    ```java
    @Command("mycmd")
    public class MainCommand extends BukkitCommand {
    
        @Arg("cmd1 @int(default=3)")
        public void myCmd1(CommandSender sender, Integer myNum) {
            ...
        }
    }
    ```
    The command `/mycmd cmd1` will provide 3 to `myNum` variable.
    
    The command `/mycmd cmd1 10` will provide 10 to `myNum` variable.
    
    The command `/mycmd cmd1 aaa` will display the error "Invalid Number: a"
    

#### description

Provide a description when auto completing for platforms that support it.

!!! example
    ```java
    @Command("mycmd")
    public class MainCommand extends BukkitCommand {
    
        @Arg("cmd1 @int(description=A silly number)")
        public void myCmd1(CommandSender sender, Integer myNum) {
            ...
        }
    }
    ```
    

#### required

If set to true will require a value to be provided either through input or through a `default` parameter.

If set to false (default) then missing input without a `default` will return a null object to the method.

!!! example
    ```java
    @Command("mycmd")
    public class MainCommand extends BukkitCommand {
    
        @Arg("cmd1 @int(required=true, default=3)")
        public void myCmd1(CommandSender sender, Integer myNum) {
            ...
        }
        
        @Arg("cmd2 @int(required=true)")
        public void myCmd2(CommandSender sender, Integer myNum) {
            ...
        }
    
        @Arg("cmd3 @int(required=false)")
        public void myCmd3(CommandSender sender, Integer myNum) {
            ...
        }
    
    }
    ```
    The command `/mycmd cmd1` will succeed and provide `3` for variable `myNum`
    
    The command `/mycmd cmd2` will show an error "A number is required"
    
    The command `/mycmd cmd3` will succeed and provide null for variable `myNum`

#### suppress

If set to true then this parser will not return any object to a method but otherwise will behave the same

!!! example
    ```java
    @Command("mycmd")
    public class MainCommand extends BukkitCommand {
    
        @Arg("cmd1 @int(suppress=true, required=true)")
        public void myCmd1(CommandSender sender) {
            ...
        }
    }
    ```
    The command `/mycmd cmd1 5` will execute the method with no additional parameters.
    
    The command `/mycmd cmd1` will return a required parameter error due to the `required=true` parameter.

#### switch

The presence of this parameter will make a Parser into a named parameter instead of a positional one. It
lists the name(s) of the switch separated by a `|`.

From the point it is defined it will consume input only if the input has a `-` with one of the names
provided in which case the next word(s) of the input will go towards this parser. The returned object
will be provided to the method in the order it is defined in the `@Arg` string.

Full command completion is provided for both the name(s) of the switch as well as its values.

!!! example
    ```java
    @Command("mycmd")
    public class MainCommand extends BukkitCommand {
    
        @Arg("cmd1 @int(switch=test|t, required=false) param1 param2 param3")
        public void myCmd1(CommandSender sender, Integer myNum) {
            ...
        }
    }
    ```
    The following are all valid:
    
    * `/mycmd cmd1 -test 3 param1 param2 param3`
    * `/mycmd cmd1 param1 -test 3 param2 param3`
    * `/mycmd cmd1 param1 param2 -test 3 param 3`
    * `/mycmd cmd1 param1 param2 param3 -test 3`
    * `/mycmd cmd1 param1 param2 param3`
    
    All but the last will return `3` to the variable `myNum`.
    
    The last command will return `null` to the variable `myNum`.

### Literal

**Consumes**: 1

**Returns**: `String` (if `suppress` is `false`)

**Completions**: `yes`

The simplest Parser is the Literal Parser. This one does not have a special name but rather is used whenever
a string is detected instead of the name of a parser prefixed with `@`.

The Literal parser will use its name as input and has `suppress` set to true by default so will not normally
provide any parameter to the method. Multiple options can be provided by separating the names with `|`. 

Command completion will show all the options provided.

!!! example
    ```java
    @Command("mycmd")
    public class MainCommand extends BukkitCommand {
    
        @Arg("cmd1|c1 param1 param2|p2(suppress=false) param3")
        public void myCmd1(CommandSender sender, String p2) {
            ...
        }
    }
    ```
    The following are all valid commands:
    
    * `/mycmd cmd1 param1 param2 param3`
    * `/mycmd c1 param1 param2 param3`
    * `/mycmd cmd1 param1 p2 param3`
    * `/mycmd c1 param1 p2 param3`
    
    The method parameter `p2` will be filled in with either `param2` or `p2`.
    
### @Double

**Consumes**: 1

**Returns**: `Double`

**Completions**: `no`

This Parser will try to read a floating point number from input and will return it as a `Double`

!!! example
    ```java
    @Command("mycmd")
    public class MainCommand extends BukkitCommand {
    
        @Arg("cmd1 @double")
        public void myCmd1(CommandSender sender, Double p1) {
            ...
        }
    }
    ```
    The command: `/mycmd cmd1 1.5` will provided the method parmeter `p1` with a Double with value `1.5`
    
    The command `/mycmd cmd1 aaa` will return an error.

### @Float

**Consumes**: 1

**Returns**: `Float`

**Completions**: `no`

This Parser will try to read a floating point number from input and will return it as a `Float`

!!! example
    ```java
    @Command("mycmd")
    public class MainCommand extends BukkitCommand {
    
        @Arg("cmd1 @float")
        public void myCmd1(CommandSender sender, Float p1) {
            ...
        }
    }
    ```
    The command: `/mycmd cmd1 1.5` will provided the method parmeter `p1` with a Float with value `1.5`
    
    The command `/mycmd cmd1 aaa` will return an error.

### @Int

**Consumes**: 1

**Returns**: `Integer`

**Completions**: `sometimes`

This Parser will try to read an integer from input and will return it as an `Integer`.

If a `max` parameter is provided then completion will show up to 20 numbers between `min` and `max`. If
`min` is not defined but `max` is then `min` will be considered to be 0 for completion only but will not
affect execution.

#### min

Set the minimum value accepted. Defaults to no minimum.

#### max

Set the maximum value accepted. Defaults to no maximum.


!!! example
    ```java
    @Command("mycmd")
    public class MainCommand extends BukkitCommand {
    
        @Arg("cmd1 @int(min=4,max=10)")
        public void myCmd1(CommandSender sender, Integer p1) {
            ...
        }
    }
    ```
    The command: `/mycmd cmd1 6` will provided the method parmeter `p1` with a Integer with value `6`
    
    The command `/mycmd cmd1 100` will return an error.
    
    The command `/mycmd cmd1 aaa` will return an error.
    
    
    
### @String

**Consumes**: 1

**Returns**: `String`

**Completions**: `no`

This Parser will consume a single word from input and return it as a `String` object.

No completions will be provided.

!!! example
    ```java
    @Command("mycmd")
    public class MainCommand extends BukkitCommand {
    
        @Arg("cmd1 @string @string")
        public void myCmd1(CommandSender sender, String p1, String p2) {
            ...
        }
    }
    ```
    The command: `/mycmd cmd1 foo bar` will provided the String `foo` for method parmaeter `p1` and `bar` for method 
    parameter `p2`
    
## Bukkit

These parsers are available for the Bukkit execution environment

### @Player

**Consumes**: 1

**Returns**: `Player`, `OfflinePlayer`

**Completions**: `yes`

Reads in a player name and will return either a `Player` or `OfflinePlayer` depending on the setting of the `mode` parameter.

A value of `%self` will refer to the command sender. This is useful to provide as a `default`. When the command
sender is the console then an error will be returned "When console a player name is required".

#### mode

Can be either `online` or `offline` (default). 

When `mode` is `online` then completion will only show currently online players and when executing will only accept a player
that is online.  Returns a `Player` object to the method.

When `mode` is `offline` then completion will show all players, online and offlline, and when executing will validate
that the player exists.  Returns an `OfflinePlayer` object to the method.

!!! example
    ```java
    @Command("mycmd")
    public class MainCommand extends BukkitCommand {
    
        @Arg("cmd1 @player(default=%self, mode=online")
        public void myCmd1(CommandSender sender, Player player) {
            ...
        }
        
        @Arg("cmd1 @player(mode=offline")
        public void myCmd1(CommandSender sender, OfflinePlayer player) {
            ...
        }
    }
    ```