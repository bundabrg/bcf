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

package au.com.grieve.bcf.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReflectUtils {
    /**
     * Get all super classes
     */
    public static Class<?>[] getAllSuperClasses(Class<?> clz) {
        List<Class<?>> list = new ArrayList<>();
        while ((clz = clz.getSuperclass()) != null) {
            list.add(clz);
        }
        return list.toArray(new Class<?>[0]);
    }

    /**
     * Get all interfaces
     */
    public static Class<?>[] getAllInterfaces(Class<?> clz) {
        Set<Class<?>> set = new HashSet<>();
        getAllInterfaces(clz, set);
        return set.toArray(new Class<?>[0]);
    }

    private static void getAllInterfaces(Class<?> clz, Set<Class<?>> visited) {
        if (clz.getSuperclass() != null) {
            getAllInterfaces(clz.getSuperclass(), visited);
        }
        for (Class<?> c : clz.getInterfaces()) {
            if (visited.add(c)) {
                getAllInterfaces(c, visited);
            }
        }
    }
}
