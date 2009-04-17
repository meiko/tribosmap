package org.tribosmap.model.app

/**
 * This class is used to read a tosm file,
 * and provide the fetched information and data.
 * <p>
 * the informations of the map (every array has elements of count zoomLevelCount)<br>
 * dataOffsets the offsets in the file to the sparate tiles for each zoomlevel<br>
 * dataLengths the lengths in bytes of the tiles for each zoomlevel<br>
 * dataHeaders the headers of the tiles for each zoomlevel<br>
 * dataColCounts the count of columns for each zoomlevel<br>
 * dataRowCounts the count of rows for each zoomlevel<br>
 * zoomLevelCount the count of zoomlevels<br>
 * <br>
 * @param mapFileName the path of the tosm file
 * @author Meiko rachimow
 */
class MapFileReader(mapFileName: String) {
  
  private val dataInx = new java.io.DataInputStream(
    new java.io.FileInputStream(mapFileName))
  
  /**
   * the informations of the map (every array has elements of count zoomLevelCount)
   * dataOffsets the offsets in the file to the sparate tiles for each zoomlevel
   * dataLengths the lengths in bytes of the tiles for each zoomlevel
   * dataHeaders the headers of the tiles for each zoomlevel
   * dataColCounts the count of columns for each zoomlevel
   * dataRowCounts the count of rows for each zoomlevel
   * zoomLevelCount the count of zoomlevels
   */
  val (dataOffsets, 
       dataLengths, 
       dataHeaders, 
       dataColCounts, 
       dataRowCounts, 
       zoomLevelCount) = try {
    
    val startAvailable = dataInx.available
    
    val zoomLevelCount = dataInx.readInt
    val rowCounts = new Array[Int](zoomLevelCount)
    val colCounts = new Array[Int](zoomLevelCount)
    val headerLengths = new Array[Int](zoomLevelCount)
    val allZoomDataLengths = new Array[Array[Int]](zoomLevelCount)
    val headers = new Array[Array[Byte]](zoomLevelCount)
      
    for(zoom ← 0 until zoomLevelCount) {
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
      for(i ← 0 until offsetCount) {
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
    
    var pos = startAvailable - dataInx.available
    val tileFileOffsets = new Array[Array[Array[Long]]](zoomLevelCount)
    
    for(z ← 0 until zoomLevelCount) {
      tileFileOffsets(z) = new Array[Array[Long]](rowCounts(z))
      for(row ← 0 until rowCounts(z)) {
        tileFileOffsets(z)(row) = new Array[Long](colCounts(z))
        for(col ← 0 until colCounts(z)) {
          tileFileOffsets(z)(row)(col) = pos
          val i = row * colCounts(z) + col
          val dataLength = allZoomDataLengths(z)(i)
          pos = pos + dataLength
        }
      }
    }
    
    (tileFileOffsets, allZoomDataLengths, headers, colCounts, rowCounts, zoomLevelCount)
    
  } finally {
    dataInx.close
  }
}

