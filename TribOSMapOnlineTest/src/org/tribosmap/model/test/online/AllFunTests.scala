package org.tribosmap.model.test.online

import org.scalatest.SuperSuite
import org.tribosmap.math.test.DistanceTest
import org.tribosmap.math.test.{CoordinatesTest, WGS84CoordConverterTest, PositionTest}
import org.tribosmap.model.test.online.business._

class AllFunTests extends SuperSuite(
  List(
    new DistanceTest with SettingsOnline,
    new CoordinatesTest with SettingsOnline,
    new WGS84CoordConverterTest with SettingsOnline,
    new PositionTest with SettingsOnline,
    new GeoMapServiceTest,
    new MarkerServiceTest,
    new TraceServiceTest
  )){
  
  override def expectedTestCount(incl: Set[String], excl: Set[String]) = {
    val testCountList = for (suite <- this.nestedSuites)
        yield suite.expectedTestCount(incl, excl)
  
    def sumInts(list: List[Int]): Int =
      list match {
        case Nil => 0
        case x :: xs => x + sumInts(xs)
      }

    sumInts(testCountList)
  }
}


