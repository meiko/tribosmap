/*
 * Copyright 2001-2008 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scalatest

import java.util.ResourceBundle
import java.text.MessageFormat

/**
 * Resources for internationalization.
 *
 * @author Bill Venners
 */
private[scalatest] object Resources {

  val ressourceStrings = Map(
"AppVersion"->"Version 0.9.3-2.7.2.RC2",
"exceptionExpected"->"Expected exception: {0} to be thrown, but no exception was thrown.",
"didNotEqual"->"{0} did not equal {1}",
"wrongException"->"Expected exception: {0} to be thrown, but {1} was thrown.",
"expectedButGot"->"Expected {0}, but got {1}",
"conditionFalse"->"A boolean condition should have been true.",
"refNotNull"->"A reference should have been null.",
"refNull"->"A reference should have been non-null.",
"floatInfinite"->"A float value was infinite. Expected: {0} Actual: {1}. Delta: {2}.",
"floatNaN"->"A float value was NaN. Expected: {0} Actual: {1}. Delta: {2}.",
"doubleInfinite"->"A double value was infinite. Expected: {0} Actual: {1}. Delta: {2}.",
"doubleNaN"->"A double value was NaN. Expected: {0} Actual: {1}. Delta: {2}.",
"testEvent"->"Test Event: {0}: {1}",

"testFailed"->"TEST FAILED - {0}: {1}",
"testStarting"->"Test Starting - {0}: {1}",
"testSucceeded"->"Test Succeeded - {0}: {1}",
"testIgnored"->"Test Ignored - {0}: {1}",
"suiteStarting"->"Suite Starting - {0}: {1}",
"suiteCompleted"->"Suite Completed - {0}: {1}",
"suiteAborted"->"SUITE ABORTED - {0}: {1}",
"runAborted"->"*** RUN ABORTED - {0}: {1} ***",
"infoProvided"->"Info Provided - {0}: {1}",

"testFailedNoMessage"->"TEST FAILED - {0}",
"testStartingNoMessage"->"Test Starting - {0}",
"testSucceededNoMessage"->"Test Succeeded - {0}",
"testIgnoredNoMessage"->"Test Ignored - {0}",
"suiteStartingNoMessage"->"Suite Starting - {0}",
"suiteCompletedNoMessage"->"Suite Completed - {0}",
"suiteAbortedNoMessage"->"SUITE ABORTED - {0}",
"runAbortedNoMessage"->"*** RUN ABORTED - {0} ***",
"infoProvidedNoMessage"->"Info Provided - {0}",

"runStarting"->"Run starting. Expected test count is: {0}",
"rerunStarting"->"Rerun starting. Expected test count is: {0}",
"runCompleted"->"Run completed. Total number of tests run was: {0}",
"runStopped"->"Run stopped. Total number of tests run was: {0}",
"rerunCompleted"->"Rerun completed. Total number of tests run was: {0}",
"rerunStopped"->"Rerun stopped. Total number of tests run was: {0}",
"friendlyFailure"->"Invalid option given to Runner.\njava org.suiterunner.Runner [option1 [option2..]] [suite1 [suite2...]]\n    Valid options are:\n    -g  display graphical user interface\n    -o  print results to standard output\n    -e  print results to standard error\n    -f <filename>  print results to file\n    -r <reporter class name>  pass test events to reporter",
"showStackTraceOption"->"Show Stack Traces",
"suitebeforeclass"->"Suite class names must appear after reporters.",
"reportTestsStarting"->"Report Tests Starting",
"reportTestsSucceeded"->"Report Test Success",
"reportTestsFailed"->"Report Test Failed",
"reportAlerts"->"Report Alerts",
"reportInfo"->"Report Miscellaneous Information Messages",
"reportStackTraces"->"Include Stack Traces in Reports",
"reportRunStarting"->"Report Run Starting",
"reportRunCompleted"->"Report Run Completed",
"reportSummary"->"Show A Summary of Results",
"probarg"->"Problem arg: {0}",
"errBuildingDispatchReporter"->"Error preparing reporters.",
"missingFileName"->"A -f option must be followed by an output file name.",
"missingReporterClassName"->"A -r option must be followed by a Reporter class name.",
"errParsingArgs"->"Error parsing command line arguments.",
"invalidConfigOption"->"Invalid configuration option: {0}",
"cantOpenFile"->"Unable to create a PrintReporter that prints reports to a file.",
"reporterThrew"->"Reporter method {0} completed abruptly with an exception.",
"suiteExecutionStarting"->"The execute method of a nested suite is about to be invoked.",
"executeException"->"Exception encountered when invoking execute on a nested suite.",
"suiteCompletedNormally"->"The execute method of a nested suite returned normally.",
"Rerun"->"Rerun",
"executeStopping"->"The execute method of a Suite is returning because a stop was requested.",
"illegalReporterArg"->"An illegal reporter argument was specified on the command line: \"{0}\".",
"cantLoadReporterClass"->"Couldn''t load a Reporter class: \"{0}\".",
"cantInstantiateReporter"->"Couldn''t instantiate a Reporter class: \"{0}\". Is the class public with a public no-arg constructor?",
"overwriteExistingFile"->"The file \"{0}\" already exists in this directory. Replace it?",
"cannotLoadSuite"->"Unable to load a Suite class. This could be due to an error in your runpath.",
"cannotLoadDiscoveredSuite"->"Unable to load a Suite class that was discovered in the runpath: {0} ",
"nonSuite"->"One or more requested classes are not Suites:",
"cannotInstantiateSuite"->"Unable to instantiate a Suite class. Is each Suite class you specified public, with a public no-arg constuctor?",
"cannotLoadClass"->"A needed class was not found. This could be due to an error in your runpath.",
"bigProblems"->"An exception or error caused a run to abort.",
"bigProblemsMaybeCustomReporter"->"An exception or error caused a run to abort. This may have been caused by a problematic custom reporter.",
"cannotFindMethod"->"The Suite to rerun does not contain the method to rerun.",
"securityWhenReruning"->"A SecurityException was thrown when attempting a rerun.",
"overwriteDialogTitle"->"Save",
"openPrefs"->"Open Recipe",
"savePrefs"->"Save Recipe",
"runsFailures"->"Runs and Failures",
"allReports"->"All Reports",
"needFileNameTitle"->"Edit Reporter Configuration",
"needFileNameMessage"->"A file name is required to create a File Reporter. Please supply a valid file name.",
"needClassNameTitle"->"Edit Reporter Configuration",
"needClassNameMessage"->"A Reporter class name is required to create a Custom Reporter. Please supply a fully qualified name of a class that implements org.suiterunner.Reporter.",
"NoSuitesFoundText"->"No Suites found in the runpath",
"cantInvokeExceptionText"->"Can't invoke method",
"multipleTestsFailed"->"*** {0} TESTS FAILED ***",
"oneTestFailed"->"*** 1 TEST FAILED ***",
"oneSuiteAborted"->"*** 1 SUITE ABORTED ***",
"multipleSuitesAborted"->"*** {0} SUITES ABORTED ***",
"allTestsPassed"->"All tests passed.",

"reportsLabel"->"Reports:",
"detailsLabel"->"Details:",
"testsRun"->"Tests Run:",
"testsFailed"->"Failed:",
"testsExpected"->"Expected:",
"testsIgnored"->"Ignored:",

"ScalaTestTitle"->"ScalaTest",
"ScalaTestMenu"->"ScalaTest",
"Run"->"Run",
"Stop"->"Stop",
"Exit"->"Exit",
"About"->"About...",
"AboutBoxTitle"->"About ScalaTest",

"AppName"->"ScalaTest",
"AppCopyright"->"Copyright (C) 2001-2008 Artima, Inc. All rights reserved.",
"AppURL"->"http://www.artima.com/scalatest/",
"Reason"->"A tool for testing Scala and Java software",
"Trademarks"->"ScalaTest is a trademark of Artima, Inc.",
"ArtimaInc"->"Artima, Inc.",
"MoreInfo"->"For more information, visit:",

"ViewMenu"->"View",

"JavaSuiteRunnerFile"->"srj",
"JavaSuiteRunnerFileDescription"->"Recipe Files (*.srj)",

"defaultConfiguration"->"default",

"reporterTypeLabel"->"Reporter Type:",
"graphicReporterType"->"Graphic Reporter",
"customReporterType"->"Custom Reporter",
"stdoutReporterType"->"Standard Output Reporter",
"stderrReporterType"->"Standard Error Reporter",
"fileReporterType"->"File Reporter",
"reporterConfigLabel"->"Reporter Configuration: {0}",
"unusedField"->"Field for Custom and File Reporters:",

"couldntRun"->"Couldn't Run",
"couldntRerun"->"Couldn't Rerun",

"MENU_REPORT_RUN_STARTING"->"Run Starting Reports",
"MENU_REPORT_TEST_STARTING"->"Test Starting Reports",
"MENU_REPORT_TEST_FAILED"->"Test Failed Reports",
"MENU_REPORT_TEST_SUCCEEDED"->"Test Succeeded Reports",
"MENU_REPORT_TEST_IGNORED"->"Test Ignored Reports",
"MENU_REPORT_SUITE_STARTING"->"Suite Starting reports",
"MENU_REPORT_SUITE_ABORTED"->"Suite Aborted Reports",
"MENU_REPORT_SUITE_COMPLETED"->"Suite Completed Reports",
"MENU_REPORT_INFO_PROVIDED"->"Information Provided Reports",
"MENU_REPORT_RUN_STOPPED"->"Run Stopped Reports",
"MENU_REPORT_RUN_ABORTED"->"Run Aborted Reports",
"MENU_REPORT_RUN_COMPLETED"->"Run Completed Reports",

"REPORT_RUN_STARTING"->"Run Starting",
"REPORT_TEST_STARTING"->"Test Starting",
"REPORT_TEST_FAILED"->"Test Failed",
"REPORT_TEST_SUCCEEDED"->"Test Succeeded",
"REPORT_TEST_IGNORED"->"Test Ignored",
"REPORT_SUITE_STARTING"->"Suite Starting",
"REPORT_SUITE_ABORTED"->"Suite Aborted",
"REPORT_SUITE_COMPLETED"->"Suite Completed",
"REPORT_INFO_PROVIDED"->"Info Provided",
"REPORT_RUN_STOPPED"->"Run Stopped",
"REPORT_RUN_ABORTED"->"Run Aborted",
"REPORT_RUN_COMPLETED"->"Run Completed",

"RERUN_REPORT_RUN_STARTING"->"Rerun Starting",
"RERUN_REPORT_TEST_STARTING"->"Rerun Test Starting",
"RERUN_REPORT_TEST_FAILED"->"Rerun Test Failed",
"RERUN_REPORT_TEST_SUCCEEDED"->"Rerun Test Succeeded",
"RERUN_REPORT_TEST_IGNORED"->"Rerun Test Ignored",
"RERUN_REPORT_SUITE_STARTING"->"Rerun Suite Starting",
"RERUN_REPORT_SUITE_ABORTED"->"Rerun Suite Aborted",
"RERUN_REPORT_SUITE_COMPLETED"->"Rerun Suite Completed",
"RERUN_REPORT_INFO_PROVIDED"->"Rerun Info Provided",
"RERUN_REPORT_RUN_STOPPED"->"Rerun Stopped",
"RERUN_REPORT_RUN_ABORTED"->"Rerun Aborted",
"RERUN_REPORT_RUN_COMPLETED"->"Rerun Completed",

"DetailsName"->"Name",
"DetailsMessage"->"Message",
"DetailsDate"->"Date",
"DetailsThread"->"Thread",
"DetailsThrowable"->"Throwable",

"should"->"should {0}",
"itShould"->"it should {0}",
"prefixSuffix"->"{0} {1}",
"prefixShouldSuffix"->"{0} should {1}",

"exampleSucceededIconChar"->"-",
"exampleFailedIconChar"->"-",
"exampleIconPlusShortName"->"{0} {1}",
"exampleIconPlusShortNameAndNote"->"{0} {1} {2}",
"failedNote"->"*** FAILED ***"
  )
  
  def apply(resourceName: String): String = ressourceStrings(resourceName)
		  

  //def apply(resourceName: String): String = ResourceBundle.getBundle("org.scalatest.ScalaTestBundle").getString(resourceName)

  private def makeString(resourceName: String, argArray: Array[Object]): String = {
    val raw = apply(resourceName)
    val msgFmt = new MessageFormat(raw)
    msgFmt.format(argArray)
  }

  // Later, figure out how to get varargs to work.
  def apply(resourceName: String, o1: Any): String = makeString(resourceName, Array[Object](o1.asInstanceOf[Object]))
  def apply(resourceName: String, o1: Any, o2: Any): String = makeString(resourceName, Array[Object](o1.asInstanceOf[Object], o2.asInstanceOf[Object]))
  def apply(resourceName: String, o1: Any, o2: Any, o3: Any): String = makeString(resourceName, Array[Object](o1.asInstanceOf[Object], o2.asInstanceOf[Object], o3.asInstanceOf[Object]))
}

