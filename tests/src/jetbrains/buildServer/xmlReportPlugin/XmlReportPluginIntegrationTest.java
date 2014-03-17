package jetbrains.buildServer.xmlReportPlugin;

import jetbrains.buildServer.AgentServerFunctionalTestCase;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.agent.duplicates.DuplicatesReporter;
import jetbrains.buildServer.agent.inspections.InspectionReporter;
import jetbrains.buildServer.serverSide.BuildTypeEx;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.xmlReportPlugin.parsers.antJUnit.AntJUnitFactory;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @User Victory.Bedrosova
 * 12/16/13.
 */
@Test
public class XmlReportPluginIntegrationTest extends AgentServerFunctionalTestCase {

  public static final String RUN_TYPE = "reportPublisher";

  @NotNull
  private File myCheckoutDir;

  @BeforeClass
  @Override
  protected void setUpClass() {
    super.setUpClass();

  }

  @BeforeMethod
  @Override
  protected void setUp1() throws Throwable {
    super.setUp1();
    myCheckoutDir = createTempDir();
    new XmlReportPlugin(Collections.<String, ParserFactory>singletonMap("junit", new AntJUnitFactory()),
                        getAgentEvents(),
                        getExtensionHolder().findSingletonService(InspectionReporter.class),
                        getExtensionHolder().findSingletonService(DuplicatesReporter.class));
    registerRunner(new AgentBuildRunner() {
      @NotNull
      public BuildProcess createBuildProcess(@NotNull final AgentRunningBuild runningBuild, @NotNull final BuildRunnerContext context) {
        return new BuildProcess() {
          public void start() { }
          public boolean isInterrupted() { return false; }
          public boolean isFinished() { return false; }
          public void interrupt() { }

          @NotNull
          public BuildFinishedStatus waitFor() throws RunBuildException {
            try {
              final File reportFile = getCheckoutDirFile("report.xml");
              FileUtil.writeFile(reportFile,
                                 "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                                 "<testsuite errors=\"0\" failures=\"0\" hostname=\"ruspd-student3\" name=\"TestCase\" tests=\"1\" time=\"0.031\"\n" +
                                 "           timestamp=\"2008-10-30T17:11:25\">\n" +
                                 "  <properties/>\n" +
                                 "  <testcase classname=\"TestCase\" name=\"test\" time=\"0.031\"/>\n" +
                                 "</testsuite>\n",
                                 "UTF-8"
              );
              // Make sure file is created after build start, as some
              // filesystems have 1 second last-modified resolution.
                assertTrue("Failed to update 'last-modified' attribute of a report", reportFile.setLastModified(reportFile.lastModified() + 1000));

              final File resultFile = getCheckoutDirFile("result.xml");
              FileUtil.writeFile(resultFile,
                                 "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                                 "<testsuite errors=\"0\" failures=\"1\" hostname=\"ruspd-student3\" name=\"TestCase\" tests=\"1\" time=\"0.047\"\n" +
                                 "           timestamp=\"2008-10-30T17:11:25\">\n" +
                                 "  <properties/>\n" +
                                 "  <testcase classname=\"TestCase\" name=\"test\" time=\"0.047\">\n" +
                                 "    <failure message=\"Assertion message from test\" type=\"junit.framework.AssertionFailedError\">\n" +
                                 "      junit.framework.AssertionFailedError: Assertion message from test\n" +
                                 "      at TestCase.test(Unknown Source)\n" +
                                 "    </failure>\n" +
                                 "  </testcase>\n" +
                                 "</testsuite>\n",
                                 "UTF-8"
              );
              // Make sure file is created after build start, as some
              // filesystems have 1 second last-modified resolution.
              assertTrue("Failed to update 'last-modified' attribute of a report", resultFile.setLastModified(resultFile.lastModified() + 1000));

            } catch (IOException e) {
              throw new RunBuildException(e);
            }
            return BuildFinishedStatus.FINISHED_SUCCESS;
          }
        };
      }

      @NotNull
      public AgentBuildRunnerInfo getRunnerInfo() {
        return new AgentBuildRunnerInfo() {
          @NotNull
          public String getType() {
            return RUN_TYPE;
          }

          public boolean canRun(@NotNull final BuildAgentConfiguration agentConfiguration) {
            return true;
          }
        };
      }
    });
  }

  @NotNull
  private File getCheckoutDirFile(@NotNull String name) {
    return new File(myCheckoutDir, name);
  }

  @Test
  public void testUnexisting() throws Exception {
    doTest("abrakadabra.xml", 0, "No reports found");
  }

  @Test
  public void testUnexistingAbsolute() throws Exception {
    doTest("##C_D##/abrakadabra.xml", 0, "No reports found");
  }

  @Test
  public void testUnexistingMask() throws Exception {
    doTest("abrakadabra*.xml", 0, "No reports found");
  }

  @Test
  public void testUnexistingAbsoluteMask() throws Exception {
    doTest("##C_D##/abrakadabra*.xml", 0, "No reports found");
  }

  @Test
  public void testSingleFile() throws Exception {
    doTest("report.xml", 1, "1 report found for paths");
  }

  @Test
  public void testSingleMaskFile() throws Exception {
    doTest("rep*.xml", 1, "1 report found for paths");
  }

  @Test
  public void testSingleAbsoluteFile() throws Exception {
    doTest("##C_D##/report.xml", 1, "1 report found for paths");
  }

  @Test
  public void testSingleAbsoluteMaskFile() throws Exception {
    doTest("##C_D##/rep*.xml", 1, "1 report found for paths");
  }

  @Test
  public void testSingleRule() throws Exception {
    doTest("+:report.xml", 1, "1 report found for paths");
  }

  @Test
  public void testSingleMaskRule() throws Exception {
    doTest("+:rep*.xml", 1, "1 report found for paths");
  }

  @Test
  public void testSingleAbsoluteRule() throws Exception {
    doTest("+:##C_D##/report.xml", 1, "1 report found for paths");
  }

  @Test
  public void testSingleAbsoluteMaskRule() throws Exception {
    doTest("+:##C_D##/rep*.xml", 1, "1 report found for paths");
  }

  @Test
  public void testTwoFiles() throws Exception {
    doTest("report.xml\nresult.xml", 2, "2 reports found for paths");
  }

  @Test
  public void testTwoMaskFiles() throws Exception {
    doTest("rep*.xml\nres*.xml", 2, "2 reports found for paths");
  }

  @Test
  public void testTwoAbsoluteFiles() throws Exception {
    doTest("##C_D##/report.xml\n##C_D##/result.xml", 2, "2 reports found for paths");
  }

  @Test
  public void testTwoAbsoluteMaskFiles() throws Exception {
    doTest("##C_D##/rep*.xml\n##C_D##/res*.xml", 2, "2 reports found for paths");
  }

  @Test
  public void testTwoRules() throws Exception {
    doTest("+:report.xml\n+:result.xml", 2, "2 reports found for paths");
  }

  @Test
  public void testTwoMaskRules() throws Exception {
    doTest("+:rep*.xml\n+:res*.xml", 2, "2 reports found for paths");
  }

  @Test
  public void testTwoAbsoluteRules() throws Exception {
    doTest("+:##C_D##/report.xml\n+:##C_D##/result.xml", 2, "2 reports found for paths");
  }

  @Test
  public void testTwoAbsoluteMaskRules() throws Exception {
    doTest("+:##C_D##/rep*.xml\n+:##C_D##/res*.xml", 2, "2 reports found for paths");
  }

  @Test
  public void testTwoDifferentRules() throws Exception {
    doTest("+:report.xml\n-:result.xml", 1, "1 report found for paths");
  }

  @Test
  public void testTwoDifferentMaskRules() throws Exception {
    doTest("+:rep*.xml\n-:res*.xml", 1, "1 report found for paths");
  }

  @Test
  public void testTwoDifferentAbsoluteRules() throws Exception {
    doTest("+:##C_D##/report.xml\n-:##C_D##/result.xml", 1, "1 report found for paths");
  }

  @Test
  public void testTwoDifferentAbsoluteMaskRules() throws Exception {
    doTest("+:##C_D##/rep*.xml\n-:##C_D##/res*.xml", 1, "1 report found for paths");
  }

  private void doTest(@NotNull String reportDirs, int numberOfTests, String... messages) throws Exception {

    final BuildTypeEx bt = createBuildType(RUN_TYPE);
    bt.setCheckoutDirectory(myCheckoutDir.getAbsolutePath());

    final Map<String, String> fps = new HashMap<String, String>();
    fps.put(XmlReportPluginConstants.REPORT_TYPE, "junit");
    fps.put(XmlReportPluginConstants.REPORT_DIRS, reportDirs.replace("##C_D##", myCheckoutDir.getAbsolutePath()));

    bt.addBuildFeature(XmlReportPluginBuildFeature.FEATURE_TYPE, fps);

    final SRunningBuild sb = startBuild(bt, true);
    final SFinishedBuild fb = finishBuild(sb);

    assertEquals(numberOfTests, fb.getShortStatistics().getAllTestCount());

    final String buildLog = getBuildLog(fb);
    for (String m : messages) {
      assertTrue(buildLog.contains(m));
    }
  }
}
