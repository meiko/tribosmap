package org.tribosmap.converter

class TileReader(dataFile: String) {


  def writeTileToFile(targetFile: String, row: Int, col: Int, zoomLevel: Int) {
    
    //1. zoom
    val dataInx = new java.io.DataInputStream(new java.io.FileInputStream(dataFile))
  
    val zoomLevelCount = dataInx.readInt
    val rowCounts = new Array[Int](zoomLevelCount)
    val colCounts = new Array[Int](zoomLevelCount)
    val headerLengths = new Array[Int](zoomLevelCount)
    val allZoomDataLengths = new Array[Array[Int]](zoomLevelCount)
    val headers = new Array[Array[Byte]](zoomLevelCount)

    for(zoom <- 0 until zoomLevelCount) {
      //2.1. rowCount
      rowCounts(zoom) = dataInx.readInt
      //2.2. colCount
      colCounts(zoom) = dataInx.readInt
      //2.3. headerLength
      headerLengths(zoom) = dataInx.readInt
    
      val allZoomDataLengthsBytes = new Array[Byte]((rowCounts(zoom) * colCounts(zoom)) * 4)
    
      //2.4. offsets
      dataInx.read(allZoomDataLengthsBytes)
      
      val offsetCount = allZoomDataLengthsBytes.length/ 4
      allZoomDataLengths(zoom) = new Array[Int](offsetCount)
      for(i <- 0 until offsetCount) {
        val startIndex = i*4
        allZoomDataLengths(zoom)(i) = 
          ((allZoomDataLengthsBytes(startIndex) & 0x000000FF) << 24) |
          ((allZoomDataLengthsBytes(startIndex + 1) & 0x000000FF) << 16) |
          ((allZoomDataLengthsBytes(startIndex + 2) & 0x000000FF) << 8) |
          ((allZoomDataLengthsBytes(startIndex + 3) & 0x000000FF) << 0)
      }
      
      //2.5. header
      headers(zoom) = new Array[Byte](headerLengths(zoom))
      dataInx.read(headers(zoom))
    }

    assume(zoomLevel < zoomLevelCount)
    val dataOut = new java.io.FileOutputStream(targetFile)
    dataOut.write(headers(zoomLevel))

    
    //println("colCounts(zoomLevel): " + colCounts(zoomLevel))
    val index = row * colCounts(zoomLevel) + col
    
    //println("index: " + index)
    
    //allZoomDataLengths.foreach(x => println(x.length))
    
    def skipBefore(indexMain: Int, indexChild: Int, sum: Int) : Int =  {
      
      val realIndex = 
      
      if(indexMain == zoomLevel && indexChild == index) return sum
      
      val newSum = allZoomDataLengths(indexMain)(indexChild) + sum
      val newIndexChild = indexChild + 1
      if(newIndexChild >= allZoomDataLengths(indexMain).length) {
        skipBefore(indexMain  + 1, 0, newSum)
      } else {
        skipBefore(indexMain, newIndexChild, newSum)
      }
    }
    
    val toSkip = skipBefore(0, 0, 0)
    //println("toSkip: " + toSkip)
    dataInx.skip(toSkip)
    

    val blocklength = allZoomDataLengths(zoomLevel)(index)
    
    //println("blocklength: " + blocklength)

    val blob = new Array[Byte](blocklength)
    dataInx.read(blob)
    dataOut.write(blob)
    
    dataOut.close
    dataInx.close
    
  }
  
}
