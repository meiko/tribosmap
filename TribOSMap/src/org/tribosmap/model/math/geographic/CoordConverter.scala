package org.tribosmap.model.math.geographic


import java.lang.Math._

/**
 * Converts geographic coordinates to utm coordinates
 * and vice versa
 * <p>
 * The utmcoordinates here are using the false-northing/easting
 * absolut coordinates (without the need of a zone-letter, but with the
 * need for a given heimsphere)
 * </p>
 * <p>
 * Based on the Algorithm in Paper:
 * "How to Use the Spreadsheet for 
 * Converting UTM to Latitude and Longitude (Or Vice Versa) –Part 1"
 * by Steven Dutch, University of Wisconsin ­ Green Bay
 * (http://www.uwgb.edu/dutchs/index.html)
 * </p>
 * 
 * @param datum a datum describing the used geoid
 * @author Meiko Rachimow
 */
class CoordConverter(datum: Datum) {
  
  private[this] val a = datum.equatorialRadius
  
  private[this] val f = datum.flattening

  /**
   * the scale factor along the central meridian
   */
  private[this] val k0 = 0.9996
  
  /**
   * Semi-minor Axis in meter (polar radius of the ellipsoid)
   */
  private[this] val b = (1.0-f)*a

  private[this] val radToDeg = 180.0 / PI
  
  private[this] val am2 = a*a
  
  private[this] val bm2 = b*b
  
  /**
   * Second Eccentricity Squared (e'²)
   * a² / b² - 1
   */
  private[this] val e2m2 = am2 / bm2 - 1

  /**
   * First Eccentricity Squared (e²)
   * 1 - b² / a²
   */
  private[this] val em2 = 1 - bm2 / am2

  /**
   * (a-b)/(a+b)
   */
  private[this] val n = (a-b) / (a+b)
  private[this] val nm2 = n * n
  private[this] val nm3 = nm2 * n
  private[this] val nm4 = nm3 * n
  private[this] val nm5 = nm4 * n
  
  /** 
   * convert a geographic coordinate (latitude/longitude pair) to an UTM-coordinate.
   * The UTM conversion is defined for latitudes between -80 an 84 degree and for
   * longitudes between -360 an 360 degree.
   * @param geoCoord geographic coordinate
   * @return the utm coordinate
   */
  def latLonToUtm(geoCoord: GeographicCoOrdinatePair) : UtmCoOrdinates = {

    val x = geoCoord.latitude
    val y = geoCoord.longitude
    
    //UTM system only defined for geo-latitude between 80° S and 84° N
    require(x >= -80 && x <= 84, "latitude out of range: [-80 <= latitude <= 84] is " + x)
    
    //TODO: evtl unnecessary
    require(y >= -360 && y <= 360, "longitude out of range: [-360 <= latitude <= 360] is " + y)
    
    val degToRad = PI / 180.0
    
    //lon: -180.00 .. 179.9
    val lonTmp = (y+180.0) - ((y+180.0)/360.0).toInt * 360.0 - 180.0
    
    //zones outside grid ( Exceptions in norway...)
    val isSvalbardZone = (x >= 72.0 && x < 84.0)
    val zoneNumber = {
      if (x >= 56.0 && x < 64.0 && lonTmp >= 3.0 && lonTmp < 12.0) 32
      else if  (isSvalbardZone && lonTmp >= 0.0  && lonTmp <  9.0) 31
      else if (isSvalbardZone && lonTmp >= 9.0  && lonTmp < 21.0) 33
      else if (isSvalbardZone && lonTmp >= 21.0 && lonTmp < 33.0) 35
      else if (isSvalbardZone && lonTmp >= 33.0 && lonTmp < 42.0) 37
      else ((lonTmp + 180)/6).toInt + 1
    }	
    
    val latRad = x * degToRad
    val lonRad = lonTmp * degToRad
    
    //central meridian
    val lonOriginRad = ((zoneNumber - 1) * 6.0 - 180.0 + 3.0) * degToRad

    val em4 = em2 * em2
    val em6 = em4 * em2

    //Calculate the Meridional Arc
    val M = a * ((1 - em2 / 4 - 3 * em4 / 64 - 5 * em6 / 256) * latRad  
        - (3 * em2/8 + 3 * em4 / 32 + 45 * em6 / 1024) * sin(2 * latRad) 
        + (15 * em4 / 256 + 45 * em6 / 1024 ) * sin(4 * latRad) 
        - (35 * em6 / 3072) * sin(6 * latRad))
    
    //val eccPrimeSquared = em2 / (1-em2)
    //Calculate Lat and Long
    val N = a / sqrt(1 - em2 * sin(latRad) * sin(latRad))
    val T = tan(latRad) * tan(latRad)
    val Tm2 = T * T
    val C = e2m2 * cos(latRad) * cos(latRad)
    val A = cos(latRad) * (lonRad - lonOriginRad)
    val Am2 = A*A
    val Am3 = Am2 * A
    
    val utmEasting = 
      k0 * N * (A + (1 - T + C) * 
      Am3/6 + (5 - 18 * T + Tm2 + 72 * C - 58 * e2m2) * 
      Am2 * Am3/120) + 500000.0

    val utmNorthing = 
      k0 * (M + N * tan(latRad) * 
      (Am2/2 + (5 - T + 9 * C + 4 * C * C) * Am3 * A/24  + 
      (61 - 58 * T + Tm2 + 600 * C -330 * e2m2) * Am3 * Am3/720)) 
    
    if (x < 0) {
      UtmCoOrdinates(utmNorthing + 10000000.0, 
                     utmEasting, zoneNumber, Hemisphere.South, datum)
    } else {
      UtmCoOrdinates(utmNorthing, 
                     utmEasting, zoneNumber, Hemisphere.North, datum)
    }
  }

  
  /** 
   *  convert UTM-coordinates to geographic coordinates (latitude/longitude pair)
   *  <p>
   *  Here are total northing and easting values are used,
   *  so a latitude-band is unnecessary.
   *  the easting includes falseEasting (500.000 meters)
   *  the northing in the southern hemisphere include falseNorthing (10.000.000 meters)
   *  @see UtmCoOrdinates
   * 
   *  @param utm the utm coordinates
   *  @return the geographic coordinates
   */
  def utmToLatLon(utm: UtmCoOrdinates) = {

    //central meridian of zone
    val lonOrigin = (utm.zoneNumber - 1.0) * 6.0 - 177.0
    
    //x subtract false-easting
    val x = utm.eastingD - 500000.0 
    
    //y subtract false-northing if in southern hemisphere
    val y = if(utm.hemisphere == Hemisphere.North) utm.northingD
            else (utm.northingD - 10000000.0)
    
    //meridional arc, the true meridional distance on the ellipsoid from the equator
    val M = y / k0
    
    //to calculate footprint latitude
    val mu = M / (a * (1.0 - em2/4.0 - 
                  3.0 * em2 * em2/64.0 - 
                  5.0 * em2 * em2 * em2/256.0))

    val j1 = ( 3.0 * n / 2.0 ) - (27.0 * nm3 / 32.0 ) 
    val j2 = ( 21.0 * nm2 / 16.0 ) - (55.0 * nm4 / 32.0 ) 
    val j3 = ( 151.0 * nm3 / 96.0) 
    val j4 = ( 1097.0 * nm4 / 512.0 ) 
    
    
    //the footprint latitude
    val fp = ( mu + j1 * sin(2.0 * mu) + j2 * sin(4.0 * mu) + 
        j3 * sin(6.0 * mu) + j4 * sin(8.0 * mu))
    
    
    //calculate latitude longitude
    val radToDeg = 180.0 / PI
    val N1 = a / sqrt( 1 - em2 * sin(fp) * sin(fp) )
    val T1 = tan(fp) * tan(fp)
    val C1 = e2m2 * cos(fp) * cos(fp)
    val R1 = a * ( 1 - em2 ) / pow( 1 - em2 * sin(fp) * sin(fp), 1.5)
    val D = x / ( N1 * k0)               
    val Dm2 = D*D
    val Dm3 = Dm2 * D
    val Dm4 = Dm3 * D
    val C1m2 = C1 * C1
    val T1m2 = T1 * T1
    val lat = (fp - (N1 * tan(fp) / R1) * (
       Dm2 / 2 -
       (5.0 + 3.0 * T1 + 10.0 * C1 - 4.0 * C1m2 - 9.0 * e2m2) * Dm4 / 24.0 +
       (61.0 + 90.0 * T1 + 298.0 * C1 + 45.0 * T1m2 - 252.0 * e2m2 - 3.0 * C1m2 ) * 
         Dm3 * Dm3 / 720.0)) * radToDeg
    
    val lon = lonOrigin + ( (D-(1.0 +2.0 * T1 + C1) * Dm3 / 6.0 + 
         (5.0 - 2.0 * C1 + 28.0 * T1 - 3.0 * C1m2 + 8.0 * e2m2 + 24.0 * T1m2) * 
            Dm3 * Dm2 / 120.0 ) / cos(fp) ) * radToDeg
    
    GeographicCoOrdinatePair(lat, lon)
    
  }

}

