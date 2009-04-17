package org.tribosmap.model.test.online.business

import org.scalatest.FunSuite
import org.tribosmap.model.business.domain._
import java.util.Date
import java.io.File
import org.tribosmap.model.math.geographic._
import org.tribosmap.model.math.distance._
import org.tribosmap.model.math._
import java.lang.AssertionError
import android.test.AndroidTestCase

class GeoMapServiceTest extends AndroidTestCase with FunSuite with OnlineTestServiceAccess {

  val context = getContext
  
  test("my import, update and delete map") {
    
    val rootDir = "/sdcard/tribosmap/"
    val testFile = new File(rootDir + "hardangerviddaWest.xml")
    val testImageName = rootDir + "./hardangerviddaWest.tosm"
    val time = (new java.util.Date()).getTime
    val pointSize = 9
  
    def testMapSaved(id :Long) = GeoMap(id, 
                                        "Hardangerviddawest.jpg", 
                                        testImageName, 
                                        Vector2i(10816, 14682), 
                                        Datum.WGS84Datum, 
                                        "(UTM) Universal Transverse Mercator", 
                                        time)
  
    def testPoint( id: Long, mapId: Long, x: Int, y: Int, 
                   northing: Double, easting: Double, utmZone: Int, hemisphere: Hemisphere.Value) = {
    
      GeoReferencedPixel(id, mapId, Vector2i(x, y), 
                         UtmCoOrdinates(northing, easting, utmZone, hemisphere, Datum.WGS84Datum))
    }
  
    def testPoints(mapId: Long) = List(
        testPoint(1, mapId, 10264, 601, 6718000, 428000, 32, Hemisphere.North),
        testPoint(2, mapId, 10151, 14181, 6632000, 424000, 32, Hemisphere.North),
        testPoint(3, mapId, 426, 14248, 6634000, 362000, 32, Hemisphere.North),
        testPoint(4, mapId, 317, 3205, 6704000, 364000, 32, Hemisphere.North),
        testPoint(5, mapId, 3669, 546, 6720000, 386000, 32, Hemisphere.North),
        testPoint(6, mapId, 3839, 4959, 6692000, 386000, 32, Hemisphere.North),
        testPoint(7, mapId, 5075, 12802, 6642000, 392000, 32, Hemisphere.North),
        testPoint(8, mapId, 8668, 8237, 6670000, 416000, 32, Hemisphere.North),
        testPoint(9, mapId, 1780, 8512, 6670000, 372000, 32, Hemisphere.North))

    val resultMap = mapService.importMapFromXml(testFile, time)
    
    assert(resultMap === testMapSaved(resultMap.id))
    
    val refPixels = mapService.getAllReferencedPixelsForMap(resultMap)
    
    val mapTestReferencePoints = testPoints(resultMap.id)
    
    assert(refPixels.size === mapTestReferencePoints.size)
    
    for(i <- 1 until 9) {
      assert(refPixels(i) === mapTestReferencePoints(i))
    }

    intercept(classOf[IllegalArgumentException]) {
      resultMap.rename("")
    }
    
    resultMap.rename("Neuer Name")
    assert(resultMap.save() === true )

    resultMap.rename("Irgendwas")
    
    assert(resultMap.reload.getName === "Neuer Name")

    assert(resultMap.delete() === true)
 
    intercept(classOf[AssertionError]) {
      resultMap.delete()
    }
    
    assert(daoAccessor.geoMapDAO.fetchAll.size === 0)
    assert(daoAccessor.geoReferencedPixelDAO.fetchAll().size === 0)
    
  }   
}
