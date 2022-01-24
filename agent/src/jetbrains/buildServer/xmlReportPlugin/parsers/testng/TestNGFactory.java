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

package jetbrains.buildServer.xmlReportPlugin.parsers.testng;

import jetbrains.buildServer.xmlReportPlugin.*;
import jetbrains.buildServer.xmlReportPlugin.tests.MillisecondDurationParser;
import jetbrains.buildServer.xmlReportPlugin.tests.TestParsingResult;
import org.jetbrains.annotations.NotNull;

public class TestNGFactory implements ParserFactory {
  @NotNull
  @Override
  public String getType() {
    return "testng";
  }

  @NotNull
  @Override
  public ParsingStage getParsingStage() {
    return ParsingStage.RUNTIME;
  }

  @NotNull
  public Parser createParser(@NotNull ParseParameters parameters) {
    return new TestNGReportParser(parameters.getTestReporter(), new MillisecondDurationParser(),
                                  XmlReportPluginUtil.isLogInternalSystemError(parameters.getParameters()));
  }

  @NotNull
  public ParsingResult createEmptyResult() {
    return TestParsingResult.createEmptyResult();
  }
}
