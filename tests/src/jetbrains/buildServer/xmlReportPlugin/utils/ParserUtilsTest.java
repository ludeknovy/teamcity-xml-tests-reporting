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

package jetbrains.buildServer.xmlReportPlugin.utils;

import java.io.File;
import java.io.FileNotFoundException;
import jetbrains.buildServer.xmlReportPlugin.TestUtil;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ParserUtilsTest {

  private File getReport(final String name) throws FileNotFoundException {
    return TestUtil.getTestDataFile(name, "pmd");
  }

  private void doTestReportComplete(final String name, final boolean expected) throws FileNotFoundException {
    final File report = getReport(name);
    Assert.assertTrue(report.exists());
    Assert.assertEquals(ParserUtils.isReportComplete(report, null), expected);
  }

  @Test
  public void testIsReportComplete_Simple() throws Exception {
    doTestReportComplete("simple.xml", true);
  }

  @Test
  public void testIsReportComplete_XXE_File() throws Exception {
    doTestReportComplete("xml-xxe-file.xml", true);
  }

  @Test
  public void testIsReportComplete_XXE_URL() throws Exception {
    doTestReportComplete("xml-xxe-url.xml", true);
  }

  @Test
  public void testIsReportComplete_XXE_File_First() throws Exception {
    doTestReportComplete("xml-xxe-file-first.xml", true);
  }

  @Test(timeOut = 5 * 1000)
  public void testIsReportComplete_XML_Bomb() throws Exception {
    doTestReportComplete("xml-bomb.xml", false);
  }

  @DataProvider(name = "isNumberData")
  public Object[][] isNumberData() {
    return new Object[][] {
      {"0", true},
      {"+0", true},
      {"-0", true},
      {"1", true},
      {"+1", true},
      {"-1", true},
      {"019", true},
      {"+019", true},
      {"-019", true},
      {"1234567890", true},
      {"-1234567890", true},
      {"+1234567890", true},
      {"123456789098765432123456789098765432123456789098765432", true},
      {"+", false},
      {"-", false},
      {" ", false},
      {"", false},
      {null, false},
      {"text", false},
      {"0.19", false},
      {"0,19", false},
      {" 19", false},
      {"19 ", false},
      {"1 9", false},
      {"123456789098765432123456789098765432123456789098765432 ", false},
    };
  }

  @Test(dataProvider = "isNumberData")
  public void isNumber(String str, Boolean isNumber) {
    Assert.assertEquals(Boolean.valueOf(ParserUtils.isNumber(str)), isNumber, str + " != " + isNumber);
  }
}