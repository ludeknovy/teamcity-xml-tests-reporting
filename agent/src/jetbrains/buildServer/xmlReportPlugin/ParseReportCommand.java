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

package jetbrains.buildServer.xmlReportPlugin;

import java.io.File;
import jetbrains.buildServer.xmlReportPlugin.utils.LoggingUtils;
import org.jetbrains.annotations.NotNull;

/**
 * User: vbedrosova
 * Date: 16.12.10
 * Time: 18:06
 */
public class ParseReportCommand implements Runnable {
  @NotNull
  private final File myFile;

  @NotNull
  private final ParseParameters myParameters;

  @NotNull
  private final RulesState myRulesState;

  @NotNull
  private final ParserFactory myParserFactory;

  public ParseReportCommand(@NotNull final File file,
                            @NotNull final ParseParameters parameters,
                            @NotNull final RulesState rulesState,
                            @NotNull final ParserFactory parserFactory) {
    myFile = file;
    myParameters = parameters;
    myRulesState = rulesState;
    myParserFactory = parserFactory;
  }

  @NotNull
  public ParserFactory.ParsingStage getParsingStage() {
    return myParserFactory.getParsingStage();
  }

  public void run() {
    final Parser parser = myParserFactory.createParser(myParameters);

    boolean finished;
    Throwable problem = null;
    try {
      finished = parser.parse(myFile, myRulesState.getParsingResult(myFile));
    } catch (ParsingException e) {
      finished = true;
      problem = e;
    } catch (Throwable t) {
      finished = true;
      problem = t;
      LoggingUtils.logException("Unexpected exception occurred while parsing " + myFile, t, myParameters.getThreadLogger());
    }

    final ParsingResult parsingResult = parser.getParsingResult();
    assert parsingResult != null;

    if (problem != null) parsingResult.setProblem(problem);

    if (finished) { // file processed
      parsingResult.logAsFileResult(myFile, myParameters);
      myRulesState.setReportState(myFile, problem == null ? ReportStateHolder.ReportState.PROCESSED : ReportStateHolder.ReportState.ERROR, parsingResult);
    } else {
      //todo: log file not processed
      myRulesState.setReportState(myFile, ReportStateHolder.ReportState.ERROR, parsingResult);
    }
  }
}