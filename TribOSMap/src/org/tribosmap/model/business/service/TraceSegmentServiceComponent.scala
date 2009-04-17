package org.tribosmap.model.business.service

import java.io.File
import java.util.Date

import android.location.{Location}
import org.tribosmap.model.business.repository.DAOAccessor
import org.tribosmap.model.business.domain.{Trace, TraceSegment, TracePoint}
import org.tribosmap.model.math.geographic.{Position, GeographicCoOrdinatePair}
import org.tribosmap.model.math.distance.Distance
import org.tribosmap.model.math.distance.MetricUnit.Meter

trait TraceSegmentServiceComponent {

  //a Service to work with traces
  protected[service] val traceSegmentService: TraceSegmentService  

  private[model] val daoAccessor: DAOAccessor

  
  /**
   * The service to work with a trace and the containing objects in the trace
   * (traceSegment and tracePoint)
   * to use this class, you have to mix the trait TraceServiceComponent
   * in a class, and define the repositories...
   */
  protected[service] class TraceSegmentService {
  
    //the repositories
    private[this] val traceSegmentDAO = daoAccessor.traceSegmentDAO
    private[this] val tracePointDAO = daoAccessor.tracePointDAO
    
    /**
     * Save a traceSegment to the repository
     * @param segment the trace segment
     * @return if saved true else false
     */
    def save(segment: TraceSegment) = {
      val saved = if(segment.id < 0) {
        traceSegmentDAO.create(segment) != null
      } else {
        traceSegmentDAO.update(segment)
      }
      assert(saved, 
             "cannot save the traceSegment in the repository")
      saved
    }
    
    /**
     * create a new TraceSegment
     * @param trace the parent trace of the segment
     */
    def create(trace: Trace) = {
      traceSegmentDAO.create(new TraceSegment(-1, trace.id, 0))
    }
    
    def segmentsToXmlString(trace: Trace): String = {
      val stringBuffer = new StringBuffer()
      val lineSeparator = System.getProperty("line.separator")
      traceSegmentDAO.foreachSegment(trace)( segment ⇒ {
          
          val segmentXML = segment.xml
          stringBuffer.append("<" + segmentXML.label + ">")
          stringBuffer.append(lineSeparator)
          
          segmentXML.child.foreach( childXml ⇒ {
            stringBuffer.append(childXml.toString)
            stringBuffer.append(lineSeparator)
          })
          
          tracePointDAO.foreachPoint(segment)( point ⇒ {
              stringBuffer.append(point.xml.toString)
              stringBuffer.append(lineSeparator)
          })
          
          stringBuffer.append("</" + segmentXML.label + ">")
          stringBuffer.append(lineSeparator)
      })
      
      stringBuffer.toString
      
    }
    
    /**
     * Get all segments for a trace
     * 
     * @param trace the Trace
     * @return the list of tracesegments
     */
    def getAllSegments(trace: Trace): List[TraceSegment] = {
      traceSegmentDAO.fetchAllSegments(trace)
    }
    
    /**
     * Add a point to a trace segment and save it to the repository
     * 
     * @param segment the TraceSegment
     * @param position the Position of the point
     * @param satelliteCount the actual count of satellites
     * @param bearing the actual bearing
     * @param speed the actual speed
     * @param hdop the actual hdop (quality of signal)
     * @time the time used to order the points by time
     * @return the id of the saved point
     */
    def addPoint(segment: TraceSegment, 
                 position: Position, 
                 satelliteCount: Int,
                 bearing: Float,
                 speed: Float,
                 hdop: Float,
                 time: Date) = {

      val id = tracePointDAO.createFast ( 
        TracePoint(
          -1, 
          segment.id, 
          time.getTime, 
          position,
          satelliteCount,
          bearing,
          speed,
          hdop
      ))
      assert(id >= 0, "cannot write point")
      id
    }
    
  }
  
  /**
   * implicit definition, for TraceSegment objects
   */
  protected implicit def doOnTraceSegment(segment: TraceSegment) = new {
    
    /**
     * Add a point to the trace segment and save it to the repository
     * 
     * @param position the Position of the point
     * @param satelliteCount the actual count of satellites
     * @param bearing the actual bearing
     * @param speed the actual speed
     * @param hdop the actual hdop (quality of signal)
     * @time the time used to order the points by time
     * @return the id of the saved point
     */
    def addPoint( position: Position, satelliteCount: Int, bearing: Float,
      speed: Float, hdop: Float, time: Date) = 
        traceSegmentService.addPoint(
          segment, position, satelliteCount, bearing, speed, hdop, time)
    
    /**
     * Save the traceSegment to the repository
     * @return if saved true else false
     */
    def save() = traceSegmentService.save(segment)
  }
                                           
}
