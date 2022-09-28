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

package au.com.grieve.bcf.platform.bukkit.impl.framework.annotation;

import au.com.grieve.bcf.CompletionCandidateGroup;
import au.com.grieve.bcf.CompletionContext;
import au.com.grieve.bcf.OldExecutionCandidate;
import au.com.grieve.bcf.ExecutionContext;
import au.com.grieve.bcf.CommandErrorCandidate;
import au.com.grieve.bcf.annotation.Default;
import au.com.grieve.bcf.annotation.Error;
import au.com.grieve.bcf.impl.command.AnnotationCommand;
import au.com.grieve.bcf.platform.bukkit.BukkitCommand;
import au.com.grieve.bcf.platform.bukkit.annotation.Permission;
import au.com.grieve.bcf.platform.bukkit.impl.BukkitExecutionContext;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.CommandSender;

public class BukkitAnnotationCommand extends AnnotationCommand implements BukkitCommand {
  @Error
  public void onError(CommandSender sender, List<CommandErrorCandidate> errors) {
    sender.spigot().sendMessage(
        new ComponentBuilder(buildErrorMessage(errors)).color(ChatColor.RED).create()
    );
  }

  // Default Default
  @Default
  public void onDefault(CommandSender sender) {
    sender.spigot().sendMessage(
        new ComponentBuilder("Invalid Command").color(ChatColor.RED).create()
    );
  }

  /**
   * Return the list of permissions on this class
   * @return List of permissions
   */
  public List<String> getPermissions() {
    return Arrays.stream(getClass().getAnnotationsByType(Permission.class))
        .map(Permission::value)
        .collect(Collectors.toList());
  }

  /**
   * Return the list of permissions on the method
   * @return List of permissions
   */
  protected List<String> getPermissions(Method method) {
    return Arrays.stream(method.getAnnotationsByType(Permission.class))
        .map(Permission::value)
        .collect(Collectors.toList());
  }

  /**
   * Return true if the commandsender has permission on the class
   * @param sender The command sender
   * @return true if command sender has permission
   */
  protected boolean testPermission(CommandSender sender) {
    List<String> permissions = getPermissions();
    return permissions.size() == 0 || permissions.stream()
        .map(sender::hasPermission)
        .filter(b -> b)
        .findFirst()
        .orElse(false);
  }

  /**
   * Return true if the commandsender has permission on the method
   * @param sender The command sender
   * @param method The method being tested
   * @return true if command sender has permission
   */
  protected boolean testPermission(CommandSender sender, Method method) {
    List<String> permissions = getPermissions(method);
    return permissions.size() == 0 || permissions.stream()
        .map(sender::hasPermission)
        .filter(b -> b)
        .findFirst()
        .orElse(false);
  }

  @Override
  protected void executeMethod(ExecutionContext context, Method method,
      List<OldExecutionCandidate> candidates, List<CommandErrorCandidate> errors) {

    // Only allowed if sender has permissions
    if (testPermission(((BukkitExecutionContext) context).getCommandSender(), method)) {
      super.executeMethod(context, method, candidates, errors);
    }
  }

  @Override
  public OldExecutionCandidate execute(ExecutionContext context) {
    // Only allowed if sender has permissions
    if (testPermission(((BukkitExecutionContext) context).getCommandSender())) {
      return super.execute(context);
    }
    return null;
  }

  @Override
  public OldExecutionCandidate execute(ExecutionContext context, List<CommandErrorCandidate> errors) {
    // Only allowed if sender has permissions
    if (testPermission(((BukkitExecutionContext) context).getCommandSender())) {
      return super.execute(context, errors);
    }
    return null;
  }

  @Override
  protected void completeMethod(CompletionContext context, Method method,
      List<CompletionCandidateGroup> candidates) {
    // Only allowed if sender has permissions
    if (testPermission(((BukkitExecutionContext) context).getCommandSender(), method)) {
      super.completeMethod(context, method, candidates);
    }
  }

  @Override
  public List<CompletionCandidateGroup> complete(CompletionContext context) {
    // Only allowed if sender has permissions
    if (testPermission(((BukkitExecutionContext) context).getCommandSender())) {
      super.complete(context);
    }
    return new ArrayList<>();
  }
}
