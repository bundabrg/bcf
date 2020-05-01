## Maven

Add the following repository to your `pom.xml`

```xml
<!-- Bundabrg's Repo -->
<repository>
    <id>bundabrg-repo</id>
    <url>https://repo.worldguard.com.au/repository/maven-public</url>
    <releases>
        <enabled>true</enabled>
    </releases>
    <snapshots>
        <enabled>false</enabled>
    </snapshots>
</repository>   
```

Add the following dependency to your `pom.xml`
```xml
<dependency>
    <groupId>au.com.grieve.bcf</groupId>
    <artifactId>bukkit</artifactId>
    <version>1.2.0</version>
    <scope>provided</scope>
</dependency>
```

Shade the library into your own code by adding in your `pom.xml`
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.2.3</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                       <goal>shade</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <relocations>
                    <relocation>
                        <pattern>au.com.grieve.bcf</pattern>
                        <shadedPattern>${project.groupId}.${project.artifactId}.bcf</shadedPattern>
                    </relocation>
                </relocations>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## Command Class

To create a command extend from an appropriate BaseCommand class and annotate with @Command. For a Bukkit plugin use `BukkitCommand`.

!!! example
    ```java
    @Command("mycmd")
    public class MainCommand extends BukkitCommand {
       
        @Arg("list")
        public void doList(CommandSender sender) {
            sender.spigot().sendMessage(
                new ComponentBuilder("Reached List").color(ChatColor.GREEN).create()
            );
        }
    }
    ```

You can of course (and are encouraged) to extend any of your command classes to better
separate groups of commands.  This can also be an effective way to allow 3rd parties to
add commands under your plugin.

## Command Manager

During your plugin initialization you need to setup a new Command Manager instance.  For Bukkit
use a `BukkitCommandManager`, passing your own plugin as a parameter.

Then register each of your Command classes with the manager, passing the class type as a parameter.

!!! example
    ```java
    
    public final class MyPlugin extends JavaPlugin {
        @Getter
        private BukkitCommandManager bcf;
    
        @Override
        public void onEnable() {
            // Setup Command Manager
            bcf = new BukkitCommandManager(this);
    
            // Register Commands
            bcf.registerCommand(MainCommand.class);
        }
    }
    ```
## Parser

You may wish to add your own custom parser. Either the built in ones are not sufficient or you
have some custom arguments that need to be parsed in a special way.

!!! note
    If you do write a Parser that may be useful to others then please to send a PR
    to have it included as a built-in type. `
    
A parser can be though of as having the following attributes:

1. It will be provided a list of input words available to it at its position in the `@Arg` command string
and must consume 0 or more of them.

2. If possible it should be able to use partial input to provide command completion

3. It should be able to validate its input and return a concrete object

A custom parser should extend `Parser` or a class derived from this.

The parser is registered by calling the `registerParser` method on the `CommandManager` and from that point its name
prefixed with `@` can be used in an `@Arg` string.

!!! example
    ```java
    bcf.registerParser("myparser", MyParser.class);
    ```

### Overrides

The three important methods to provide are:

1. `parse` - Consume input words

2. `compete` - Return list of completions for the input

3. `result` - Return a concrete object for the input


#### parse

!!! definition
    ```java
    public void parse(List<String> input, boolean defaults) throws ParserRequiredArgumentException {
        ...
    }
    ```    

Provided a list of words from input available at this parsers point in the `@Arg` string. Any input
for this parser must be removed from the head of the list with any remaining items being available
for the calling process.

If `defaults` is set to `false` then no action that forgives missing input should be allowed.  This means
even if a default is set missing input should cause an error.

Any required input that is missing (and not provided another way like through a `default` parameter) must
throw a `ParserRequiredArgumentException`

!!! important
    As most Parsers only consume 1 input a custom parser that also only consumes 1 input should extend
    `SingleParser` which handles this method for you. Make use of `getInput()` to get the consumed
    input.
    
#### complete

!!! definition
    ```java
    protected List<String> complete() {
        ...
    }
    ```

Return a list of completions for the consumed input ideally filtering it by what has been
entered and limiting it to a maximum of 20 items.  If no completions are available then
return an empty list.

The results are cached.


#### result

!!! definition
    ```java
    protected Object result() throws ParserInvalidResultException {
        ...
    }
    ```

Return a concrete Object for the input consumed.  If there is no valid result for the input then
this must throw a `ParserInvalidResultException` which will cause it to be rejected as a valid
command candidate.

The results are cached.