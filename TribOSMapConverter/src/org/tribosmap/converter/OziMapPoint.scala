package org.tribosmap.converter
import org.tribosmap.model.math.{Vector2i, Vector2d}
import org.tribosmap.model.math.geographic.{UtmCoOrdinates, Hemisphere, Datum}

import scala.xml.{Node, Unparsed}

/**
 * Parsen einer Zeile der OziMap Datei nach den
 * Informationen die einen georeferenzierten Punkt 
 * beschreiben.
 * 
 * @param line die Zeile einer OziMap Datei, die einen georeferenzierten Punkt beschreibt
 */
class OziMapPoint (val line: String, datum: Datum) {
  
  //die felder der Zeile
  private val fields = line.split(",").map(f => f.trim)
  
  //die Koordinaten des georeferenzierten Pixels
  val pixel = Vector2i(fields(2).trim.toInt, fields(3).trim.toInt)
  
  //die referenzierten UTM Koordinaten des Pixels
  val utm = getUtm(fields(13), fields(14), fields(15), fields(16))
  
  //die referenzierten geografischen Koordinaten des Pixels
  val latLon = getLatLon(fields(6), fields(7), fields(8), 
                         fields(9), fields(10), fields(11))

  
  private def getLatLon(degreeLat: String, minuteLat: String, 
                        hemisphereLat: String,
                        degreeLon: String, minuteLon: String, 
                        hemisphereLon: String) : Option[Vector2d] = {
    if(degreeLat.length == 0){
      return None
    }
    val degreeLatResult = if(hemisphereLat.equals("S")){
      - degMinuteToDeg(degreeLat.toDouble, minuteLat.toDouble)
    } else {
      degMinuteToDeg(degreeLat.toDouble, minuteLat.toDouble)
    }
      
    val degreeLonResult = if(hemisphereLon.equals("W")){
      - degMinuteToDeg(degreeLon.toDouble, minuteLon.toDouble)
    } else {
      degMinuteToDeg(degreeLon.toDouble, minuteLon.toDouble)
    }
    Some(Vector2d(degreeLatResult, degreeLonResult))
  }
  
  private def degMinuteToDeg(degree: Double, minute: Double) = {
    degree + minute / 60.0
  }
  
  private def getUtm(zone: String, easting: String, northing: String, hemisphereLetter: String) : Option[UtmCoOrdinates] = {
    if(zone.length == 0){
      return None
    }
    val hemisphere = Hemisphere(
      hemisphereLetter match {
        case "N" => "North"
        case "S" => "South"
      }
    )
    Some(UtmCoOrdinates(northing.toDouble, easting.toDouble, zone.toInt, hemisphere, datum))
    
  }
  
  override def toString(): String = {
    "Pixel: " + pixel + ", LatLon: " + latLon + ", Utm: " + utm
  }
  
  def toXml:Node = {
  <point x={ pixel.x.toString } y={ pixel.y.toString }>
    {if(latLon.isDefined) {
      <latLon latitude={ latLon.get.x.toString } longitude={ latLon.get.y.toString } />
    }else Unparsed("")
    if(utm.isDefined) {
      utm.get.toXml
    }else Unparsed("")}
  </point>
  
  }
}