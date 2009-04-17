package org.tribosmap.converter

import java.io.File
import scala.io.Source
import scala.io.BufferedSource
import org.tribosmap.model.math.Vector2i
import org.tribosmap.model.math.geographic.Datum

/**
 * Die Klasse OziMap liest eine Ozi-Map Datei ein,
 * und bietet die Möglichkeit die Informationen
 * im Anschluß als XML Nodes zurückzugeben.
 * Auch ein Zugriff auf die einzelnen Informationen
 * der Karte ist unmittelbar nach der Instanziierung möglich.
 * 
 * @param mapFile die einzulesende Datei im OZI Map Format
 */
class OziMap(val mapFile: File) {
  
  //die referenzierte Datei der Pixelgrafik
  var imageFile: File = null
  
  //der Name der Karte
  var name : String = null
  
  //das Datum (Geoid) der Karte
  var datum : String = null
  
  //das verwendete KoordinatenSystem der Karte
  var coordinateSystem : String = null
  
  //die Projektion der Karte
  var projection : String = null
  
  //die georeferenzierten Punkte
  var referencePoints = List[OziMapPoint]()
  
  //die Dimension der Karte in Pixeln
  var size: Vector2i = null
  
  //die geöffnete Quelle über die angegebene Datei
  private val source = Source.fromFile(mapFile).asInstanceOf[BufferedSource]
  
  //Einlesen und Parsen der Datei
  try {
    val lines = source.getLines.counted
    lines.foreach(line => parseLine(lines.count, line))
  } finally {
    source.close
  }
  
  /**
   * Parsen einer Zeile der OziMap Datei
   * @param line die Zeile
   * @param index der Index der Zeile in der Datei (Zeilennummer)
   */
  private def parseLine(index: Int, i_line: String) {
    val line = i_line.replaceAll(System.getProperty("line.separator"), "")
    if(index <= 8) index match {
      
      //Metainformationen
      case 1 => 
        name = line.trim
      case 2 => 
        val fileName = line.trim
        imageFile = new File(fileName.subSequence(0, fileName.length - 3) + "tosm")
      case 4 => {
        val fields = line.split(",")
        datum = fields(0)
        coordinateSystem = fields(1)
      }
      case 8 => {
        val fields = line.split(",")
        projection = fields(1)
      }
      case _ => //ignore
        
    } else if(index <= 38 && line.contains(",N,")) {
      
      //erzeugen der georeferenzierten Punkte
      referencePoints = (new OziMapPoint(line: String, Datum(datum))) :: referencePoints
      
    } else if(index == 55) {
     
      //weitere Metainformationen
      val fields = line.split(",")
      size = Vector2i(fields(2).trim.toInt, fields(3).trim.toInt)
    }
  }
  
  /**
   * gibt die XML Repräsentation der angegebenen OZI Map Datei
   * @return XML ein XML Node Objekt, der RootNode der erzeugten XML Daten
   */
  def toXml = 
<map name={ name }>
  <file>{ imageFile }</file>
  <datum>{ datum }</datum>
  <coordsystem>{ coordinateSystem }</coordsystem>
  <projection>{ projection }</projection>
  <size x={ size.x.toString } y={ size.y.toString }/>
  <points>{
    for (point <- referencePoints) yield point.toXml
  }</points>
</map>
}