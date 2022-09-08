![Logo](docs/img/title.png)

[![MIT license](https://img.shields.io/badge/License-MIT-blue.svg)](https://lbesson.mit-license.org/)
[![GitHub release](https://img.shields.io/github/release/Bundabrg/bcf)](https://GitHub.com/Bundabrg/bcf/releases/)
[![GitHub commits](https://img.shields.io/github/commits-since/Bundabrg/bcf/latest)](https://GitHub.com/Bundabrg/bcf/commit/)
[![Github all releases](https://img.shields.io/github/downloads/Bundabrg/bcf/total.svg)](https://GitHub.com/Bundabrg/bcf/releases/)
![HitCount](http://hits.dwyl.com/bundabrg/portalnetwork.svg)

![Workflow](https://github.com/bundabrg/bcf/workflows/build/badge.svg)
[![Maintenance](https://img.shields.io/badge/Maintained%3F-yes-green.svg)](https://GitHub.com/Bundabrg/bcf/graphs/commit-activity)
[![GitHub contributors](https://img.shields.io/github/contributors/Bundabrg/bcf)](https://GitHub.com/Bundabrg/bcf/graphs/contributors/)
[![GitHub issues](https://img.shields.io/github/issues/Bundabrg/bcf)](https://GitHub.com/Bundabrg/bcf/issues/)
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/Bundabrg/bcf.svg)](http://isitmaintained.com/project/Bundabrg/bcf "Average time to resolve an issue")
[![GitHub pull-requests](https://img.shields.io/github/issues-pr/Bundabrg/bcf)](https://GitHub.com/Bundabrg/bcf/pull/)
 

---

[**Documentation**](https://bundabrg.github.io/bcf/)

[**Source Code**](https://github.com/bundabrg/bcf/)

---

`bcf` is a Command Management tool suitable for use in Bukkit/Spigot/PaperMC, Bungeecord and Standalone applications though 
it may support other platforms in the future. 

It allows one to easily provide full command completion for all your commands as
well as automatically resolve and marshal input from the user so your command
gets the data it is expecting to get. For example if your command is expecting a player
name then available players will be provided during command completion and your command
method will receive the actual player object, or the command sender will receive an error
explaining what the error is.

This library is inspired by aikar's [Annotation Command Framework (ACF)](https://github.com/aikar/commands) which used
before I decided on the crazy idea of writing my own to support some extra  features I felt I desperately needed for 
some reason. The name `bcf` is used in recognition of that.

It was originally created for minecraft projects but now supports standalone console apps as you can see below.

![standalone](https://bundabrg.github.io/bcf/img/standalone.gif)

## Features

* Supports Bukkit (Spigot/PaperMC), Bungeecoord and Standalone applications

* Define your commands by simply extending a au.com.grieve.bcf.Command derived class (like `BukkitCommand`) and annotating it with
a `@Command` to define it, with any aliases separated by a `|`.

* Annotate your method with `@Arg` to define what arguments it is expecting. This is a string of arguments that may
consume 0 or more words from a users input both to provide command completion as well as to fully resolve and pass 
objects to the method.

* Multiple annotations can be used and each will be checked in turn.

* Nearly all annotations can be added to your class to apply to all methods (and child classes). 
        
* Your class can extend another command class to inherit any of its settings. For example a 3rd party
plugin could extend your command class to add sub-commands under your own.

* Create command aliases by adding a `@Command` annotation to a derived class. This allows shortcut commands to jump
straight to a class. For example instead of `/command view playername` you can have `/cv playername`
as an alias

* When a command needs to send an error (for example a parameter is not valid) the class will look for a method
annotated with `@Error`.  If it fails to find one it will check all its parent classes until it reaches the default. This
allows you to override how errors are handled.

* When no command is reached a method annotated with `@Default` is looked for to handle things.  If no method is found
then every parent class is checked until it reaches the default which outputs "Invalid Command".  This can be used to
provide more help.

* Add permission requirements by annotating your class or methods with `@Permission`. The command sender must either be
console or have at least one of the permissions at each level to proceed otherwise both command completion and execution
will be ignored as if the arguments did not exist.

* Support both required and optional positional parameters.  A required parameter must either have a default or
must have valid input provided. Optional parameters with no default and no input will be set to null.

* As well as supporting positional parameters we support named parameters called `switches`.  To pass a switch the 
command sender uses `-<switchname> <value(s)>` and full command completion is provided.  A switch becomes available in the
chain of arguments once it is reached and it can have multiple aliases.  A non required switch with no default value
will be resolved to null. A required switch must be resolved by input sometime after the point it is defined otherwise
the command will be rejected.  Designating a switch parameter means it is no longer treated as positional.

## Quickstart

1. Add the following Maven repository to your `pom.xml`
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
   
2. Add the following dependency to your `pom.xml`
    ```xml
    <dependency>
        <groupId>au.com.grieve.bcf</groupId>
        <artifactId>bukkit</artifactId>
        <version>1.5.0-SNAPSHOT</version>
    </dependency>
    ```
   
3. Shade the library into your own code by adding in your `pom.xml`
    
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
   
4. Create a command class that extends BukkitCommand
    
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
   
5. Create a new CommandManager in your plugin, passing your plugin as a parameter and register your commandclass.

    ```java
    // Setup Command Manager
    bcf = new BukkitCommandManager(this);
    
    // Register Commands
    bcf.registerCommand(new MainCommand());
    ```
   
6. You should now be able to use `/mycmd list` in-game.

Please refer to the documentation for more info on other supported platforms.