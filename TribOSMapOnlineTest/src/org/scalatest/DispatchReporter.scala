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

import scala.actors.Exit
import scala.actors.Actor
import scala.actors.Actor.actor
import scala.actors.Actor.loop
import scala.actors.Actor.receive
import org.scalatest.CatchReporter.handleReporterException
import java.io.PrintStream

/**
 * A <code>Reporter</code> that dispatches test results to other <code>Reporter</code>s.
 * Attempts to dispatch each method invocation to each contained <code>Reporter</code>,
 * even if some <code>Reporter</code> methods throw <code>Exception</code>s. Catches
 * <code>Exception</code>s thrown by <code>Reporter</code> methods and prints error
 * messages to the standard error stream.
 *
 * The primary constructor creates a new <code>DispatchReporter</code> with specified <code>Reporter</code>s list.
 * Each object in the <code>reporters</code> list must implement <code>Reporter</code>.
 *
 * @param reporters the initial <code>Reporter</code>s list for this
 * <code>DispatchReporter</code>
 * @throws NullPointerException if <code>reporters</code> is <code>null</code>.
 * @author Bill Venners
 */
private[scalatest] class DispatchReporter(val reporters: List[Reporter], out: PrintStream) extends Reporter {

  private val julia = actor {
    var alive = true // local variable, right? Only used by the Actor's thread, so no need for synchronization
    while (alive) {
      receive {
        case RunStarting(expectedTestCount) => dispatch("runStarting", (reporter: Reporter) => reporter.runStarting(expectedTestCount))
        case TestStarting(report) => dispatch("testStarting", (reporter: Reporter) => reporter.testStarting(report))
        case TestIgnored(report) => dispatch("testIgnored", (reporter: Reporter) => reporter.testIgnored(report))
        case TestSucceeded(report) => dispatch("testSucceeded", (reporter: Reporter) => reporter.testSucceeded(report))
        case TestFailed(report) => dispatch("testFailed", (reporter: Reporter) => reporter.testFailed(report))
        case SuiteStarting(report) => dispatch("suiteStarting", (reporter: Reporter) => reporter.suiteStarting(report))
        case SuiteCompleted(report) => dispatch("suiteCompleted", (reporter: Reporter) => reporter.suiteCompleted(report))
        case SuiteAborted(report) => dispatch("suiteAborted", (reporter: Reporter) => reporter.suiteAborted(report))
        case InfoProvided(report) => dispatch("infoProvided", (reporter: Reporter) => reporter.infoProvided(report))
        case RunStopped() => dispatch("runStopped", (reporter: Reporter) => reporter.runStopped())
        case RunAborted(report) => dispatch("runAborted", (reporter: Reporter) => reporter.runAborted(report))
        case RunCompleted() => dispatch("runCompleted", (reporter: Reporter) => reporter.runCompleted())
        case Dispose() => {
          dispatch("dispose", (reporter: Reporter) => reporter.dispose())
          alive = false
        }
      }
    }
  }

  def this(reporters: List[Reporter]) = this(reporters, System.out)
  def this(reporter: Reporter) = this(List(reporter), System.out)

  /* where do I put this Scaladoc?
   * Returns a <code>List</code> of the <code>Reporter</code>s contained in this
   * <code>DispatchReporter</code>.
   *
   * @return a <code>List</code> of the <code>Reporter</code>s contained in this
   * <code>DispatchReporter</code>.
   */

  /**
   * Invokes <code>runStarting</code> on each <code>Reporter</code> in this
   * <code>DispatchReporter</code>'s reporters list, passing the specified
   * <code>testCount</code>.
   *
   * <P>
   * This method attempts to invoke <code>runStarting</code> on each contained <code>Reporter</code>,
   * even if some <code>Reporter</code>'s <code>runStarting</code> methods throw
   * <code>Exception</code>s. This method catches any <code>Exception</code> thrown by
   * a <code>runStarting</code> method and handles it by printing an error message to the
   * standard error stream.
   *
   * @param testCount the number of tests expected during this run
   * @throws IllegalArgumentException if <code>testCount</code> is less than zero
   */
  override def runStarting(expectedTestCount: Int) = julia ! RunStarting(expectedTestCount)
     
  /**
   * Invokes <code>testSucceeded</code> on each <code>Reporter</code> in this
   * <code>DispatchReporter</code>'s reporters list, passing the specified
   * <code>report</code>.
   *
   * <P>
   * This method attempts to invoke <code>testSucceeded</code> on each contained <code>Reporter</code>,
   * even if some <code>Reporter</code>'s <code>testSucceeded</code> methods throw
   * <code>Exception</code>s. This method catches any <code>Exception</code> thrown by
   * a <code>testSucceeded</code> method and handles it by printing an error message to the
   * standard error stream.
   *
   * @param report the <code>Report</code> encapsulating this test succeeded event
   * @throws NullPointerException if <code>report</code> is <code>null</code>
   */
  override def testSucceeded(report: Report) = julia ! TestSucceeded(report)

  /**
   * Invokes <code>testIgnored</code> on each <code>Reporter</code> in this
   * <code>DispatchReporter</code>'s reporters list, passing the specified
   * <code>report</code>
   * 
   * <P>
   * This method attempts to invoke <code>testIgnored</code> on each contained <code>Reporter</code>,
   * even if some <code>Reporter</code>'s <code>testIgnored</code> methods throw
   * <code>Exception</code>s. This method catches any <code>Exception</code> thrown by
   * a <code>testIgnored</code> method and handles it by printing an error message to the
   * standard error stream.
   * 
   */
  override def testIgnored(report: Report) = julia ! TestIgnored(report) 

  /**
   * Invokes <code>testFailed</code> on each <code>Reporter</code> in this
   * <code>DispatchReporter</code>'s reporters list, passing the specified
   * <code>report</code>.
   *
   * <P>
   * This method attempts to invoke <code>testFailed</code> on each contained <code>Reporter</code>,
   * even if some <code>Reporter</code>'s <code>testFailed</code> methods throw
   * <code>Exception</code>s. This method catches any <code>Exception</code> thrown by
   * a <code>testFailed</code> method and handles it by printing an error message to the
   * standard error stream.
   *
   * @param report the <code>Report</code> encapsulating this test failed event
   * @throws NullPointerException if <code>report</code> is <code>null</code>
   */
  override def testFailed(report: Report) = julia ! TestFailed(report)

  /**
   * Invokes <code>infoProvided</code> on each <code>Reporter</code> in this
   * <code>DispatchReporter</code>'s reporters list, passing the specified
   * <code>report</code>.
   *
   * <P>
   * This method attempts to invoke <code>infoProvided</code> on each contained <code>Reporter</code>,
   * even if some <code>Reporter</code>'s <code>infoProvided</code> methods throw
   * <code>Exception</code>s. This method catches any <code>Exception</code> thrown by
   * a <code>infoProvided</code> method and handles it by printing an error message to the
   * standard error stream.
   *
   * @param report the <code>Report</code> encapsulating this info provided event
   * @throws NullPointerException if <code>report</code> is <code>null</code>
   */
  override def infoProvided(report: Report) = julia ! InfoProvided(report)

 /**
  * Invokes <code>testStarting</code> on each <code>Reporter</code> in this
  * <code>DispatchReporter</code>'s reporters list, passing the specified
  * <code>report</code>.
  *
  * <P>
  * This method attempts to invoke <code>testStarting</code> on each contained <code>Reporter</code>,
  * even if some <code>Reporter</code>'s <code>testStarting</code> methods throw
  * <code>Exception</code>s. This method catches any <code>Exception</code> thrown by
  * a <code>testStarting</code> method and handles it by printing an error message to the
  * standard error stream.
  *
  * @param report the <code>Report</code> encapsulating this test starting event
  * @throws NullPointerException if <code>report</code> is <code>null</code>
  */
  override def testStarting(report: Report) = julia ! TestStarting(report)
  
  /**
   * Invokes <code>suiteStarting</code> on each <code>Reporter</code> in this
   * <code>DispatchReporter</code>'s reporters list, passing the specified
   * <code>report</code>.
   *
   * <P>
   * This method attempts to invoke <code>suiteStarting</code> on each contained <code>Reporter</code>,
   * even if some <code>Reporter</code>'s <code>suiteStarting</code> methods throw
   * <code>Exception</code>s. This method catches any <code>Exception</code> thrown by
   * a <code>suiteStarting</code> method and handles it by printing an error message to the
   * standard error stream.
   *
   * @param report a <code>Report</code> that encapsulates the suite starting event to report.
   *
   * @throws NullPointerException if <code>report</code> reference is <code>null</code>
   */
  override def suiteStarting(report: Report) = julia ! SuiteStarting(report)

  /**
   * Invokes <code>suiteCompleted</code> on each <code>Reporter</code> in this
   * <code>DispatchReporter</code>'s reporters list, passing the specified
   * <code>report</code>.
   *
   * <P>
   * This method attempts to invoke <code>suiteCompleted</code> on each contained <code>Reporter</code>,
   * even if some <code>Reporter</code>'s <code>suiteCompleted</code> methods throw
   * <code>Exception</code>s. This method catches any <code>Exception</code> thrown by
   * a <code>suiteCompleted</code> method and handles it by printing an error message to the
   * standard error stream.
   *
   * @param report a <code>Report</code> that encapsulates the suite completed event to report.
   * @throws NullPointerException if <code>report</code> reference is <code>null</code>
   */
  override def suiteCompleted(report: Report) = julia ! SuiteCompleted(report)

  /**
   * Indicates the execution of a suite of tests has aborted prior to completion.
   * Invokes <code>suiteAborted</code> on each <code>Reporter</code> in this
   * <code>DispatchReporter</code>'s reporters list, passing the specified
   * <code>report</code>.
   *
   * <P>
   * This method attempts to invoke <code>suiteAborted</code> on each contained <code>Reporter</code>,
   * even if some <code>Reporter</code>'s <code>suiteAborted</code> methods throw
   * <code>Exception</code>s. This method catches any <code>Exception</code> thrown by
   * a <code>suiteAborted</code> method and handles it by printing an error message to the
   * standard error stream.
   *
   * @param report a <code>Report</code> that encapsulates the suite aborted event to report.
   * @throws NullPointerException if <code>report</code> reference is <code>null</code>
   */
  override def suiteAborted(report: Report) = julia ! SuiteAborted(report)

  /**
   * Indicates a runner has stopped running a suite of tests prior to completion.
   * Invokes <code>runStopped</code> on each <code>Reporter</code> in this
   * <code>DispatchReporter</code>'s reporters list.
   *
   * <P>
   * This method attempts to invoke <code>runStopped</code> on each contained <code>Reporter</code>,
   * even if some <code>Reporter</code>'s <code>runStopped</code> methods throw
   * <code>Exception</code>s. This method catches any <code>Exception</code> thrown by
   * a <code>runStopped</code> method and handles it by printing an error message to the
   * standard error stream.
   */
  override def runStopped() = julia ! RunStopped()

  /**
   * Indicates a run has aborted prior to completion.
   * Invokes <code>runAborted</code> on each <code>Reporter</code> in this
   * <code>DispatchReporter</code>'s reporters list, passing the specified
   * <code>report</code>.
   *
   * <P>
   * This method attempts to invoke <code>runAborted</code> on each contained <code>Reporter</code>,
   * even if some <code>Reporter</code>'s <code>runAborted</code> methods throw
   * <code>Exception</code>s. This method catches any <code>Exception</code> thrown by
   * a <code>runAborted</code> method and handles it by printing an error message to the
   * standard error stream.
   *
   * @param report a <code>Report</code> that encapsulates the suite aborted event to report.
   * @throws NullPointerException if <code>report</code> reference is <code>null</code>
   */
  override def runAborted(report: Report) = julia ! RunAborted(report)
     
  /**
   * Invokes <code>runCompleted</code> on each <code>Reporter</code> in this
   * <code>DispatchReporter</code>'s reporters list.
   *
   * <P>
   * This method attempts to invoke <code>runCompleted</code> on each contained <code>Reporter</code>,
   * even if some <code>Reporter</code>'s <code>runCompleted</code> methods throw
   * <code>Exception</code>s. This method catches any <code>Exception</code> thrown by
   * a <code>runCompleted</code> method and handles it by printing an error message to the
   * standard error stream.
   */
  override def runCompleted() = julia ! RunCompleted()

  /**
   * Invokes <code>dispose</code> on each <code>Reporter</code> in this
   * <code>DispatchReporter</code>'s reporters list.
   *
   * <P>
   * This method attempts to invoke <code>dispose</code> on each contained <code>Reporter</code>,
   * even if some <code>Reporter</code>'s <code>dispose</code> methods throw
   * <code>Exception</code>s. This method catches any <code>Exception</code> thrown by
   * a <code>dispose</code> method and handles it by printing an error message to the
   * standard error stream.
   */
  override def dispose() = julia ! Dispose()

  private def dispatch(methodName: String, methodCall: (Reporter) => Unit) {
 
    try {
      reporters.foreach(methodCall)
    }
    catch {
      case e: Exception => handleReporterException(e, methodName, out)
    }
  }
}

