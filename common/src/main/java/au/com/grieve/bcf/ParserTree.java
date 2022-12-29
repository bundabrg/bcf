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

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lombok.NonNull;

/** A node in the argument tree */
public interface ParserTree<DATA> {

  /**
   * Add a child node to this one
   *
   * @param node Node to add
   * @return Ourself
   */
  ParserTree<DATA> then(ParserTree<DATA> node);

  /**
   * Remove a child node from this one
   *
   * @param node Node to remove
   * @return Ourself
   */
  ParserTree<DATA> remove(ParserTree<DATA> node);

  ParserTree<DATA> forEachLeaf(Consumer<ParserTree<DATA>> consumer);

  /**
   * Return a list of the leafs
   *
   * @return list of leafs
   */
  Collection<ParserTree<DATA>> leafs();

  /**
   * Set Execute Handler, called when this node is reached
   *
   * @param handler Execute handler
   * @return Ourself
   */
  ParserTree<DATA> execute(ParserTreeHandler<ExecuteContext<DATA>> handler);

  /**
   * Set Error Handler, called when an error occurs
   *
   * @param handler Error handler
   * @return Ourself
   */
  ParserTree<DATA> error(ParserTreeHandler<ErrorContext<DATA>> handler);

  /**
   * Set a completion handler, called when any completions exist
   *
   * @param handler Completion handler
   * @return Ourself
   */
  ParserTree<DATA> complete(ParserTreeHandler<CompleteContext<DATA>> handler);

  /**
   * Set Fallback handler, called when more input exists and no child nodes exist
   *
   * @param handler Fallback handler
   * @return Ourself
   */
  ParserTree<DATA> fallback(ParserTreeFallbackHandler<DATA> handler);

  ParserTree<DATA> requires(Predicate<RequiresContext<DATA>> requirement);

  /**
   * Walk over every node
   *
   * @param consumer Execute for every node
   * @return Ourself
   */
  ParserTree<DATA> walk(Consumer<List<ParserTree<DATA>>> consumer);

  void walk(Consumer<List<ParserTree<DATA>>> consumer, List<ParserTree<DATA>> chain);

  /**
   * Return list of direct decendents
   *
   * @return Direct Children
   */
  Collection<ParserTree<DATA>> children();

  /**
   * Execute on each of our children
   *
   * @param consumer What to execute
   * @return Ourself
   */
  ParserTree<DATA> forEach(Consumer<ParserTree<DATA>> consumer);

  /**
   * Return the best candidate for the supplied context
   *
   * @param context Parser Tree Context
   * @return Best Candidate
   */
  @NonNull
  ParserTreeResult<DATA> parse(ParserTreeContext<DATA> context);

  @NonNull
  ParserTreeResult<DATA> parse(ParsedLine line, DATA data);

  @NonNull
  ParserTreeResult<DATA> parse(String line, DATA data);

  /**
   * Return the number of direct descendants we have
   *
   * @return number of children
   */
  int size();
}
