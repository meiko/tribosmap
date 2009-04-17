package org.tribosmap.model.business.domain

import scala.xml.Node
import java.text.SimpleDateFormat

/**
 * Object of the type TraceSegment can be saved in the repository.
 * They represents a segment of a trace recorded by the user,
 * containing the TracePoints (only accecible through the services)
 * and belongs to a Trace.
 * 
 * @param id the id of this object in the database, if < 0 not in database
 * @param traceID the id of the trace containing this segment
 * @param size the number of points in this segment
 * @author Meiko Rachimow
 */
case class TraceSegment (val id: Long, 
                         val traceID: Long, 
                         var size: Long) extends java.io.Serializable {
  
  /**
   * compute the xml node for this tracesegment
   * @return the xml representation of this tracesegment
   */
  def xml: Node  = 
      <trkseg >
        <extensions> <segmentSize>{ size }</segmentSize> </extensions>
      </trkseg>
}
