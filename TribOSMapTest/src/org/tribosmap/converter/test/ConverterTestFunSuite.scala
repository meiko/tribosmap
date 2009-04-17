package org.tribosmap.converter.test


import org.scalatest.FunSuite
import java.io.File
import scala.io.Source


import org.tribosmap.converter.{OziMap, Converter, ConvertOzi}
import org.tribosmap.model.math.geographic._
import org.tribosmap.model.math.distance._
import org.tribosmap.model.math._
import org.tribosmap.model.business.domain._


/**
 * Test the converter for the map image files.
 * (only an offline test, because the converter is not an android application)
 * @author Meiko Rachimow
 */
class ConverterTestFunSuite extends FunSuite {

  /**
   * the input
   */
  private val testFile = new File("./testFiles/hardangerviddaWest.map")
  
  /**
   * the expected output
   */
  private val testFileOutputReference = new File("./testFiles/hardangerviddaWest.xml")
  
  /**
   * the real output of the test
   */
  private val testFileOutput = new File("./testFiles/tmp/hardangerviddaWest.xml")
  
  /**
   * creates some testPoints
   */
  private def testPoint( x: Int, y: Int, 
                         northing: Double, easting: Double, 
                         utmZone: Int, hemisphere: Hemisphere.Value) = {
    new {
      override def toString() = 
        "Pixel: " + Vector2i(x, y) + 
        ", LatLon: " + None + ", Utm: " + 
        Some(UtmCoOrdinates(northing, easting, utmZone, hemisphere, Datum.WGS84Datum))
    }
  }
  
  /**
   * the testPoints (should be the same, as in the input test file ("val testFile"))
   */
  private val testPoints = List(
        testPoint(10264, 601, 6718000, 428000, 32, Hemisphere.North),
        testPoint(10151, 14181, 6632000, 424000, 32, Hemisphere.North),
        testPoint(426, 14248, 6634000, 362000, 32, Hemisphere.North),
        testPoint(317, 3205, 6704000, 364000, 32, Hemisphere.North),
        testPoint(3669, 546, 6720000, 386000, 32, Hemisphere.North),
        testPoint(3839, 4959, 6692000, 386000, 32, Hemisphere.North),
        testPoint(5075, 12802, 6642000, 392000, 32, Hemisphere.North),
        testPoint(8668, 8237, 6670000, 416000, 32, Hemisphere.North),
        testPoint(1780, 8512, 6670000, 372000, 32, Hemisphere.North))
  
  
  test("read the ozi-Map file") {
    val map = new OziMap(testFile)
    println(map.toXml)
    assert(map.imageFile.toString === "./hardangerviddaWest.tosm")
    assert(map.name === "Hardangerviddawest.jpg")
    assert(map.datum === "WGS 84")
    assert(map.coordinateSystem === "WGS 84")
    assert(map.projection === "(UTM) Universal Transverse Mercator")
    assert(map.referencePoints.length === 9)
    assert(map.size === Vector2i(10816, 14682)) 
    
    assert(testPoints.sort(_.toString < _.toString).toString === 
           map.referencePoints.sort(_.toString < _.toString).toString)
  }
  
  test("write the ozi map to our tribosmap map-xml file") {
    val map = new OziMap(testFile)
    ConvertOzi.writeXmlToFile(testFileOutput, map.toXml)
    val reference = Source.fromFile(testFileOutputReference).getLines.toList
    val result = Source.fromFile(testFileOutput).getLines.toList
    val toCompare = reference.zip(result)
    assert(toCompare.length === result.length)
    assert(toCompare.length === reference.length)
    testFileOutput.delete
  }
}
