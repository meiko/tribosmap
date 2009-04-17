package org.tribosmap.model.math.geographic

import java.text.DecimalFormat
import org.tribosmap.model.math.distance._

/** 
 * A position on earth. Based on the WGS84 geodatic datum.
 * Contains the utm coordinates, geographic coordinates and the altiude.
 * <p>
 * TODO: better not two coordinate types in the same object ???
 * <p>
 * UTM provides constant distance relationship on the map. 
 * So the UtmCoOrdinates are used together with the altitude to calculate distances.
 * 
 * @param latLon a  GeographicCoordinatePair (latitude, longitude)
 * @param utm the utm coord
 * @param altitude the height in meter about sea level
 * @author Meiko Rachimow
 */
case class Position private (val latLon: GeographicCoOrdinatePair, 
                             val utm: UtmCoOrdinates, 
                             val altitude: Double) 
  extends java.io.Serializable {
 
  /** 
   *  @param utm UtmCoOrdinates
   *  @param altitude the height in meter about sea level
   */
  def this(utm: UtmCoOrdinates, altitude: Double) = 
    this(new CoordConverter(utm.datum).utmToLatLon(utm), utm, altitude)
  
  /** 
   *  @param latLon a  GeographicCoordinatePair (latitude, longitude)
   *  @param altitude the height in meter about sea level
   */
  def this(latLon: GeographicCoOrdinatePair, altitude: Double, datum: Datum) = 
    this(latLon, new CoordConverter(datum).latLonToUtm(latLon), altitude)
  
  /**
   * Computes the distance between this Position and the parameter
   * (Euclidean distance)
   * @param pos the Position
   * @return the Distance between the positions
   */
  def distanceTo(pos: Position): Distance = {
    
    val x = Math.abs(pos.utm.eastingD - utm.eastingD)
    val y = Math.abs(pos.utm.northingD - utm.northingD)
    val z = Math.abs(pos.altitude - altitude)
    Distance(Math.sqrt(x * x + y * y + z * z), MetricUnit.Meter)
  }

  override def toString() : String = {
    val format = new DecimalFormat();
    format.setMaximumFractionDigits(1);
    val altString = format.format(altitude) + "m"
    latLon + ", " + altString
  }
}
