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

package au.com.grieve.bcf.impl.error;

import au.com.grieve.bcf.CommandError;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;

@Getter
public class InvalidFormatError implements CommandError {
  private final Set<String> format = new HashSet<>();

  public InvalidFormatError(String format) {
    this.format.add(format);
  }

  @Override
  public String getName() {
    return "invalid_format";
  }

  @Override
  public void merge(CommandError error) {
    assert (error instanceof InvalidFormatError);
    this.format.addAll(((InvalidFormatError) error).getFormat());
  }

  @Override
  public String toString() {
    return "Invalid format, should be"
        + (format.size() > 1 ? " one of" : "")
        + ": "
        + String.join(", ", format);
  }
}
