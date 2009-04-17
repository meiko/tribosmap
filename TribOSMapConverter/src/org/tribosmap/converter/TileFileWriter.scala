package org.tribosmap.converter

import java.io.File
import magick.{ImageInfo, MagickImage}

class TileFileWriter(quality: Int, tileSize: Int) {
  
  /*
   * convert an image to a tosm-file
   * 
   * the data format tosm will contain tiles of the source image
   * for different zoomLevel
   * 
   * 
   * @parameter source the source file, read from
   * @parameter target the target file, write to
   * @zoomLevelCount count of zoomLevels in the file
   */
  def createTiles(source: String, target: String, zoomLevelCount: Int) {
    
    val sourceFile = new File(source)
    val targetFile = new File(target)
    
    //tmpFile for tileData
    val tmpFile = new File(target + ".tmp")
    
    //tmpFile for converted imagefile
    val tmpConvertedName = source + ".jpg"
    
    //check preconditions
    assume(sourceFile.exists && sourceFile.canRead, "Cannot read file: " + sourceFile.getAbsolutePath)
    assume(!targetFile.exists || targetFile.canWrite, "Cannot open file for writing: " + targetFile.getAbsolutePath)
    assume(zoomLevelCount > 0, "zoomLevelCount has to be a number between 1 and n")
    assume(!tmpFile.exists || tmpFile.canWrite, "Cannot open Tmp file for writing: " + tmpFile.getAbsolutePath)
    
    println("start conversion: " + source + " to "+ target + " with " + zoomLevelCount + " zoom-levels")
    
    //create magickimage and info for the source file
    val (inputInfo, inputImage) = {
      
      val info = new ImageInfo(source)
      
      //if the source file is not a jpeg -> convert it to jpg
      if(!source.toLowerCase.endsWith("jpg")) {
        
        println("image is not a jpeg file, convert it to: " + tmpConvertedName)
        
        val tmpConverted = new File(tmpConvertedName)
        assume(!tmpConverted.exists || tmpConverted.canWrite, "Cannot open Tmp file for writing: " + tmpConverted.getAbsolutePath)
        
        val mn = new MagickImage(info)
        mn.setFileName(tmpConvertedName)
        mn.writeImage(info)
        val newInfo = new ImageInfo(tmpConvertedName)
        (newInfo, new MagickImage(newInfo))
      } else {
        (info, new MagickImage(info))
      }
    }
    
    println("set quality: " + quality)
    inputInfo.setQuality(quality)
    

    println("write tiles to the tempfile, and get the header-data for every zoomlevel")
    val allZoomDataLengths = new Array[Array[Int]](zoomLevelCount)
    val allZoomHeaders = new Array[Array[Byte]](zoomLevelCount)
    val tmpout = new java.io.FileOutputStream(tmpFile)
    
    val orgDim = inputImage.getDimension()
    val inWidth = orgDim.getWidth.toInt
    val inHeight = orgDim.getHeight.toInt
    
    def zoomedTileSize(zoomLevel: Int) = tileSize * (Math.pow(2, zoomLevel))
    
    for(zoomLevel <- 0 until zoomLevelCount){

      val scaledTileSize = zoomedTileSize(zoomLevel)
      
      val colCount = Math.ceil(inWidth.toDouble / scaledTileSize).toInt
      val rowCount = Math.ceil(inHeight.toDouble / scaledTileSize).toInt
      
      println("zoomLevel: " + zoomLevel + ", rowCount: " + rowCount + ", colCount: " + colCount)
   
      allZoomDataLengths(zoomLevel) = new Array[Int](rowCount * colCount)
      allZoomHeaders(zoomLevel) = null
      
      var offIndex = 0
      
      println("write " + rowCount * colCount + " tiles to tmp")
      for(row <- 0 until rowCount) { 
      
        for(col <- 0 until colCount) {
          val tile = splitImage(inputImage, Math.round(scaledTileSize.toFloat), row, col)
          
          val blob = tile.scaleImage(256, 256).imageToBlob(inputInfo)
          tmpout.write(blob)

          //get the smallest block of data at the beginning
          //, which is equal for all tiles of a zoomlevel
          allZoomDataLengths(zoomLevel)(offIndex) = blob.length
          offIndex += 1
          if(allZoomHeaders(zoomLevel) != null) {

            val length = Math.min(blob.length, allZoomHeaders(zoomLevel).length)

            var index = 0
            while(index < length) {
              if(allZoomHeaders(zoomLevel)(index) != blob(index)) {
                allZoomHeaders(zoomLevel) = blob.slice(0, index)
                index = length
              }
              index += 1
            }
          }else {
            allZoomHeaders(zoomLevel) = blob
          }
        }	
      }
    }
    tmpout.close
    println("image data tmpFile closed")
    
    
    //////////////
    //write data
      
    println("create outstream " + target)
    val fos = new java.io.FileOutputStream(target)
    val out = new java.io.DataOutputStream(fos)
    
    
    println("write metadata to target")
    //1. zoomlevelCount
    out.writeInt(zoomLevelCount)
    
    for(zoomLevel <- 0 until zoomLevelCount) {
      val scaledSize = zoomedTileSize(zoomLevel)
      val colCount = Math.ceil(inWidth.toDouble / scaledSize).toInt
      val rowCount = Math.ceil(inHeight.toDouble / scaledSize).toInt
      
      //2.1. rowCount
      out.writeInt(rowCount)
      
      //2.2. colCount
      out.writeInt(colCount)
      
      val headerLength = allZoomHeaders(zoomLevel).length
      //2.3. headerLength
      out.writeInt(headerLength)
      
      //2.4. offsets
	  allZoomDataLengths(zoomLevel).foreach(int => {
	    out.writeInt(int - headerLength)
	  })

      //2.5. header
	  allZoomHeaders(zoomLevel).foreach(byte => out.writeByte(byte.toChar))      
    }
    
    println("open data tmp file: " + tmpFile + " for writing")
    val tmpInstream = new java.io.FileInputStream(tmpFile)
    for(zoomLevel <- 0 until zoomLevelCount) {
      var i = 0
      while(i < allZoomDataLengths(zoomLevel).length) {
        val headerLength = allZoomHeaders(zoomLevel).length
        tmpInstream.skip(headerLength)
        val r = new Array[Byte](allZoomDataLengths(zoomLevel)(i) - headerLength)
        tmpInstream.read(r)
        
        //3. data
        fos.write(r)
        i += 1
      }
    }
    
    println("close target and tmp streams")
    
    fos.close
    out.close
    tmpInstream.close
    
    println("delete data tmp")
    tmpFile.delete
    
    val convertedFile = new File(tmpConvertedName)
    if (convertedFile.exists) {
      println("delete conveted image tmp file")
      convertedFile.delete
    }
  }
  
  /*
   * create a sequence of quadratic tiles
   * @parameter in the source image
   * @parameter tileSize the maximal size of the tiles
   * @return a sequence of tiles, rows containing the sequence of cols
   */
  def splitImage(in: MagickImage, tileSize: Int, row: Int, col: Int) : MagickImage = {

    val orgDim = in.getDimension()
    val inWidth = orgDim.getWidth.toInt
    val inHeight = orgDim.getHeight.toInt
    
    //println("splitImage " + row + ":" + col)
    val widthReal = (tileSize * (col + 1))
    val heightReal = (tileSize * (row + 1))
    val sizeDecX = if(widthReal > inWidth) widthReal - inWidth else 0
    val sizeDecY = if(heightReal > inHeight) heightReal - inHeight else 0
    in.cropImage(new java.awt.Rectangle(col * tileSize, row * tileSize, tileSize - sizeDecX, tileSize - sizeDecY));
  }
}
