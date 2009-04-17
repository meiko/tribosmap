package org.tribosmap.model.business.domain

import scala.xml.Node

import org.tribosmap.model.math.geographic.Position;

import java.text.SimpleDateFormat
import java.util.Date

/**
 * A Point of a Tracesegment, the atomic object of a trace.
 * They are saved in the repository, and belongs to a TraceSegment.
 * 
 * @param id the id of the point in the repository
 * @param segmentID the id of the segment in the database, references the point
 * @param time the time when the point was saved in the database
 * @param position the position of the point
 * @param satellites the number of sattelites the gps receiver was connected, when saving this point
 * @param course the direction between this and the last saved point
 * @param speed the speed of the gps-receiver, when saving this point
 * @param hdop the quality of the gps-signal, when saving this point
 * @author Meiko Rachimow
 */
case class TracePoint (val id: Long, 
                  val segmentID: Long, 
                  val time: Long, 
                  val position: Position, 
                  val satellites: Int, 
                  val course: Float, 
                  val speed: Float, 
                  val hdop: Float) extends  java.io.Serializable {
  
  require(position != null, "no position given for the tracePoint")
  
  /**
   * compute the xml node for this point
   * @return the xml representation of this point
   */
  def xml: Node  = 
    <trkpt lat={ position.latLon.latitude.toString } lon={ position.latLon.longitude.toString } >
      <ele>{ position.altitude }</ele>
      <time>{ new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date(time)) }</time>
      <course>{ course }</course>
      <speed>{ speed }</speed>
      <sat>{ satellites }</sat>
      <hdop>{ hdop }</hdop>
    </trkpt>
}
  

