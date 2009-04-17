package org.tribosmap.model.test.online

import org.scalacheck.Test
import org.scalacheck.util.StdRand

private[online] trait SettingsOnline {
  
  protected val testRuns = 5
  protected val workers = 1
  protected val wrkSize = 1
  
  val testParams = Test.Params(
    testRuns, testRuns*5, 0, testRuns, StdRand,workers, wrkSize)
}

