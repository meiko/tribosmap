package org.tribosmap.math.test

import org.scalatest.prop.PropSuite
import org.scalacheck.Prop.{forAll, passed ⇒ OKAY, undecided ⇒ UNDECIDED}
import org.scalacheck.{Test, Gen}
import org.scalacheck.util.StdRand
import org.scalacheck.Prop._

import org.tribosmap.model.math.geographic._
import org.tribosmap.model.math.distance._
import org.tribosmap.model.math.distance.DistanceImplicits._

/**
 * contains tests for the Position class
 * @author Meiko Rachimow
 */
trait PositionTest extends PropSuite {
  
  val testParams: Test.Params
  
  test("PositionTest: check positions distanceTo method") {
  
    //some testdata which mostly failes the requirtements
    val coords = for {
      northing ← Gen.choose(0.0, 10000000.0)
      easting ← Gen.choose(0.0, 834000.0)
      zoneNumber ← Gen.choose(1, 60)
      hemisphere ← Gen.elements(Hemisphere.North, Hemisphere.South)
      altitude ← Gen.choose(-100000.0, 100000.0)
      northing2 ← Gen.choose(0.0, 10000000.0)
      easting2 ← Gen.choose(0.0, 834000.0)
      zoneNumber2 ← Gen.choose(1, 60)
      hemisphere2 ← Gen.elements(Hemisphere.North, Hemisphere.South)
      altitude2 ← Gen.choose(-100000.0, 100000.0)
    } yield (northing, easting, zoneNumber, hemisphere, altitude,
             northing2, easting2, zoneNumber2, hemisphere2, altitude2)
    

    check(forAll(coords) { data ⇒ {
      val ( northing, easting, zoneNumber, hemisphere, altitude,
            northing2, easting2, zoneNumber2, hemisphere2, altitude2 ) = data      
      
      try{
        val coord = new UtmCoOrdinates(
          northing, easting, zoneNumber, hemisphere, Datum.WGS84Datum)
      
        val coord2 = new UtmCoOrdinates(
          northing2, easting2, zoneNumber2, hemisphere2, Datum.WGS84Datum)
        
        val pos1 = new Position(coord, altitude)
        val pos2 = new Position(coord2, altitude2)
        
        val distance = pos1.distanceTo(pos2)

        val dx = (northing - northing2)
        val dy = (easting - easting2)
        val dz = (altitude - altitude2)

        val refMaxFalsing = 0.0000001
        assert(distance.differenceTo(Math.sqrt(dx*dx + dy*dy + dz*dz).m) < refMaxFalsing.m, 
               "distance should be smaller than: " + refMaxFalsing + " but is " + distance)
        OKAY
      } catch {
        case e: IllegalArgumentException ⇒ 
          UNDECIDED
      }

    }},testParams)
  }
}
