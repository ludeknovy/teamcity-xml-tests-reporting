/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package jetbrains.buildServer.testReportParserPlugin;

import jetbrains.buildServer.agent.BaseServerLoggerFacade;
import jetbrains.buildServer.testReportParserPlugin.antJUnit.AntJUnitReportParser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;


public class TestReportDirectoryWatcher implements Runnable {
    private static final int SCAN_INTERVAL = 50;

    private final LinkedBlockingQueue<File> myReportQueue;
    private final Map<File, Long> myDirectories;
    private final List<String> myProcessedFiles;
    private final BaseServerLoggerFacade myLogger;
    private final long myBuildStartTime;

    private volatile boolean myStopWatching;
    private volatile boolean myStopped;


    public TestReportDirectoryWatcher(@NotNull final List<File> directories, @NotNull LinkedBlockingQueue<File> queue, BaseServerLoggerFacade logger, long buildStartTime) {
        myDirectories = new HashMap<File, Long>();
        for (File f : directories) {
            myDirectories.put(f, buildStartTime);
        }
        myReportQueue = queue;
        myStopWatching = false;
        myStopped = false;
        myProcessedFiles = new ArrayList<String>();
        myLogger = logger;
        myBuildStartTime = buildStartTime;
    }

    public void run() {
        while (!myStopWatching) {
            try {
                scanDirectories();
                Thread.sleep(SCAN_INTERVAL);
            } catch (InterruptedException e) {
                myLogger.warning("TestReportDirectoryWatcher thread interrupted");
            }
        }
        scanDirectories();
        synchronized (this) {
            myStopped = true;
            this.notify();
        }
    }

    private void scanDirectories() {
        for (File dir : myDirectories.keySet()) {
            if (dir.isDirectory()) {

                File[] files = dir.listFiles();
                for (int i = 0; i < files.length; ++i) {
                    File report = files[i];

                    if (report.isFile() && (report.lastModified() > myBuildStartTime)) {
                        if (!myProcessedFiles.contains(report.getPath())) {
                            if (report.canRead() && AntJUnitReportParser.isReportFileComplete(report)) {
                                TestReportParserPlugin.log("FILE: " + report.getName());
                                myProcessedFiles.add(report.getPath());
                                try {
                                    myReportQueue.put(report);
                                } catch (InterruptedException e) {
                                    myLogger.warning("TestReportDirectoryWatcher thread interrupted");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void stopWatching() {
        myStopWatching = true;
    }

    public boolean isStopped() {
        return myStopped;
    }
}