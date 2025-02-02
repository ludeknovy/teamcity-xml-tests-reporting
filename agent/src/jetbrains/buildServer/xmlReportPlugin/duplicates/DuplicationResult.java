/*
 * Copyright 2000-2022 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.xmlReportPlugin.duplicates;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 * User: vbedrosova
 * Date: 21.02.11
 * Time: 14:53
 */
public class DuplicationResult {
  private final int myLines;
  private final int myTokens;
  private int myHash;

  @NotNull
  private final List<DuplicatingFragment> myFragments = new ArrayList<DuplicatingFragment>();

  public DuplicationResult(int lines, int tokens) {
    myLines = lines;
    myTokens = tokens;
  }

  public int getLines() {
    return myLines;
  }

  public int getTokens() {
    return myTokens;
  }

  public void addFragment(@NotNull DuplicatingFragment fragment) {
    myFragments.add(fragment);
  }

  @NotNull
  public List<DuplicatingFragment> getFragments() {
    return myFragments;
  }

  public int getHash() {
    return myHash;
  }

  public void setHash(int hash) {
    myHash = hash;
  }

  public void setFragmentHashes() {
    final Set<Integer> used = new HashSet<Integer>();
    for (DuplicatingFragment fragment : myFragments) {
      int hash = ("" + fragment.getPath() + fragment.getLine() + myHash).hashCode();
      while (!used.add(hash)) {
        hash++;
      }
      fragment.setHash(hash);
    }
  }
}
