package org.tribosmap.model.test.offline

import org.scalacheck.Test
import org.scalacheck.util.StdRand

private[offline] trait SettingsOffline {
  
  protected val testRuns = 1000
  protected val workers = 1
  protected val wrkSize = 1
  
  val testParams = Test.Params(
    testRuns, testRuns*5, 0, testRuns, StdRand,workers, wrkSize)
}

