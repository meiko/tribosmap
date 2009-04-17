package org.tribosmap.model.business.domain

import scala.xml.Node
import java.text.SimpleDateFormat
import java.util.Date
import org.tribosmap.model.math.distance.Distance

/**
 * object of the type Trace can be saved in the repository,
 * they represents a trace recorded by the user,
 * These objects contains some information and tracesegments of a record.
 * 
 * @param id the id of this object in the database, if < 0 not in database
 * @param i_name the new name, length has to be between 1 and 255 (inclusive)
 * @param i_information the new information, length has to be between 0 and 2000000 (exclusive)
 * @param time the time when the trace was created
 * @param pointCount the number of points in this trace
 * @author Meiko Rachimow
 */
case class Trace (val id: Long, 
                  i_name:String, 
                  i_information:String, 
                  val creationTime: Long, 
                  var pointCount: Int,
                  var distance: Distance) extends java.io.Serializable {
  
  //mutable variables
  private[this] var name: String = null
  private[this] var information: String = null
  
  //init mutable variables and check parameters
  rename(i_name)
  editInformation(i_information)
  
  
  /**
   * @return the name of the trace
   */
  def getName() = name
  
  /**
   * @return the information of the trace
   */
  def getInformation() = information
  
  /**
   * rename the trace
   * @param i_name the new name, length has to be between 1 and 255 (inclusive)
   */
  def rename(i_name: String) {
    require(i_name != null && i_name.length >= 1 && i_name.length <= 255, 
            "length of name has to be between 1 and 255")
    name = i_name
  }
  
  /**
   * edit the information of the trace
   * @param i_information the new information, length has to be between 0 and 2000000 (exclusive)
   */
  def editInformation(i_information: String) {
    require(i_name != null && i_information.length < 2000000, 
            "length of name has to be between 0 and 2000000")
    information = i_information
  }
  
  /**
   * compute the xml node for this trace
   * @return the xml representation of this trace
   */
  def xml: Node  = {
    <trk >
      <name> {name} </name>
      <cmt> { new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date(creationTime)) } </cmt>
      <desc> {information} </desc>
      <extensions> <pointCount>{ pointCount }</pointCount> </extensions>
    </trk> }
}

