package org.tribosmap.model.business.domain

import org.tribosmap.model.math._
import org.tribosmap.model.math.geographic.UtmCoOrdinates

/**
 * Object of this class can be saved in the repository,
 * they represents a pixel on a map referenced by a geographic position,
 * 
 * @param id the id of this object in the database, if < 0 not in database
 * @param mapId the mapId of the map in the database, containig this GeoReferencedPixel
 * @param pixel the pixel coordinate
 * @param utm the utm coordinate
 * @author Meiko Rachimow
 */
case class GeoReferencedPixel(val id: Long, 
                         val mapId: Long, 
                         val pixel: Vector2i,
                         val utm: UtmCoOrdinates) extends java.io.Serializable { 
  
}
