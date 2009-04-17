package org.tribosmap.model.test.offline

import org.scalatest.SuperSuite
import org.tribosmap.math.test.DistanceTest
import org.tribosmap.math.test.{CoordinatesTest, WGS84CoordConverterTest, PositionTest}
import org.tribosmap.model.test.offline.business._

class AllTests extends SuperSuite(
  List(
    new DistanceTest with SettingsOffline,
    new CoordinatesTest with SettingsOffline,
    new WGS84CoordConverterTest with SettingsOffline,
    new PositionTest with SettingsOffline,
    new GeoMapServiceTest,
    new MarkerServiceTest,
    new TraceServiceTest
  ))
