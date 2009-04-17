package org.tribosmap.model.math.geographic

/** 
 *  coordinates in Universal Transverse Mercator projection
 *  Only the geodetic datum WGS84 is supported in this application now.
 *  <p>
 *  Here are total northing and easting values are used,
 *  so a latitude-band is unnecessary.
 *  the easting includes falseEasting (500.000 meters)
 *  the northing in the southern hemisphere include falseNorthing (10,000,000)
 *  </p>
 *  @param northingD the northing in meter 
 *  @param eastingD the easting in meter
 *  @param zoneNumber the UTM longitude zone (between 1 and 60 defined)
 *  @param hemisphere the Utm hemisphere (Hemisphere.NORTH or Hemisphere.SOUTH)
 *  @param datum the Datum of the utm
 * @author Meiko Rachimow
 */
case class UtmCoOrdinates(val northingD : Double, 
                          val eastingD : Double, 
                          val zoneNumber : Int, 
                          val hemisphere: Hemisphere.Value,
                          val datum: Datum)  {  
  
  //TODO: complete the requirements... 
  
  require(zoneNumber > 0 && zoneNumber < 61, "not a valid utm zone: " + zoneNumber)
  
  
  
  //if south minNorthing ==  1100000.0 (-80°lat)
  //if south maxNorthing == 10000000.0 (0°lat)
  require((hemisphere equals Hemisphere.North) || 
          (northingD >= 1100000 && northingD <= 10000000),
          "utm is not defined for this northing in southern hemisphere: " + northingD)
  
  //if north maxNorthing about 9.330.000m (84°lat)
  //if north minNorthing == 0 (0°lat)
  require((hemisphere equals Hemisphere.South) || 
          (northingD >= 0 && northingD <= 9333000),
          "utm is not defined for this northing in northern hemisphere: " + northingD)
  
  //eastings range from 166.000 to 834.000 at the equator
  //TODO: points in the Svalbard zones have other min easting
  //require(eastingD >= 166000 && eastingD <= 834000, "not a valid easting: " + eastingD)
  require(eastingD >= 0 && eastingD <= 834000, "not a valid easting: " + eastingD)
  
  /**
   * @return a rounded value of northing in meter
   */
  def northing = Math.round(northingD).toInt
  
  /**
   * @return a rounded value of easting in meter
   */
  def easting = Math.round(eastingD).toInt

  /**
   * will check the distance between two coords,
   * if the distance is < 5 cm the objects are similar
   * @param that the other UtmCoOrdinates
   * @return true if the objects are similar enough
   */
  def alwaysEqual(that: UtmCoOrdinates) = {
    this.equals(that) || 
      (hemisphere == that.hemisphere && 
       zoneNumber == that.zoneNumber &&
       Vector2d(northingD - that.northingD, 
                eastingD - that.eastingD).length < 0.05)
  }
  
  /**
   * will check the distance between two coords,
   * if the distance is < 5 cm the objects are similar
   * @param that the other UtmCoOrdinates
   * @return true if the objects are similar enough
   */
  def ≈(that: UtmCoOrdinates) = alwaysEqual(that)
  
  /**
   * will check the distance between two coords,
   * if the distance is < 5 cm the objects are similar
   * @param that the other UtmCoOrdinates
   * @return false if the objects are similar enough
   */
  def notAlwaysEqual(that: UtmCoOrdinates) = ! alwaysEqual(that)
  
  /**
   * will check the distance between two coords,
   * if the distance is < 5 cm the objects are similar
   * @param that the other UtmCoOrdinates
   * @return false if the objects are similar enough
   */
  def ≉(that: UtmCoOrdinates) = notAlwaysEqual(that)
  
  /**
   * @return the xml node of the utm coordinate
   */
  def toXml = {
    <utm northing={ northing.toString } 
         easting={ easting.toString } 
         zonenumber={ zoneNumber.toString } 
         hemisphere={ hemisphere.toString }/>
  }
  
  override def toString() : String = {
    northing + "N, " + easting + "E, " + zoneNumber + hemisphere
  }
}
