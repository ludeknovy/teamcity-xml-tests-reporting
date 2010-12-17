/*
 * Copyright 2000-2010 JetBrains s.r.o.
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

package jetbrains.buildServer.xmlReportPlugin;

import jetbrains.buildServer.agent.BuildProgressLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * User: vbedrosova
 * Date: 16.11.10
 * Time: 18:25
 */
public interface PathParameters {
  @NotNull
  LogAction getWhenNoDataPublished();
  boolean isParseOutOfDate();
  //TODO: use this method instead of global isVerbose()
  boolean isVerbose();
  @NotNull
  public BuildProgressLogger getPathLogger();

  public static enum LogAction {
    DO_NOTHING("nothing") {
      @Override
      public void doLogAction(@NotNull String message, @NotNull BuildProgressLogger logger) {}
    },
    WARNING("warning") {
      @Override
      public void doLogAction(@NotNull String message, @NotNull BuildProgressLogger logger) {
        logger.warning(message);
      }
    },
    ERROR("error") {
      @Override
      public void doLogAction(@NotNull String message, @NotNull BuildProgressLogger logger) {
        logger.error(message);
      }
    };

    private static final List<LogAction> ourActions = Arrays.asList(DO_NOTHING, WARNING, ERROR);

    @NotNull
    private final String myName;

    private LogAction(@NotNull String name) {
      myName = name;
    }

    @NotNull
    private String getName() {
      return myName;
    }

    public abstract void doLogAction(@NotNull String message, @NotNull BuildProgressLogger logger);

    public static LogAction getAction(@Nullable String name) {
      if (name == null) return ERROR;
      for (final LogAction action : ourActions) {
        if (action.getName().equals(name)) return action;
      }
      return ERROR;
    }
  }
}