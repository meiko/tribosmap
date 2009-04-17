package org.tribosmap.model.test.offline.business

import org.scalatest.FunSuite
import org.tribosmap.model.business.domain._
import java.util.Date
import java.io.File
import org.tribosmap.model.math.geographic._
import org.tribosmap.model.math.distance._
import org.tribosmap.model.math._
import java.lang.AssertionError

class GeoMapServiceTest extends FunSuite with OfflineTestServiceAccess {

  val testFile = new File("./testFiles/hardangerviddaWest.xml")
  val testImageName = "/home/meiko/tribosmap/workspace/TribOSMapTest/./testFiles/./hardangerviddaWest.tosm"
  val time = (new java.util.Date()).getTime
  val pointSize = 9

  val testMap = GeoMap(-1l, 
                           "Hardangerviddawest.jpg", 
                           testImageName, 
                           Vector2i(10816, 14682), 
                           Datum.WGS84Datum,  
                           "(UTM) Universal Transverse Mercator", 
                           time)
  
  val testMapSaved = GeoMap(111l, 
                           testMap.getName, 
                           testImageName, 
                           testMap.imageSize,
                           testMap.datum, 
                           testMap.projection, 
                           time)
  
  def testPoint( mapId: Long, x: Int, y: Int, 
                 northing: Double, easting: Double, utmZone: Int, hemisphere: Hemisphere.Value) = {
    
    GeoReferencedPixel(-1, mapId, Vector2i(x, y), 
                       UtmCoOrdinates(northing, easting, utmZone, hemisphere, Datum.WGS84Datum))
  }
  
  val testPoints = List(
        testPoint(111, 10264, 601, 6718000, 428000, 32, Hemisphere.North),
        testPoint(111, 10151, 14181, 6632000, 424000, 32, Hemisphere.North),
        testPoint(111, 426, 14248, 6634000, 362000, 32, Hemisphere.North),
        testPoint(111, 317, 3205, 6704000, 364000, 32, Hemisphere.North),
        testPoint(111, 3669, 546, 6720000, 386000, 32, Hemisphere.North),
        testPoint(111, 3839, 4959, 6692000, 386000, 32, Hemisphere.North),
        testPoint(111, 5075, 12802, 6642000, 392000, 32, Hemisphere.North),
        testPoint(111, 8668, 8237, 6670000, 416000, 32, Hemisphere.North),
        testPoint(111, 1780, 8512, 6670000, 372000, 32, Hemisphere.North))
        
  test("delete map") {

    expecting {
      one(daoAccessor.geoMapDAO).delete(testMap) willReturn true
      one(daoAccessor.geoMapDAO).delete(testMap) willReturn false
    }
    
    assert(testMap.delete() === true)
 
    intercept(classOf[AssertionError]) {
      testMap.delete()
    }
  }
  
  test("save map") {
    
    expecting {
      one(daoAccessor.geoMapDAO).create(testMap) willReturn testMap
      one(daoAccessor.geoMapDAO).update(testMapSaved) willReturn true
      one(daoAccessor.geoMapDAO).update(testMapSaved) willReturn false
    }
  
    assert(testMap.save() === true )
    assert(testMapSaved.save() === true )
    intercept(classOf[AssertionError]) {
      testMapSaved.save()
    }

  }
  
  test("import map") {
    
    expecting {
      one(daoAccessor.geoMapDAO).create(testMap) willReturn testMapSaved
      testPoints.foreach( testPoint ⇒ {
        one(daoAccessor.geoReferencedPixelDAO).create(testPoint) willReturn testPoint           
      })
    }
    
    val file = new File("./testFiles/hardangerviddaWest.xml")
    val result = mapService.importMapFromXml(file, time)
    assert(result === testMapSaved)
  } 
   
}
