## Example

When someone is typing a command or executing it we can see it as being a list of words. Lets look at
the following fictional command:

    /sudo tp notch ~ ~25 ~ -world nether 

The command can be viewed as a list of words and can be broken down into:
 
* `sudo` - The main command itself
* `tp` - A sub-command
* `notch` - A players name
* `~ ~25 ~` - a Location
* `-world nether` - a named parameter defining the world
 
We call the full command string the `input`.
 
We could define our class as follows to handle this particular command (ignore the @Arg for the moment)

```java
@Command("sudo")
class MainCommand extends BukkitCommand {
    @Arg("tp @world(switch=world, required=false) @player(required=true, mode=online) @location")
    public void doTeleport(CommandSender sender, World world, Player player, Location location) {
       .
    }
}
```

This shows the following interesting things:

1. The parameters the method receives are fully resolved objects and not just the input strings. This means the method does
not have to deal with all the complexity of providing command completion nor need to perform any validation. If
a command is not valid then it won't reach the method and may instead match another more specific
method or it will raise an error.

2. `sudo` and `tp` are not passed to the method and are only interesting so far as part of deciding which
method to call.

3. There is an extra parameter to the method `sender` which represents the command sender (player or console)

4. The argument `player` consumes a single word from the input, `notch`, and returns a Player object representing an online player.

5. The argument `location` consumes 3 words from the input, `~ ~25 ~`, and returns a Location object

6. The argument `world` is special as it is designated as a `switch` in the @Arg and thus is a named parameter. A named parameter can appear anywhere in
the input as long as it comes at or after the point it is defined (in @Arg its defined right after `tp`) and if the input contains
its name prefixed with a `-`. So this parameter consumes `-world nether` and will return a World object.

What happens whilst someone is entering the command?

1. After typing `/sudo ` an auto-complete of `tp` will show.

2. After entering `tp` an auto-complete list of online players is shown and is filtered as the player types. A `-world` is
also shown as an option when there is no partial player name entered.

3. After filling in the player name, if it is valid, a location autocomplete will show using the players existing position
if needed. A `-world` is also shown as an option when there is no partial location entered.

4. After filling in the location, if it is valid, a `-world` is shown. The player can choose to hit enter to not fill
it in (as its not a required parameter) or they can type `-world` in which case a list of worlds are provided as part
of the auto-complete. Note that they could have entered this in at any time after entering `tp`.


## Parsers

The magic happens through the use of a `Parser` that understands both how to consume 0 or more words from the input into
a fully resolved object but also how to auto-complete partial inputs.

The annotation `@Arg` defines a chain of parsers to be used when consuming input. Only once all the required parsers are
consumed will a method be invoked.

A parser is typically defined by adding an `@` and its name to the `@Arg` string, and each additional parser is separated 
by a space.

There is one special parser of note called the `LiteralParser` and it does not start with an `@`. You will note in the 
example above we have `tp` in arg. This is a `LiteralParser` and it will consume its own name from the input without
passing anything to the method. A `LiteralParser` can also provide multiple aliases by separating each alias with a `|`. So
in our example above we could have defined the sub-command as `teleport|tp`.  In this case it would accept either 
as input and will show both during autocomplete.

A parser can have parameters that define its behaviour. These are passed by adding a list of value=key pairs after the
parser name inside `(` and `)` brackets. In the example above `@world` and `@player` both have defined parameters whereas
the `@location` parser does not.

Many parsers will have common parameters. For example all parsers have a `required` boolean to determine if it is
a required argument. They also all have a `default` which is provided when no input is provided for that parser.

A special parameter called `switch` is used on the `@world` parser. This turns the `@world` into a named parameter instead
of a positional parameter which means it can be provided in the input anywhere after it is defined as long as the input
contains the special sequence `-` followed by any of the names defined in the switch. For `@world` only a single name
was given so entering `-world` would then trigger that parser to consume the next input(s). Multiple aliases can be
defined by separating each alias with a `|`. An example would be `@world(switch=world|w)` and would allow either `-w` or
`-world`.

Some parsers have unique parameters. For example the `@player` parser used in the example has a `mode` parameter that defines
if the player should currently be online or can be any player (offline). This affects what is shown in auto-complete as well
as if the input is valid.

A Parser will typically consume 1 word from the input but it is possible for a Parser to consume no words as it may rely
on data from a previous Parser or on something entirely unrelated to input, and as in the case of the `@location` parser in the example 
it can consume multiple words from the input.

Finally a BukkitCommand derived class will always provide a CommandSender parameter as a method's first parameter.  This will be either
the console or the player who is executing the command.