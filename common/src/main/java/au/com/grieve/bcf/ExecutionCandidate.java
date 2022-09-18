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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/** Contains a method to execute along with all its parameters */
public interface ExecutionCandidate {
  /**
   * Return the class instance the method is a member of
   *
   * @return Class of method
   */
  Object getInstance();

  /**
   * Return the method that will be executed
   *
   * @return Method
   */
  Method getMethod();

  /**
   * Return list of parameters to be passed to the method
   *
   * @return List of parameters
   */
  List<Object> getParameters();

  /**
   * Returns the weight of this Candidate. Heavier is better
   *
   * @return weight
   */
  int getWeight();

  /**
   * Invoke the method, prepending passed in args then adding the stored parameters, filling in any
   * missing parameters with null
   *
   * @param args Arguments to prepend to the parameters
   * @return data from the method
   */
  Object invoke(Object... args) throws InvocationTargetException, IllegalAccessException;
}
