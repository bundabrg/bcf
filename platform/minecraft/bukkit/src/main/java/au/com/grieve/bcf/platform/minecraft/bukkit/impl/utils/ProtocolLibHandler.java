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

package au.com.grieve.bcf.platform.minecraft.bukkit.impl.utils;

import au.com.grieve.bcf.CompletionCandidateGroup;
import au.com.grieve.bcf.platform.minecraft.bukkit.impl.BukkitCommandManager;
import au.com.grieve.bcf.platform.minecraft.bukkit.impl.BukkitCommandShim;
import com.comphenix.protocol.PacketType.Play;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

public class ProtocolLibHandler {

  public static void hookTabComplete(BukkitCommandManager manager) {
    ProtocolLibrary.getProtocolManager()
        .addPacketListener(
            new PacketAdapter(
                manager.getPlugin(), ListenerPriority.NORMAL, Play.Client.TAB_COMPLETE) {
              @Override
              public void onPacketReceiving(PacketEvent event) {
                int id = event.getPacket().getIntegers().read(0);
                String input = event.getPacket().getStrings().read(0);

                if (!input.startsWith("/")) {
                  return;
                }

                input = input.substring(1);
                String[] args = input.split(" +", 2);
                BukkitCommandShim shim = manager.getCommandShimMap().get(args[0]);

                if (shim == null) {
                  return;
                }

                // We found something to handle so prevent the server seeing this packet
                event.setCancelled(true);

                int startRange =
                    1 + args[0].length() + 1 + (args.length > 1 ? args[1].lastIndexOf(' ') : 0) + 1;

                List<CompletionCandidateGroup> completions =
                    shim.advancedTabComplete(
                        event.getPlayer(),
                        args[0],
                        args.length > 1 ? args[1].split(" +", -1) : new String[0]);

                PacketContainer packet = new PacketContainer(Play.Server.TAB_COMPLETE);
                packet.getIntegers().write(0, id);

                List<Suggestion> suggestions =
                    completions.stream()
                        .flatMap(
                            cg ->
                                cg.getMatchingCompletionCandidates().stream()
                                    .map(
                                        c ->
                                            new Suggestion(
                                                new StringRange(
                                                    startRange, startRange + c.getValue().length()),
                                                c.getValue(),
                                                cg.getDescription() != null
                                                    ? cg::getDescription
                                                    : null)))
                        .collect(Collectors.toList());

                if (suggestions.size() > 0) {
                  Suggestions suggestionCollection =
                      new Suggestions(
                          new StringRange(
                              startRange,
                              suggestions.stream()
                                  .map(s -> s.getRange().getEnd())
                                  .max(Integer::compareTo)
                                  .orElse(startRange)),
                          suggestions);

                  packet.getStructures().withType(Suggestions.class).write(0, suggestionCollection);
                  try {
                    ProtocolLibrary.getProtocolManager()
                        .sendServerPacket(event.getPlayer(), packet);
                  } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                  }
                }
              }
            });
  }
}
