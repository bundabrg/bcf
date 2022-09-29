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

package au.com.grieve.bcf.platform.minecraft.nms;

import au.com.grieve.bcf.platform.minecraft.nms.v1_18.ServerDefinition_v1_18;
import au.com.grieve.bcf.platform.minecraft.nms.v1_19.ServerDefinition_v1_19;

public class ServerVersion {
  public static ServerDefinition getNMS(String version) {
    // Version is in the following format: git-Paper-160 (MC: 1.19.2)

    String versionNumber = version.split("\\(MC: ", 2)[1].split("\\)", 2)[0];

    switch (versionNumber) {
      case "1.19":
      case "1.19.1":
      case "1.19.2":
        return new ServerDefinition_v1_19();
      case "1.18":
      case "1.18.1":
      case "1.18.2":
        return new ServerDefinition_v1_18();
    }
    throw new RuntimeException("Unsupported Minecraft Version");
  }
}
