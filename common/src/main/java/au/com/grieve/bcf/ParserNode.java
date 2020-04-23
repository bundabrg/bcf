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


import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ParserNode implements Iterable<ParserNode> {

    @Getter
    final List<ParserNode> children;
    private final List<ParserNode> elementsIndex;
    @Getter
    ParserNode parent;
    @Getter
    ParserNodeData data;
    // Method to call for this node
    @Getter
    @Setter
    ParserMethod execute;
    // Method to call if no child node found
    ParserMethod default_;
    // Method to call if an error on children
    @Getter
    @Setter
    ParserMethod error;

    // Permissions
    @Getter
    final List<String> permissions = new ArrayList<>();


    public ParserNode() {
        this.children = new ArrayList<>();
        this.elementsIndex = new ArrayList<>();
        this.elementsIndex.add(this);
    }

    public ParserNode(ParserNodeData data) {
        this();
        this.data = data;
    }

    public boolean isRoot() {
        return parent == null;
    }

    @SuppressWarnings("unused")
    public boolean isLeaf() {
        return children.size() == 0;
    }

    public ParserNode addChild(ParserNode child) {
        assert (child.isRoot());

        child.parent = this;
        this.children.add(child);
        this.registerChildForSearch(child);
        return child;
    }

    @SuppressWarnings("unused")
    public int getLevel() {
        if (this.isRoot())
            return 0;
        else
            return parent.getLevel() + 1;
    }

    private void registerChildForSearch(ParserNode node) {
        elementsIndex.add(node);
        if (parent != null)
            parent.registerChildForSearch(node);
    }

    @SuppressWarnings("unused")
    public ParserNode find(Comparable<ParserNode> cmp) {
        for (ParserNode element : this.elementsIndex) {
            if (cmp.compareTo(element) == 0)
                return element;
        }

        return null;
    }

    @SuppressWarnings("unused")
    public boolean contains(ParserNode node) {
        return children.stream()
                .filter(c -> c.equals(node))
                .findFirst()
                .orElse(null) != null;
    }

    /**
     * Create a new node at path.
     * <p>
     * As this may result in multiple nodes created the new leaf nodes are returned
     */
    public List<ParserNode> create(String path) {
        StringReader reader = new StringReader(path);

        return create(reader);
    }

    public List<ParserNode> create(StringReader reader) {

        List<ParserNode> current = Collections.singletonList(this);

        for (List<ParserNodeData> newData = ParserNodeData.parse(reader); newData.size() > 0; newData = ParserNodeData.parse(reader)) {
            List<ParserNode> newCurrent = new ArrayList<>();

            for (ParserNode node : current) {
                for (ParserNodeData nodeData : newData) {
                    boolean found = false;
                    for (ParserNode child : node.children) {
                        if (child.data.equals(nodeData)) {
                            newCurrent.add(child);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        newCurrent.add(node.addChild(new ParserNode(nodeData)));
                    }
                }
            }

            current = newCurrent;
        }

        return current;
    }

    /**
     * Return a walk of the tree under ourself
     */
    @SuppressWarnings("unused")
    public String walkTree() {
        return walkTree(0);
    }

    private String walkTree(int depth) {
        StringBuilder result = new StringBuilder();

        char[] repeat = new char[depth];
        Arrays.fill(repeat, ' ');
        String pad = new String(repeat);

        result.append(pad).append(toString()).append("\n");

        for (ParserNode n : children) {
            result.append(n.walkTree(depth + 1));
        }
        return result.toString();
    }

    public ParserMethod getDefault() {
        return default_;
    }

    public void setDefault(ParserMethod value) {
        default_ = value;
    }

    @Override
    public @NotNull Iterator<ParserNode> iterator() {
        return new ParserNodeIter<ParserNode>(this);
    }

    @Override
    public String toString() {
        return "node(data=" + (data == null ? "null" : data.toString()) + ", " +
                "execute=" + execute + ", " +
                "default=" + default_ + ", " +
                "error=" + error + ")";
    }

}