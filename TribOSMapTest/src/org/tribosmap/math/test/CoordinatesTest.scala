package org.tribosmap.math.test

import org.scalatest.prop.PropSuite
import org.scalacheck.Prop.{forAll, passed ⇒ OKAY}
import org.scalacheck.{Test, Gen}
import org.scalacheck.Prop._

import org.tribosmap.model.test.offline.SettingsOffline;
import org.tribosmap.model.math.geographic._

/**
 * contains tests for Coordinates (GeographicCoOrdinatePair and UtmCoOrdinates)
 * @author Meiko Rachimow
 */
trait CoordinatesTest extends PropSuite {

  val testParams: Test.Params
  
  test("CoordinatesTest: check GeographicCoOrdinatePair Constructor Requirements") { 
    //some testdata which mostly failes the requirtements
    val coords = for {
      lat ← Gen.choose(-1000.0, 1000.0)
      lon ← Gen.choose(-1000.0, 1000.0)
    } yield (lat, lon)
    
    //some testdata which mostly solves the requirtements
    val coords2 = for {
      lat ← Gen.choose(-100.0, 100.0)
      lon ← Gen.choose(-200.0, 200.0)
    } yield (lat, lon)
    
    //select more valid values (3:2)
    val allCoords = Gen.frequency((2, coords), (3,coords2))
    check(forAll(allCoords) { tuple ⇒ {
      val (lat, lon) = tuple
      //the requirements where the constructor will return
      if(lat > -91.0 && lat < 91.0 && lon > -181.0 && lon < 181.0){
        
        val coord = new GeographicCoOrdinatePair(lat, lon)
        
        //check correct return values from the getters
        assert(coord.latitude == lat && coord.longitude == lon)
      } else {
        //the requirements failed
        intercept(classOf[IllegalArgumentException]) {
          new GeographicCoOrdinatePair(lat, lon)
        }
      }
      OKAY
    }},testParams)
  }
  
  test("CoordinatesTest: check UtmCoOrdinates Constructor Requirements") {
    
    //some testdata which mostly failes the requirtements
    val coords = for {
      northing ← Gen.choose(-10000000.0, 1000000000.0)
      easting ← Gen.choose(-10000000.0, 1000000000.0)
      zoneNumber ← Gen.choose(-1000, 1000)
      hemisphere ← Gen.elements(Hemisphere.North, Hemisphere.South)
    } yield (northing, easting, zoneNumber, hemisphere)
    
    //some testdata which mostly solves the requirtements
    val coords2 = for {
      northing ← Gen.choose(0.0, 10000000.0)
      easting ← Gen.choose(100000.0, 900000.0)
      zoneNumber ← Gen.choose(0, 70)
      hemisphere ← Gen.elements(Hemisphere.North, Hemisphere.South)
    } yield (northing, easting, zoneNumber, hemisphere)
    
    //select more valid values (5:2)
    val allCoords = Gen.frequency((2, coords), (5,coords2))
    check(forAll(allCoords) { quadrupel ⇒ {
      val (northing, easting, zoneNumber, hemisphere) = quadrupel
      //the requirements where the constructor will return
      if(zoneNumber > 0 && zoneNumber < 61 && 
         easting >= 0 && easting <= 834000 &&
         ((hemisphere equals Hemisphere.North) || 
            (northing >= 1100000 && northing <= 10000000)) &&
         ((hemisphere equals Hemisphere.South) || 
            (northing >= 0 && northing <= 9333000))) {
        
        val coord = new UtmCoOrdinates(
          northing, easting, zoneNumber, hemisphere, Datum.WGS84Datum)

        //check correct return values from the getters
        assert(
          coord.northingD == northing && 
            coord.eastingD == easting && 
            coord.zoneNumber == zoneNumber &&
            coord.hemisphere == hemisphere)
      } else {

        //the requirements failed
        intercept(classOf[IllegalArgumentException]) {
          new UtmCoOrdinates(
            northing, easting, zoneNumber, hemisphere, Datum.WGS84Datum)
        }
      }
      OKAY
    }},testParams)
  }
  
  test("CoordinatesTest: check GeographicCoOrdinatePair similar") {

    //some testdata which mostly solves the requirements
    val coords = for {
      lat1 ← Gen.choose(40, 40.000006)
      lon1 ← Gen.choose(40, 40.000006)
      lat2 ← Gen.choose(40, 40.000006)
      lon2 ← Gen.choose(40, 40.000006)
    } yield (lat1, lon1, lat2, lon2)
    
    check(forAll(coords) { quadrupel ⇒ 
      val (lat1, lon1, lat2, lon2) = quadrupel
      val coord1 = new GeographicCoOrdinatePair(lat1, lon1)
      val coord2 = new GeographicCoOrdinatePair(lat2, lon2)
      val xd = (lat1 - lat2)
      val yd = (lon1 - lon2)
      (Math.sqrt(xd*xd + yd*yd) < 0.000006) ==> ( coord1 ≈ coord2 )
    },testParams)
  }
  
  test("CoordinatesTest: check UtmCoOrdinates similar") {

    //some testdata which mostly solves the requirements
    val coords = for {
      northing1 ← Gen.choose(1.1, 1.2)
      easting1 ← Gen.choose(100000.9, 100001.0)
      northing2 ← Gen.choose(1.1, 1.2)
      easting2 ← Gen.choose(100000.9, 100001.0)
    } yield (northing1, easting1, northing2, easting2)
    
    check(forAll(coords) { quadrupel ⇒ 
      val (northing1, easting1, northing2, easting2) = quadrupel
      val coord1 = new UtmCoOrdinates(northing1, easting1, 50, Hemisphere.North, Datum.WGS84Datum)
      val coord2 = new UtmCoOrdinates(northing2, easting2, 50, Hemisphere.North, Datum.WGS84Datum)
      val xd = (easting1 - easting2)
      val yd = (northing1 - northing2)
      (Math.sqrt(xd*xd + yd*yd) < 0.05) ==> ( coord1 ≈ coord2 )
    },testParams)
  }
}
