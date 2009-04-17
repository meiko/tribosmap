package org.tribosmap.model.test.offline.business


import org.scalatest.FunSuite
import org.tribosmap.model.business.domain.{Marker, MarkerType}
import org.tribosmap.model.math.geographic._
import java.lang.AssertionError
import java.util.Date

class MarkerServiceTest extends FunSuite with OfflineTestServiceAccess {
  
  val id = -1l
  val traceId = 11l
  val name = "testName"
  val info = "testInfo"
  val time = (new Date()).getTime
  val fileName = "/home/meiko/tmp/aa.tst"
  val position = new Position(GeographicCoOrdinatePair(12,14),11, Datum.WGS84Datum)
  val markerType = MarkerType.PointOfInterest
  
  val savedId = 99
  
  val marker = Marker(
    id, 
    traceId, 
    name,  
    info, 
    markerType, 
    time, 
    fileName, 
    position 
  )
  
  val savedMarker = Marker(
    savedId, 
    traceId, 
    name,  
    info, 
    markerType, 
    time, 
    fileName, 
    position 
  )
  
  val savedMarker2 = Marker(
    savedId, 
    traceId, 
    name + "neu",  
    info + "neu", 
    markerType, 
    time, 
    fileName, 
    position 
  )

  test("create marker") {
    
    expecting {
      one(daoAccessor.markerDAO).create(marker) willReturn marker
    }

    assert(marker === markerService.create(traceId, 
                                           name,  
                                           info, 
                                           markerType, 
                                           fileName, 
                                           time,
                                           position ))
    
  }
  
  test("delete marker") {

    expecting {
        one(daoAccessor.markerDAO).delete(marker) willReturn true
        one(daoAccessor.markerDAO).delete(marker) willReturn false
    }
    
    assert(marker.delete() === true)
 
    intercept(classOf[AssertionError]) {
      marker.delete()
    }
  }
  
  test("save marker") {
    expecting {
        one(daoAccessor.markerDAO).create(marker) willReturn marker
        one(daoAccessor.markerDAO).update(savedMarker) willReturn true
        one(daoAccessor.markerDAO).update(savedMarker) willReturn false
    }
    assert(marker.save() === true )
    assert(savedMarker.save() === true )
    intercept(classOf[AssertionError]) {
      savedMarker.save()
    }
  }
  
  test("reload marker") {
    
    expecting {
        one(daoAccessor.markerDAO).fetch(savedId) willReturn savedMarker2
    }
    
    val resultMarker = savedMarker.reload()
    assert(resultMarker === savedMarker2)
    
    intercept(classOf[IllegalArgumentException]) {
      marker.reload()
    }
    
  }
  
  test("export All Markers") {
    
    intercept(classOf[IllegalArgumentException]) {
      markerService.exportAllMarkers(new java.io.File("/"))
    }
    
    /*
    
    def fun(file: java.io.File) = (marker: Marker) ⇒ { println(marker, file) }
    val fileTest = new java.io.File("/home/meiko/tmp/tst.tst")
    
    expecting {
        one(ServiceRegistry.markerDAO).foreach(fun(fileTest))
    }
    markerService.exportAllMarkers(fileTest)
    */
    
  }
}
