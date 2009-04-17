package org.tribosmap.model.test.offline.business
import org.tribosmap.model.business.service._
import org.tribosmap.model.business.repository._

/*
 * The base class of activities which need access to services
 * here are some convenient methods to handle with the database objects, 
 */
trait OfflineTestServiceAccess
  extends org.specs.mock.JMocker 
  with org.specs.mock.ClassMocker
  with TraceServiceComponent 
  with MarkerServiceComponent 
  with GeoMapServiceComponent
  with TraceSegmentServiceComponent { 
  
 /**
  * the markerService  
  */
  lazy val markerService = new MarkerService()
  
 /**
  * the traceService  
  */
  lazy val traceService = new TraceService()
  
 /**
  * the mapService  
  */
  lazy val mapService = new GeoMapService()
  
 /**
  * the traceSegmentService  
  */
  lazy val traceSegmentService = new TraceSegmentService()
  
  def addExpectation = null
  def expecting[T] = expect(_: T)

  val daoAccessor = new DAOAccessor {
    val markerDAO: MarkerDAO = 
      mock[MarkerDAO]
  
    val traceDAO: TraceDAO = 
      mock[TraceDAO]
  
    val tracePointDAO: TracePointDAO = 
      mock[TracePointDAO]
  
    val traceSegmentDAO: TraceSegmentDAO = 
      mock[TraceSegmentDAO]
    
    val geoMapDAO: GeoMapDAO = 
      mock[GeoMapDAO]
      
    val geoReferencedPixelDAO: GeoReferencedPixelDAO =
      mock[GeoReferencedPixelDAO]
  }  
}
