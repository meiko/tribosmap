package org.tribosmap.model.test.online.business

import org.scalatest.FunSuite
import org.tribosmap.model.business.domain._
import java.util.Date
import org.tribosmap.model.math.geographic._
import org.tribosmap.model.math.distance._
import java.lang.AssertionError
import android.test.AndroidTestCase

class TraceServiceTest extends AndroidTestCase with FunSuite with OnlineTestServiceAccess {

  val context = getContext

  val name = "testTrace"
  val information = "testInfo"
  val time = new Date().getTime
  
  test("create, save and delete trace") {
    
    val result = traceService.create(name, information, time)
    assert(result.id > 0, "id not > 0")
    assert(result.getName === name)
    assert(result.getInformation === information)
    assert(result.creationTime === time)
    
    result.rename("Neuer Name")
    assert(result.save() === true )
    
    result.rename("xxxx")
    
    val result2 = result.reload
    
    assert(traceService.getAllTraces.size === 1)
    assert(traceService.getAllTraces.apply(0) === result2)
    assert(result2.delete() === true)
    
    assert(traceService.getAllTraces.size === 0)
    
    intercept(classOf[AssertionError]) {
      result2.delete()
    }
  }

  
  test("add new segment") {
    
    val trace = traceService.create(name, information, time)
    val segment = trace.addNewSegment
    
    assert(trace.getAllSegments.size === 1)
    assert(trace.getAllSegments.apply(0) === segment)
    
    daoAccessor.traceSegmentDAO.delete(segment)
    assert(trace.getAllSegments.size === 0)
    
    trace.addNewSegment
    trace.delete
    assert(daoAccessor.traceSegmentDAO.fetchAll.size === 0)
  }
  
  
  test("add a Point to a traceSegment") {

    val trace = traceService.create(name, information, time)
    val segment = trace.addNewSegment
    
    val pointId = segment.addPoint(
      new Position( new GeographicCoOrdinatePair(11, 12), 13, Datum.WGS84Datum), 
      1, 2, 3, 4, new Date(time)
    )
    
    assert(pointId > 0, "pointId not > 0")
    
    val pointId2 = segment.addPoint(
      new Position( new GeographicCoOrdinatePair(11, 12), 13, Datum.WGS84Datum), 
      1, 2, 3, 4, new Date(time)
    )
    
    assert(pointId != pointId2, "pointId == pointId2 but should be !=")
    
    assert(daoAccessor.tracePointDAO.fetchAll.size === 2)
    
    trace.delete
    
    assert(daoAccessor.tracePointDAO.fetchAll.size === 0)
    
  }
  


}
