package org.tribosmap.converter

import java.io.File

object Converter {

  val standardQuality = 75
  val standardTileSize = 256
  val standardZoomLevel = 5
  
  def main(args : Array[String]) : Unit = {
    if(args.length < 1 || args(0) == "help") {
      println("use it: in out quality tileSize zoomLevel")
      exit
    }
    
    
    val startTime = System.currentTimeMillis
    val inputFileName = args(0)
    val targetFileName = args(1)
    
    
    val qality = if(args.length > 2) {
      try {
        args(2).toInt
      } catch {
        case _ => 
          println("no correct number given for quality : use standard")
          standardQuality
      }
    } else{ 
      standardQuality
    }
    
    val tileSize = if(args.length > 3) {
      try {
        args(3).toInt
      } catch {
        case _ => 
          println("no correct number given for tileSize : use standard")
          standardTileSize
      }
    } else{ 
      standardTileSize
    }
    
    val zoomLevel = if(args.length > 4) {
      try {
        args(4).toInt
      } catch {
        case _ => 
          println("no correct number given for zoomLevel : use standard")
          standardZoomLevel
      }
    } else{ 
      standardZoomLevel
    }
    
    val writer = new TileFileWriter(qality, tileSize)
    writer.createTiles(inputFileName, targetFileName, zoomLevel)
    
    //val reader = new TileReader(targetFileName)
    //for(z <- 0 to 4)
    //  reader.writeTileToFile("./image2/hardangerviddaWest" + z + ".jpg", 0,0, z)
    
    println(System.currentTimeMillis - startTime)
  }
}
