package org.tribosmap.model.test.online.business


import org.scalatest.FunSuite
import org.tribosmap.model.business.domain.{Marker, MarkerType}
import java.util.Date
import org.tribosmap.model.math.geographic._
import java.lang.AssertionError
import android.test.AndroidTestCase

class MarkerServiceTest extends AndroidTestCase with FunSuite with OnlineTestServiceAccess {

  val context = getContext
  
  val marker = Marker(
    -1, 
    -1, 
    "testName",  
    "testInfo", 
    MarkerType.PointOfInterest, 
    (new Date()).getTime, 
    "/sdcard/aa.tst", 
    new Position(GeographicCoOrdinatePair(12,14),11, Datum.WGS84Datum) 
  )
  
  test("create, update and delete Marker") {

    val result = markerService.create(marker.traceId, 
                                      marker.getName,  
                                      marker.getInformation, 
                                      marker.typ, 
                                      marker.getPhotoFileName, 
                                      marker.creationTime,
                                      marker.getPosition )
    assert(result.id > 0, "id is < 0")
    assert(result.getName === marker.getName)
    assert(result.getInformation === marker.getInformation)
    assert(result.typ === marker.typ)
    assert(result.getPhotoFileName === marker.getPhotoFileName)
    assert(result.creationTime === marker.creationTime)
    assert(result.getPosition === marker.getPosition)
    
    intercept(classOf[IllegalArgumentException]) {
      result.rename("")
    }
    
    intercept(classOf[IllegalArgumentException]) {
      result.editInformation(null)
    }
    
    intercept(classOf[AssertionError]) {
      result.editPosition(null)
    }

    result.rename("Neuer Name")
    
    assert(result.save() === true)
    
    result.rename("xxxxx")
    
    assert(result.reload.getName === "Neuer Name")
    
    val result2 = result.reload
    
    assert(markerService.getAllMarkers.size === 1)
    assert(markerService.getAllMarkers.apply(0) === result2)
    
    assert(result2.delete === true)
    
    assert(markerService.getAllMarkers.size === 0)
    
    intercept(classOf[AssertionError]) {
      result2.delete()
    }
    
    intercept(classOf[IllegalArgumentException]) {
      markerService.exportAllMarkers(new java.io.File("/"))
    }
    
    assert(daoAccessor.markerDAO.fetchAll.size === 0)
    
  }
}
