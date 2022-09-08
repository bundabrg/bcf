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

package au.com.grieve.bcf.impl.line;

import au.com.grieve.bcf.ParsedLine;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class DefaultParsedLine implements ParsedLine {
    private final List<String> words;
    private final String prefix;
    @Setter
    private int wordIndex = 0;

    public DefaultParsedLine(String line, String prefix) {
        this.words = Arrays.asList(line.split(" "));
        this.prefix = prefix;
    }

    public DefaultParsedLine(List<String> args, String prefix) {
        this.words = new ArrayList<>(args);
        this.prefix = prefix;
    }

    /**
     * Return the unparsed line
      * @return Unparsed line
     */
    public String getLine() {
        return String.join(" ", this.words);
    }

    /**
     * Return the current word being completed
     * @return current word
     */
    public String getCurrentWord() {
        return words.get(0);
    }


}
