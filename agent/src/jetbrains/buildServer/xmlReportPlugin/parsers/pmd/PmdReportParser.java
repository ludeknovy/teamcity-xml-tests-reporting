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

package jetbrains.buildServer.xmlReportPlugin.parsers.pmd;

import java.io.File;
import java.io.IOException;
import jetbrains.buildServer.xmlReportPlugin.Parser;
import jetbrains.buildServer.xmlReportPlugin.ParsingException;
import jetbrains.buildServer.xmlReportPlugin.ParsingResult;
import jetbrains.buildServer.xmlReportPlugin.inspections.InspectionParsingResult;
import jetbrains.buildServer.xmlReportPlugin.inspections.InspectionReporter;
import jetbrains.buildServer.xmlReportPlugin.inspections.InspectionResult;
import jetbrains.buildServer.xmlReportPlugin.inspections.InspectionTypeResult;
import jetbrains.buildServer.xmlReportPlugin.utils.ParserUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


class PmdReportParser implements Parser {
  @NotNull
  private final InspectionReporter myInspectionReporter;

  private int myErrors;
  private int myWarnings;
  private int myInfos;

  public PmdReportParser(@NotNull InspectionReporter inspectionReporter) {
    myInspectionReporter = inspectionReporter;
  }

  @Override
  public boolean parse(@NotNull final File file, @Nullable final ParsingResult prevResult) throws ParsingException {
    if (!ParserUtils.isReportComplete(file, "pmd")) {
      return false;
    }

    try {
      new PmdXmlReportParser(new PmdXmlReportParser.Callback() {
        public void reportInspection(@NotNull final InspectionResult inspection) {
          switch (inspection.getPriority()) {
            case 1:
              ++myErrors;
              break;
            case 2:
              ++myWarnings;
              break;
            default:
              ++myInfos;
          }
          myInspectionReporter.reportInspection(inspection);
        }

        public void reportInspectionType(@NotNull final InspectionTypeResult inspectionType) {
          myInspectionReporter.reportInspectionType(inspectionType);
        }

        @Override
        public void error(@NotNull final String message) {
          myInspectionReporter.error(message);
        }
      }).parse(file);
    } catch (IOException e) {
      throw new ParsingException(e);
    }
    return true;
  }

  public ParsingResult getParsingResult() {
    return new InspectionParsingResult(myErrors, myWarnings, myInfos);
  }
}