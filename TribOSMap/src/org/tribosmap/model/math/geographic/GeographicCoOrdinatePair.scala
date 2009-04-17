package org.tribosmap.model.math.geographic

import java.text.DecimalFormat

/** 
 * A geographic position on a sphere. 
 * (latitude: form -90° to 90°, longitude: from -180° to 180°)
 * for western longitude negative values are used
 * for southern latitude negative values are used
 * <p>
 * Note: be careful with latitudes smaller than -80 and bigger than 84 degree,
 * for that values is the utm conversion not defined. (per definition of utm...)
 * 
 * @param lat the latitude geographic coordinate [-91 < latitude < 91] (in degree North, for South the value has to be negative)
 * @param lon the longitude geographic coordinate [-181 < longitude < 181] (in degree East, for West the value has to be negative)
 * @author Meiko Rachimow
 */
case class GeographicCoOrdinatePair (val latitude: Double, val longitude: Double) 
  extends Vector2d(latitude, longitude) {

  require(-91 < latitude && latitude < 91, 
          "latitude out of range: [-91 < latitude < 91] is " + latitude)
          
  require(-181 < longitude && longitude < 181, 
          "longitude out of range: [-181 < longitude < 181] is " + longitude)
  
  
  /**
   * will check the distance between two coords,
   * if the distance is < 0.000006 degree the objects are similar
   * @param that the other GeographicCoOrdinatePair
   * @return true if the objects are similar enough
   */
  def alwaysEqual(that: GeographicCoOrdinatePair) = {
    this.equals(that) || (that - this).length < 0.000006
  }
  
  /**
   * will check the distance between two coords,
   * if the distance is < 0.000006 degree the objects are similar
   * @param that the other GeographicCoOrdinatePair
   * @return true if the objects are similar enough
   */
  def ≈(that: GeographicCoOrdinatePair) = alwaysEqual(that)
  
  /**
   * will check the distance between two coords,
   * if the distance is < 0.000006 degree the objects are similar
   * @param that the other GeographicCoOrdinatePair
   * @return False if the objects are similar enough
   */
  def notAlwaysEqual(that: GeographicCoOrdinatePair) = ! alwaysEqual(that)
  
  /**
   * will check the distance between two coords,
   * if the distance is < 0.000006 degree the objects are similar
   * @param that the other GeographicCoOrdinatePair
   * @return False if the objects are similar enough
   */
  def ≉(that: GeographicCoOrdinatePair) = notAlwaysEqual(that)
  
  override def toString() : String = {
    val format = new DecimalFormat()
    format.setMaximumFractionDigits(5)
    format.format(latitude) + "°N, " + format.format(longitude) + "°E"
  }
}

