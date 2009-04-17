package org.tribosmap.model.test.offline.business

import java.util.Date
import java.lang.AssertionError
import org.scalatest.FunSuite
import org.tribosmap.model.business.domain._
import org.tribosmap.model.math.geographic._
import org.tribosmap.model.math.distance._
import org.tribosmap.model.math.distance.MetricUnit._
import org.tribosmap.model.math.distance.AmericanUnit._

class TraceServiceTest extends FunSuite with OfflineTestServiceAccess {

  val name = "testTrace"
  val information = "testInfo"
  val time = new Date().getTime
  
  val testTrace1 = Trace(-1,
                         name, 
                         information, 
                         time, 
                         0, 
                         Distance(0, Meter))
  
  val testTraceSaved = Trace(99,
                             name, 
                             information, 
                             time, 
                             0, 
                             Distance(0, Meter))
  
  val testTraceSegment = TraceSegment(-1, testTraceSaved.id, 0) 
  
  val testTraceSegmentSaved = TraceSegment(1, testTraceSaved.id, 0) 
  

  
  test("create trace") {
    
    expecting {
      one(daoAccessor.traceDAO).create(testTrace1) willReturn testTrace1
    }

    assert(testTrace1 === traceService.create(name, information, time))
    
  }
  
  test("delete trace") {

    expecting {
        one(daoAccessor.traceDAO).delete(testTrace1) willReturn true
        one(daoAccessor.traceDAO).delete(testTrace1) willReturn false
    }
    
    assert(testTrace1.delete() === true)
 
    intercept(classOf[AssertionError]) {
      testTrace1.delete()
    }
  }
  
  test("save trace") {
    
    expecting {
        one(daoAccessor.traceDAO).create(testTrace1) willReturn testTrace1
        one(daoAccessor.traceDAO).update(testTraceSaved) willReturn true
        one(daoAccessor.traceDAO).update(testTraceSaved) willReturn false
    }
  
    assert(testTrace1.save() === true )
    assert(testTraceSaved.save() === true )
    intercept(classOf[AssertionError]) {
      testTraceSaved.save()
    }

  }
  
  test("add new segment") {
    
    val segmentFromUnsafedTrace = TraceSegment(-1, testTrace1.id, 0)
    
    expecting {
      one(daoAccessor.traceDAO).create(testTrace1) willReturn testTrace1
      one(daoAccessor.traceSegmentDAO).create(segmentFromUnsafedTrace) willReturn segmentFromUnsafedTrace
      
      one(daoAccessor.traceDAO).update(testTraceSaved) willReturn true
      one(daoAccessor.traceSegmentDAO).create(testTraceSegment) willReturn testTraceSegment
      one(daoAccessor.traceDAO).update(testTraceSaved) willReturn false

    }
    
    assert(testTrace1.addNewSegment() === segmentFromUnsafedTrace)
    assert(testTraceSaved.addNewSegment() === testTraceSegment)
    
    intercept(classOf[AssertionError]) {
      testTraceSaved.addNewSegment()
    }
    
  }
  
  test("save trace segment") {
    
    expecting {
        one(daoAccessor.traceSegmentDAO).create(testTraceSegment) willReturn testTraceSegment
        one(daoAccessor.traceSegmentDAO).update(testTraceSegmentSaved) willReturn true
        one(daoAccessor.traceSegmentDAO).update(testTraceSegmentSaved) willReturn false
    }
  
    assert(testTraceSegment.save() === true )
    assert(testTraceSegmentSaved.save() === true )
    
    intercept(classOf[AssertionError]) {
      testTraceSegmentSaved.save()
    }
  }
  
  
  test("add a Point to a traceSegment") {

    val testPoint = TracePoint(
      -1, testTraceSegmentSaved.id, time, 
      new Position( GeographicCoOrdinatePair(11, 12), 13, Datum.WGS84Datum), 
      1, 2, 3, 4 )

    expecting {
      one(daoAccessor.tracePointDAO).createFast(testPoint) willReturn 42
      one(daoAccessor.tracePointDAO).createFast(testPoint) willReturn -1
    }
    
    assert(
      testTraceSegmentSaved.addPoint(
        new Position( new GeographicCoOrdinatePair(11, 12), 13, Datum.WGS84Datum), 
        1, 2, 3, 4, new Date(time)
      ) === 42
    )
    
    intercept(classOf[AssertionError]) {
      testTraceSegmentSaved.addPoint(
        new Position( new GeographicCoOrdinatePair(11, 12), 13, Datum.WGS84Datum), 
        1, 2, 3, 4, new Date(time)
      )
    }
  }
  


}
