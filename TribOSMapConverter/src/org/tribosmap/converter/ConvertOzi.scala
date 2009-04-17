package org.tribosmap.converter

import java.io.File
import scala.xml.{Node, PrettyPrinter}

object ConvertOzi {
  
  /**
  * Schreibt einen XML Baum in eine Datei
  * @param targetFile die ZielDatei
  * @param xmlNode der RootNode des zu schreibenden XML Baumes
  */
  def writeXmlToFile(targetFile: File, xmlNode: Node) {
    val printer = new PrettyPrinter(80,4)
    val formattedXml = printer.format(xmlNode)
    val output = new java.io.BufferedWriter(new java.io.FileWriter(targetFile));
    try {
      output.write( formattedXml );
    }
    finally {
      output.close();
    }
  }
  
  def main(args : Array[String]) : Unit = {
    val mapFile = new OziMap(new File("./image/hardangerviddaWest.map"))
    println(mapFile.imageFile)
    println(mapFile.name)
    println(mapFile.datum)
    println(mapFile.coordinateSystem)
    println(mapFile.projection)
    println(mapFile.referencePoints)
    
    writeXmlToFile(new File("./image/hardangerviddaWest.xml"), mapFile.toXml)
    
  }
  
  
}
