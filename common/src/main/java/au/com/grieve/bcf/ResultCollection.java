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

import au.com.grieve.bcf.exception.ResultNotSetException;
import java.util.ArrayList;
import java.util.List;

public class ResultCollection extends ArrayList<Result> {

  public List<Object> toObjects() throws ResultNotSetException {
    List<Object> result = new ArrayList<>();
    for (Result r : this) {
      Object o = r.getValue();
      if (!r.isSuppress()) {
        result.add(o);
      }
    }
    return result;
  }

  /**
   * Return all objects that we can, even suppressed
   *
   * @return All objects
   */
  public List<Object> allObjects() {
    List<Object> result = new ArrayList<>();
    for (Result r : this) {

      Object o;
      try {
        o = r.getValue();
      } catch (ResultNotSetException e) {
        continue;
      }
      result.add(o);
    }
    return result;
  }
}
