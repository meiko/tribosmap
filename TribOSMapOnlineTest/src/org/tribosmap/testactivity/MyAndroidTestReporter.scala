package org.tribosmap.testactivity
import android.test.AndroidTestRunner
import junit.framework._
import org.scalatest.{Reporter, Report}


private[testactivity] class MyAndroidTestRunner(activity: TribOSMapTest) extends Reporter {
  
  private[this] var testsCompletedCount = 0
  private[this] var testsFailedCount = 0
  
  override def testStarting(report: Report) {
    activity.makeReport(TestEvent.START_TEST, report, 
                        "testStarting", testsCompletedCount, testsFailedCount)
  }
  
  override def testSucceeded(report: Report) {
    testsCompletedCount += 1
    activity.makeReport(TestEvent.END_TEST, report, 
                        "testSucceeded", testsCompletedCount, testsFailedCount)
  }

  override def testFailed(report: Report) {
    testsCompletedCount += 1
    testsFailedCount += 1
    activity.makeReport(TestEvent.FAILURE, report, 
                        "testFailed", testsCompletedCount, testsFailedCount)
  }
}
