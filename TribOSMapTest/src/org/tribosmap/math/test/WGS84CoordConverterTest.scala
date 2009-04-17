package org.tribosmap.math.test

import org.scalatest.prop.PropSuite
import org.scalatest.Group
import org.scalacheck.Prop.{forAll, passed ⇒ OKAY}
import org.scalacheck.{Test, Gen}
import org.scalacheck.util.StdRand

import org.tribosmap.model.math.geographic._

/**
 * contains tests for the Coordinate Converter (only WGS84)
 * @author Meiko Rachimow
 */
trait WGS84CoordConverterTest extends PropSuite {

  
  val converter = new CoordConverter(Datum.WGS84Datum)
  
  val testParams: Test.Params
  
  private case class TestTuple(geo: GeographicCoOrdinatePair, utm: UtmCoOrdinates) 

  private val testData = List[TestTuple](
    TestTuple(GeographicCoOrdinatePair( 0, 0), 
          UtmCoOrdinates( 0.000,  166021.443, 31, Hemisphere.North, Datum.WGS84Datum)),
    TestTuple(GeographicCoOrdinatePair( 0, -180), 
          UtmCoOrdinates( 0.000,  166021.443,  1, Hemisphere.North, Datum.WGS84Datum)),
    TestTuple(GeographicCoOrdinatePair( 0, 90), 
          UtmCoOrdinates( 0.000,  166021.443, 46, Hemisphere.North, Datum.WGS84Datum)),
    TestTuple(GeographicCoOrdinatePair( 84, 0), 
          UtmCoOrdinates( 9329005.183, 465005.345, 31, Hemisphere.North, Datum.WGS84Datum)),
    TestTuple(GeographicCoOrdinatePair( -80, 0), 
          UtmCoOrdinates( 1116915.044,  441867.785, 31, Hemisphere.South, Datum.WGS84Datum)),
    TestTuple(GeographicCoOrdinatePair( 0, -3), 
          UtmCoOrdinates( 0.000,  500000.000, 30, Hemisphere.North, Datum.WGS84Datum)),
    TestTuple(GeographicCoOrdinatePair( 0, 3), 
          UtmCoOrdinates( 0.000,  500000.000, 31, Hemisphere.North, Datum.WGS84Datum))
  )
  
  test("WGS84CoordConverterTest: check Conversion fixed data, latLonToUtm") {
    testData.foreach( testEntry ⇒ {
      val convertedUtm = converter.latLonToUtm(testEntry.geo)
      assert(testEntry.utm ≈ convertedUtm, 
             "converted: " + convertedUtm + 
             " not similar to original: " + testEntry.utm)
    })
  }
  
 

   test("WGS84CoordConverterTest: check Conversion fixed data, utmToLatLon"){
    for(testEntry <- testData) {
      val convertedLatLon = converter.utmToLatLon(testEntry.utm)
      assert(testEntry.geo ≈ convertedLatLon, 
             "converted: " + convertedLatLon + 
             " not similar to original: " + testEntry.geo)
    }
  }

  test("WGS84CoordConverterTest: check Conversion, latLonToUtm ⇒ utmToLatLon ⇒ latLonToUtm") {
    //testdata: correct values for Constructor of GeographicCoOrdinatePair
    val coords = for {
      lat ← Gen.choose(-90.0, 90.0)
      lon ← Gen.choose(-180.0, 180.0)
    } yield new GeographicCoOrdinatePair(lat, lon)

    check(forAll(coords) { geoCoord ⇒ {
      
      //the requirements where the function latLonToUtm is defined
      if(geoCoord.latitude >= -80 && geoCoord.latitude <= 84 && 
         geoCoord.longitude >= -360 && geoCoord.longitude <= 360){
        val convertedUtm = converter.latLonToUtm(geoCoord)
        val convertedBackGeo = converter.utmToLatLon(convertedUtm)
        assert(geoCoord ≈ convertedBackGeo, 
             "converted: " + convertedBackGeo + 
             " not similar to original: " + geoCoord)
      } else {
        //the requirements failed
        intercept(classOf[IllegalArgumentException]) {
          converter.latLonToUtm(geoCoord)
        }
      }
      OKAY
    }},testParams)
  }
  
  test("WGS84CoordConverterTest: check Conversion, UtmCoOrdinates: utmToLatLon ⇒ latLonToUtm ⇒ utmToLatLon") {
    //there is no rule to generate correct utmcoordinates at the moment (TODO:)
    val coords = for {
      lat ← Gen.choose(-80.0, 84.0)
      lon ← Gen.choose(-180.0, 180.0)
    } yield converter.latLonToUtm(
              new GeographicCoOrdinatePair(lat, lon))

    check(forAll(coords) { coordUtm ⇒ {
      val convertedLatLon = converter.utmToLatLon(coordUtm)
      val convertedBackUtm = converter.latLonToUtm(convertedLatLon)
      assert(coordUtm ≈ convertedBackUtm, 
             "converted: " + convertedBackUtm + 
               " not similar to original: " + coordUtm + 
             "geoCoord was: " + convertedLatLon)
      OKAY
    }},testParams)
  }


}

