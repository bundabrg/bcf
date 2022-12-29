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

package au.com.grieve.bcf.impl.parsertree;

import static org.junit.jupiter.api.Assertions.assertEquals;

import au.com.grieve.bcf.ParserTree;
import org.junit.jupiter.api.Test;

class NullNodeTest {

  @Test
  void leafs_1() {
    ParserTree<Object> node = new NullNode<>();
    assertEquals(1, node.leafs().size());
  }

  @Test
  void leafs_2() {
    ParserTree<Object> node = new NullNode<>();
    node.then(new NullNode<>()).then(new NullNode<>());
    assertEquals(2, node.leafs().size());
  }

  @Test
  void leafs_3() {
    ParserTree<Object> node = new NullNode<>();
    node.then(new NullNode<>().then(new NullNode<>()).then(new NullNode<>()))
        .then(new NullNode<>());
    assertEquals(3, node.leafs().size());
  }

  @Test
  void leafs_4() {
    ParserTree<Object> node1 = new NullNode<>();
    node1
        .then(new NullNode<>().then(new NullNode<>()).then(new NullNode<>()))
        .then(new NullNode<>());

    ParserTree<Object> node2 = new NullNode<>().then(new NullNode<>()).then(node1);

    assertEquals(4, node2.leafs().size());
  }
}
