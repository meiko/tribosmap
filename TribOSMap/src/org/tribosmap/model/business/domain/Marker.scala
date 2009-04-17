package org.tribosmap.model.business.domain


import scala.xml.{Node, Unparsed}
import java.text.SimpleDateFormat
import java.util.Date
import java.io.File
import org.tribosmap.model.math.geographic.Position

/**
 * Object of this class can be saved in the repository.
 * They represents a mark on the map,
 * they can contain different informations and a reference to a picture...
 * 
 * @param id the id of this object in the database, if < 0 not in database
 * @param traceId the traceId of the trace in the database, containig this marker
 * @param i_name the new name, length has to be between 1 and 255 (inclusive)
 * @param i_information the new information, length has to be between 0 and 2000000 (exclusive)
 * @param typ the type of the marker
 * @param creationTime the time when the marker was created
 * @param i_photoFileName the name of the photo image file (maybe null)
 * @param i_position the new position
 * @author Meiko Rachimow
 */
case class Marker(val id : Long, 
             val traceId: Long, 
             i_name: String, 
             i_information: String, 
             val typ: MarkerType.Value, 
             val creationTime: Long, 
             i_photoFileName: String, 
             i_position: Position ) extends java.io.Serializable { 
  
  //mutable variables
  private[this] var name: String = null
  private[this] var information: String = null
  private[this] var position: Position = null
  private[this] var photoFileName: String = null
  
  //init mutable variables and check that the given parameter are correct
  rename(i_name)
  editPosition(i_position)
  editInformation(i_information)
  editPhoto(i_photoFileName)
  
  //check other things
  require(typ != null, "the marker needs a type")
  
  /**
   * rename the marker
   * @param i_name the new name, length has to be between 1 and 255 (inclusive)
   */
  def rename(i_name: String) {
    require(i_name != null && i_name.length >= 1 && i_name.length <= 255, 
            "length of name has to be between 1 and 255")
    name = i_name
  }
  
  /**
   * edit the information of the marker
   * @param i_information the new information, length has to be between 0 and 2000000 (exclusive)
   */
  def editInformation(i_information: String) {
    require(i_information != null && i_information.length < 2000000, 
            "length of name has to be between 0 and 2000000")
    information = i_information
  }
  
  /**
   * edit the position of the marker
   * @param i_position the new position
   */
  def editPosition(i_position: Position) {
    require(i_position != null, "the marker needs a position")
    position = i_position
  }
  
  /**
   * edit the photo image filename
   * @param i_photoFileName the name of the photo image file (maybe null)
   */
  def editPhoto(i_photoFileName: String) {
    require(i_photoFileName == null || 
           {val parentFile = new File(i_photoFileName).getParentFile
            parentFile != null && parentFile.isDirectory }, 
            "the marker needs a correct imageFilename or 'null'")
    photoFileName = i_photoFileName
  }
  
  /**
   * @return the filename of the photo
   */
  def getPhotoFileName() = photoFileName
  
  /**
   * @return the name of the marker
   */
  def getName() = name
  
  /**
   * @return the informartion of the marker
   */
  def getInformation() = information

  /**
   * @return the position of the marker
   */
  def getPosition() = position
  
  /**
   * TODO: NOT IMPLEMENTED YET
   */
  def directionTo(markers: List[Marker]) = {
    0 //Direction
  }
  
  /**
   * TODO: NOT IMPLEMENTED YET
   */
  def distanceBetween(markers: List[Marker]) = {
    0 //Distance
  }

  
  /**
   * compute the xml node for this marker
   * @return the xml representation of this marker
   */
  def xml: Node  = 
      <wpt lat={ position.latLon.latitude.toString } lon={ position.latLon.longitude.toString } >
        <ele>{ position.altitude }</ele>
        <time>{ new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date(creationTime)) }</time>
        <name>{ name }</name>
        <desc>{ information }</desc>
        <type>{ typ  }</type>
        {if(photoFileName != null ) <link href={ (new java.io.File(photoFileName)).getName } ></link> else Unparsed("") }
      </wpt>
}
