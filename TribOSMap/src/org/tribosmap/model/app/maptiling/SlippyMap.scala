package org.tribosmap.model.app.maptiling

import java.lang.Math._

import org.tribosmap.model.app.imageloader.OnlineImageLoadingJob;
import org.tribosmap.model.math._
import org.tribosmap.model.math.geographic._
import android.graphics.{Rect, Bitmap}

/**
 * The SlippyMap is used with the online Openstreetmap Map.
 * @author Meiko Rachimow
 */
class SlippyMap extends MapAlgorithm {

  ////////////////////////////////////////////////////////
  //private values
  ////////////////////////////////////////////////////////
  
  val initialZoom = 16  
  
  private[this] val serverUri = "http://tile.openstreetmap.org/"
  
  private[this] val tileSize = Preferences.tileSize

  private[this] def tile2latlon(tilePos: Vector2d, z: Int) = 
    GeographicCoOrdinatePair(
      mercatorToLat(PI * (1.0 - 2.0 * tilePos.y / numTiles(z))), 
      tilePos.x / numTiles(z) * 360.0 - 180.0)

  private[this] def xy2latlon(pos : Vector2d, z : Int) = 
    tile2latlon( pos / tileSize, z)

  private[this] def latlon2tileDouble(latLon: GeographicCoOrdinatePair, z: Int) = 
    Vector2d(
      (latLon.y + 180.0) / 360.0 * numTiles(z),
      (1 - log(tan(toRadians(latLon.x)) + 1 / cos(toRadians(latLon.x))) / PI) / 2.0 * numTiles(z))
  
  private[this] def latlon2tile(latLon: GeographicCoOrdinatePair, z: Int) = 
    latlon2tileDouble(latLon, z).toVector2iFloor
  
  private[this] def latlon2xy(latLon: GeographicCoOrdinatePair, z: Int) = 
    latlon2tileDouble(latLon, z) * tileSize


  ////////////////////////////////////////////////////////
  //public methods
  ////////////////////////////////////////////////////////

  /**
   * @see MapAlgorithm
   */
  def zoomOut(oldZoom: Int) = {
    
    val zoom = if(oldZoom > 0) {
      oldZoom - 1
    } else {
      oldZoom
    }
    (numTiles(zoom).toFloat / numTiles(oldZoom).toFloat, zoom)
  }

  /**
   * @see MapAlgorithm
   */
  def zoomIn(oldZoom: Int) = {
    
    val zoom = if(oldZoom < 18) {
      oldZoom + 1
    } else {
      oldZoom
    }
    (numTiles(zoom).toFloat / numTiles(oldZoom).toFloat, zoom)
  }

  /**
   * @see MapAlgorithm
   */
  def computeLatLonAfterMove(zoom:Int, 
                             oldCenterLatLon: GeographicCoOrdinatePair, 
                             moveVector: Vector2d) = {
    
    val oldCenter = latlon2xy(oldCenterLatLon, zoom)
    val mapDimension = numTiles(zoom) * tileSize
    val center = (oldCenter + moveVector) % mapDimension
    val x = if(center.x < 0) mapDimension + center.x else center.x
    val y = if(center.y < 0) mapDimension + center.y else center.y
    xy2latlon(Vector2d(x,y), zoom)
  }

  /**
   * @see MapAlgorithm
   */
  def getMeterPerPixel = {
    //not implemented yet
    0
  }

  /**
   * @see MapAlgorithm
   */
  def getLeftTopLatLon : GeographicCoOrdinatePair =  {
    //not implemented yet
    GeographicCoOrdinatePair(0,0)
  }

  /**
   * @see MapAlgorithm
   */
  def getRightBottomLatLon : GeographicCoOrdinatePair = {
    //not implemented yet
    GeographicCoOrdinatePair(0,0)
  }

  /**
   * @see MapAlgorithm
   */
  def getWindowPositionFor(zoom:Int, 
                           latLon: GeographicCoOrdinatePair, 
                           centerLatLon: GeographicCoOrdinatePair, 
                           windowSizeD2: Vector2i): Vector2i = {
    
    val mapCenter = latlon2xy(centerLatLon, zoom).toVector2iFloor
    val mapPixel = latlon2xy(latLon, zoom).toVector2iFloor
    windowSizeD2 - (mapCenter - mapPixel)
  }

  /**
   * @see MapAlgorithm
   */
  def newPosition(zoom:Int, 
                  centerLatLon: GeographicCoOrdinatePair, 
                  windowSizeD2: Vector2i, 
                  tiles: List[MapTile]):  List[MapTile] = {
    
    val mapCenter = latlon2xy(centerLatLon, zoom)
    val centerColRow = (mapCenter / tileSize).toVector2iFloor   
    val leftTop = windowSizeD2 - (mapCenter.toVector2iFloor % tileSize)
    
    val tileDrawables = Map(tiles.map(tile ⇒ (tile.tileUri, tile.bitmap)):_*)

    var result: List[MapTile] = List()
    tiles.foreach(tile ⇒ {
      
      val posPixelLeftTop = leftTop + tile.position * tileSize
      val cr = (centerColRow + tile.position) % numTiles(zoom)
      val crx = if(cr.x < 0) numTiles(zoom) + cr.x else cr.x
      val cry = if(cr.y < 0) numTiles(zoom) + cr.y else cr.y
    
      val tileUri = zoom + "/" + crx + "/" + cry + ".png"
      
      val rect = new Rect(posPixelLeftTop.x, 
                          posPixelLeftTop.y, 
                          posPixelLeftTop.x + tileSize, 
                          posPixelLeftTop.y + tileSize)
      
      tile.tileUri = tileUri
      tile.rect = rect
      
      tileDrawables.get(tile.tileUri) match {
        case Some(bitmap: Bitmap) ⇒ 
          tile.bitmap = bitmap
        case _ ⇒  result = tile :: result
      }
    })
    result
  }
  
  def datum = Datum.WGS84Datum
  
  /**
   * @see MapAlgorithm
   */
  def createJob(tile: MapTile, 
                callBack: (Bitmap) ⇒ Unit, 
                preferences: Preferences) = {
 
    Some(new OnlineImageLoadingJob(
      tile.tileUri, serverUri, tile.tileUri, callBack, preferences.webCacheFolder))
  }
  
  ////////////////////////////////////////////////////////
  //private methods
  ////////////////////////////////////////////////////////
    
  /**
   * returns S,W,N,E
   */
  private[this] def tileEdges(x : Double, y : Double, z : Int) : 
    Tuple4[Double, Double, Double, Double] = {
      
    val latitudes = latEdges(y,z)
    val longitudes = lonEdges(x,z)
    (latitudes.y, longitudes.x, latitudes.x, longitudes.y)
  }
  
  private[this] def latEdges(y : Double, z : Int) = {
    val unit = 1 / numTiles(z)
    val relY1 = y * unit
    val relY2 = relY1 + unit
    val lat1 = mercatorToLat(PI * (1 - 2 * relY1))
    val lat2 = mercatorToLat(PI * (1 - 2 * relY2))
    GeographicCoOrdinatePair(lat1,lat2)
  }

  private[this] def lonEdges(x : Double, z : Int) = {
    val unit = 360 / numTiles(z)
    val lon1 = -180 + x * unit
    val lon2 = lon1 + unit
    GeographicCoOrdinatePair(lon1,lon2)
  }
  
  private[this] def mercatorToLat(mercatorY : Double) = 
    toDegrees(atan(sinh(mercatorY)))
  
  private[this] def numTiles(zoom:Int) = 1<<zoom
  
  private[this] def dimension(zoom:Int) = numTiles(zoom) * tileSize
  
  private[this] def mapDimension(zoom: Int) = {
    val dimension = numTiles(zoom) * tileSize
    Vector2i(dimension, dimension)
  }
}
