package org.tribosmap.model.app.maptiling

import java.lang.Math._
import android.graphics.{Bitmap, Rect}
import org.tribosmap.model.app.imageloader.OfflineImageLoadingJob;
import org.tribosmap.model.math._
import org.tribosmap.model.math.geographic._
import org.tribosmap.model.business.domain.{GeoMap, GeoReferencedPixel}
import org.tribosmap.model.app.MapFileReader

/**
 * The OfflineUtmMap is used with offline Maps (Format: UTM/Wgs84).
 * @author Meiko Rachimow
 */
class OfflineUtmMap(map: GeoMap, georeferencedPixels: List[GeoReferencedPixel])
  extends MapAlgorithm {

  ////////////////////////////////////////////////////////
  //private values
  ////////////////////////////////////////////////////////
    
  val initialZoom = 0
  
  private[this] val mapDimension = map.imageSize
  private[this] val mapFileName = map.fileName
  
  //only one zone for the map is supported
  private[this] val zone = georeferencedPixels(0).utm.zoneNumber
  private[this] val hemisphere = georeferencedPixels(0).utm.hemisphere
  
  //sort for getting positive directions for difference vectors
  private[this] val sortedPoints = georeferencedPixels.sort(
    (p1,p2) ⇒ p1.pixel.x < p2.pixel.x || p1.pixel.x == 
              p2.pixel.x && p1.pixel.y < p2.pixel.y
  )
    
  //compute alpha (the angle of the utm raster)
  private[this] val alpha = {
    val ans = for(i ← 0 until sortedPoints.length - 1; 
                        j ← i+1 until sortedPoints.length) yield {
      val p1 = sortedPoints(i)
      val p2 = sortedPoints(j)
      val pixelVectorLength = (p2.pixel - p1.pixel).toVector2d.length
      val utmVector = Vector2d(p2.utm.eastingD - p1.utm.eastingD, 
                             p2.utm.northingD - p1.utm.northingD)
      val utmVectorLength = utmVector.length
      val oppositeLeg = -p2.pixel.y + p1.pixel.y
      if(p2.utm.eastingD / p1.utm.eastingD < 1) {
        //p2-x greater the p1-x → we have to change the angle direction
        Math.Pi + 
          Math.asin(oppositeLeg / pixelVectorLength) + 
          Math.asin(utmVector.y / utmVectorLength)
      } else {
        Math.asin(oppositeLeg / pixelVectorLength) - 
          Math.asin(utmVector.y / utmVectorLength)
      }
    }
    ans.reduceLeft((sum, x) ⇒ sum + x) / ans.length
  }
  
  private[this] val mapFileReader = new MapFileReader(mapFileName)
  
  private[this] val zoomLevelCount = mapFileReader.zoomLevelCount
  
  private[this] val mPerPixelList = for(i ← 0 until georeferencedPixels.length - 1;
                          j ← i+1 until georeferencedPixels.length) yield {
    
    val deltaPixel = (georeferencedPixels(i).pixel - 
                        georeferencedPixels(j).pixel).toVector2d.length
    
    val deltaUtmMeter = Vector2d(georeferencedPixels(i).utm.eastingD - 
                                 georeferencedPixels(j).utm.eastingD, 
                                 georeferencedPixels(i).utm.northingD - 
                                 georeferencedPixels(j).utm.northingD).length
    deltaUtmMeter / deltaPixel
  }
  
  private[this] val mPerPixel = {
    mPerPixelList.reduceLeft((r,v) ⇒ r+v) / mPerPixelList.length
  }
  
  //compute translation of the pixel coordinates origin)
  private[this] val cosA = Math.cos(alpha)
  
  private[this] val sinA = Math.sin(alpha)
  
  private[this] val origins = for(i ← 0 until sortedPoints.length - 1) yield {
    val putm = Vector2d(sortedPoints(i).utm.eastingD, 
                        sortedPoints(i).utm.northingD)
    val normalized = sortedPoints(i).pixel.toVector2d.normalize
    val xP = normalized.x
    val yP = -normalized.y
    val rotX = cosA * xP + sinA * yP
    val rotY = cosA * yP - sinA * xP
    val pputm = Vector2d(rotX, rotY) * 
                (sortedPoints(i).pixel.toVector2d.length * mPerPixel)
    putm - pputm
  }
  private[this] val originPixelInUtm = 
    origins.reduceLeft((sumVector, v) ⇒ sumVector + v) / origins.length

  private[this] val tileSize = Preferences.tileSize
  
  private[this] val converter = new CoordConverter(map.datum)
  
  ////////////////////////////////////////////////////////
  //public methods
  ////////////////////////////////////////////////////////
    
  /**
   * @see MapAlgorithm
   */
  def zoomOut(oldZoom: Int) = if(oldZoom < zoomLevelCount -1) {
    (0.5f, oldZoom + 1)
  } else {
    (1.0f, oldZoom)
  }

  /**
   * @see MapAlgorithm
   */
  def zoomIn(oldZoom: Int) = if(oldZoom > 0) {
    (2.0f, oldZoom - 1)
  } else {
    (1.0f, oldZoom)
  }

  /**
   * @see MapAlgorithm
   */
  def getMeterPerPixel = mPerPixel

  /**
   * @see MapAlgorithm
   */
  def getLeftTopLatLon : GeographicCoOrdinatePair =  {
    val leftTopUtm = new UtmCoOrdinates(
      originPixelInUtm.y, originPixelInUtm.x, zone, hemisphere, map.datum)
    converter.utmToLatLon(leftTopUtm)
  }

  /**
   * @see MapAlgorithm
   */
  def getRightBottomLatLon : GeographicCoOrdinatePair = {
    val northEastUtmRightBottom = utmforPixel(initialZoom, mapDimension.toVector2d)
    val rightBottomUtm = new UtmCoOrdinates(
      northEastUtmRightBottom.y, northEastUtmRightBottom.x, zone, hemisphere, map.datum)
    converter.utmToLatLon(rightBottomUtm)
  }

  /**
   * @see MapAlgorithm
   */
  def getWindowPositionFor(zoom:Int, 
                           latLon: GeographicCoOrdinatePair, 
                           centerLatLon: GeographicCoOrdinatePair, 
                           windowSizeD2: Vector2i): Vector2i = {
    val mapCenter = pixelForUtm( zoom,
      converter.latLonToUtm(centerLatLon)).toVector2iFloor
    
    val mapPixel = pixelForUtm( zoom,
      converter.latLonToUtm(latLon)).toVector2iFloor
    
    windowSizeD2 - (mapCenter -mapPixel)
  }

  /**
   * @see MapAlgorithm
   */
  def createJob(tile: MapTile, 
                callBack: (Bitmap) ⇒ Unit, 
                preferences: Preferences) = {
    
    if(tile.row >= 0 && tile.col >= 0 && 
       tile.row < mapFileReader.dataRowCounts(tile.zoom) && 
       tile.col < mapFileReader.dataColCounts(tile.zoom)) {
      
      val index = tile.row * mapFileReader.dataColCounts(tile.zoom) + tile.col

      Some(
        new OfflineImageLoadingJob(
          tile.tileUri, 
          mapFileName, 
          mapFileReader.dataHeaders(tile.zoom): Array[Byte], 
          mapFileReader.dataOffsets(tile.zoom)(tile.row)(tile.col): Long, 
          mapFileReader.dataLengths(tile.zoom)(index): Int,
          callBack)
      )
    } else {
      None
    }
  }
  
  def datum = map.datum
  
  
  /**
   * @see MapAlgorithm
   */
  def computeLatLonAfterMove(zoom: Int, oldCenterLatLon: GeographicCoOrdinatePair, 
                             moveVector: Vector2d) = {
    
    val utmCoord = converter.latLonToUtm(oldCenterLatLon)
    assert(zone == utmCoord.zoneNumber)
    val pixel = pixelForUtm(zoom, utmCoord)
    val newPixel = pixel + moveVector
    val coord = utmforPixel(zoom, newPixel)
    converter.utmToLatLon(
      new UtmCoOrdinates(coord.y, coord.x, zone, hemisphere, map.datum))
  }

  /**
   * @see MapAlgorithm
   */
  def newPosition(zoom:Int, 
                  centerLatLon: GeographicCoOrdinatePair, 
                  windowSizeD2: Vector2i, tiles: List[MapTile]):  List[MapTile] = {
    
    val centerUtm = converter.latLonToUtm(centerLatLon)
    val mapCenter = pixelForUtm(zoom, centerUtm)
    val centerColRow = (mapCenter / tileSize).toVector2iFloor
    val leftTop = windowSizeD2 - (mapCenter.toVector2iFloor % tileSize)
    val tileDrawables = Map(tiles.map(tile ⇒ (tile.tileUri, tile.bitmap)):_*)

    var result: List[MapTile] = List()
    tiles.foreach(tile ⇒ {
      val cr = (centerColRow + tile.position)
      val posPixelLeftTop = leftTop + tile.position * tileSize
      val rect = new Rect(posPixelLeftTop.x, 
                          posPixelLeftTop.y, 
                          posPixelLeftTop.x + tileSize, 
                          posPixelLeftTop.y + tileSize)
      tile.zoom = zoom
      tile.row = cr.y
      tile.col = cr.x
      tile.tileUri = mapFileName + "-" + zoom + "-" + 
                     tile.row + "-" + tile.col
      tile.rect = rect
      if(cr.y >= 0 && cr.x >= 0) {
        tileDrawables.get(tile.tileUri) match {
          case Some(bitmap: Bitmap) ⇒ 
            tile.bitmap = bitmap
          case _ ⇒  result = tile :: result
        }
      } else {
        tile.bitmap = null
      }
    })
    result
  }
  
  ////////////////////////////////////////////////////////
  //private methods
  ////////////////////////////////////////////////////////
    
  private[this] def pixelForUtm(zoom:Int, utmCoord: UtmCoOrdinates): Vector2d = {
    val scaledMperPixel = mPerPixel * (1 << zoom)
    val utmCoordVector = Vector2d(utmCoord.eastingD, utmCoord.northingD)
    val translatedToOrigin = 0
    val d = utmCoordVector - originPixelInUtm
    val vecPixelLength = d.length / scaledMperPixel
    val dNorm = d.normalize
    val xP = dNorm.x
    val yP = -dNorm.y
    val rotX = cosA * xP + sinA * yP
    val rotY = cosA * yP - sinA * xP
    Vector2d(rotX, rotY) * vecPixelLength
  }
  
  private[this] def utmforPixel(zoom:Int, pixel: Vector2d) = {
    
    val scaledMperPixel = mPerPixel * (1 << zoom)
    val vecUtmLength = pixel.length * scaledMperPixel
    val pNorm = pixel.normalize
    val xP = pNorm.x
    val yP = -pNorm.y
    val rotX = cosA * xP + sinA * yP
    val rotY = cosA * yP - sinA * xP
    Vector2d(rotX, rotY) * vecUtmLength + originPixelInUtm
  }
  
}
