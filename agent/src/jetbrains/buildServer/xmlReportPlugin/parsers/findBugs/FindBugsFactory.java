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

package jetbrains.buildServer.xmlReportPlugin.parsers.findBugs;

import java.util.Map;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.xmlReportPlugin.*;
import jetbrains.buildServer.xmlReportPlugin.inspections.InspectionParsingResult;
import org.jetbrains.annotations.NotNull;

/**
 * User: vbedrosova
 * Date: 23.01.11
 * Time: 20:32
 */
public class FindBugsFactory implements ParserFactory {
  @NotNull
  @Override
  public String getType() {
    return "findBugs";
  }

  @NotNull
  @Override
  public ParsingStage getParsingStage() {
    final String stageName = TeamCityProperties.getPropertyOrNull(TEAMCITY_PROPERTY_STAGE_PREFIX + "." + getType());
    final ParsingStage stage = ParsingStage.of(stageName);
    if (stage != null) return stage;
    else return ParsingStage.BEFORE_FINISH;
  }

  @NotNull
  public Parser createParser(@NotNull ParseParameters parameters) {
    final Map<String,String> params = parameters.getParameters();
    return new FindBugsReportParser(parameters.getInspectionReporter(), XmlReportPluginUtil.getFindBugsHomePath(params),
                                    parameters.getCheckoutDir(), XmlReportPluginUtil.isFindBugsLookupFiles(params));
  }

  @NotNull
  public ParsingResult createEmptyResult() {
    return InspectionParsingResult.createEmptyResult();
  }
}
