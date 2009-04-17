package  org.tribosmap.model.business.domain

import java.util.Date
import org.tribosmap.model.math.Vector2i
import org.tribosmap.model.math.geographic.Datum

/**
 * Object of this class can be saved in the repository,
 * they represents a map which is georeferenced by Objects of the class GeoReferencedPixel.
 * This GeoMap is used to work with Offline Maps. These maps holds their data in the tosm-files.
 * The contain some mata-information describing the map.
 * 
 * @param id the id of this object in the database, if < 0 not in database
 * @param i_name the new name, length has to be between 1 and 255 (inclusive)
 * @param fileName the filename of the mapfile
 * @param imageSize the size in pixel
 * @param datum the datum of the mapfile
 * @param projection the projection of the map (TODO: not used in this application now)
 * @param time the time when the map was created
 * @author Meiko Rachimow
 */
case class GeoMap(val id:Long, 
             i_name:String, 
             val fileName: String, 
             val imageSize: Vector2i,
             val datum: Datum, 
             val projection: String, 
             val time: Long) extends java.io.Serializable { 
  
  private[this] var name: String = null
  
  rename(i_name)
  
  /**
   * rename the map
   * @param i_name the new name, length has to be between 1 and 255 (inclusive)
   */
  def rename(i_name: String) = {
    require(i_name != null && i_name.length >= 1 && i_name.length <= 255, 
            "length of name has to be between 1 and 255")
    name = i_name
  }
  
  def getName = name

}
