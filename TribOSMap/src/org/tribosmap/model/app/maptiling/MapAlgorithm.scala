package org.tribosmap.model.app.maptiling

import org.tribosmap.model.app.imageloader.ImageLoadingJob;
import org.tribosmap.model.math.geographic._
import org.tribosmap.model.math._
import android.graphics.Bitmap

/**
 * a trait for different computations needed by the MapView
 * to work with different types of maps. 
 * @author Meiko Rachimow
 */
trait MapAlgorithm {
  
  /**
   * the Datum used in the Map projection
   */
  def datum: Datum
  
  /**
   * the minimum zoom level (smallest view)
   */
  def initialZoom: Int
  
  /**
   * called by the MapView when the user zoomed in the map
   * @return tuple of the scaling factor between befor and after the zoom and the new zoomLevel
   */
  def zoomIn(oldZoom: Int): Tuple2[Float, Int]
  
  /**
   * called by the MapView when the user zoomed out the map
   * @return tuple of the scaling factor between befor and after the zoom and the new zoomLevel
   */
  def zoomOut(oldZoom: Int): Tuple2[Float, Int]
  
  /**
   * @return the meter per pixel for the actual zoomlevel
   */
  def getMeterPerPixel : Double
  
  /**
   * @return the geographic coordinate of the left-top point of the whole map
   */
  def getLeftTopLatLon : GeographicCoOrdinatePair
  
  /**
   * @return the geographic coordinate of the right-bottom point of the whole map
   */
  def getRightBottomLatLon : GeographicCoOrdinatePair
  
  /**
   * computes a new position for a given point,
   * after moving the map by a Vector (holding meter-values)
   * @param zoom the ZoomLevel
   * @param latLon the old point
   * @param moveVector the move-vector containing meter values
   * @return the geographic coordinate of the new position
   */
  def computeLatLonAfterMove(zoom:Int, 
                             latLon: GeographicCoOrdinatePair, 
                             moveVector: Vector2d) : GeographicCoOrdinatePair
  
  /**
   * this method is used by the MapView to calculate
   * the Position of a geographic Coordinate in the View
   * @param zoom the ZoomLevel
   * @param latLon the geographic coordinate
   * @param centerLatLon the actual geographic coordinate of the center of the view
   * @param windowSizeD2 the half-size of the window (window.size / 2)
   */
  def getWindowPositionFor(zoom:Int, 
                           latLon: GeographicCoOrdinatePair, 
                           centerLatLon: GeographicCoOrdinatePair, 
                           windowSizeD2: Vector2i): Vector2i
  
  /**
   * called from the MapView to get a List of all MapTiles,
   * which have to be displayed in the View.
   * @param zoom the ZoomLevel
   * @param centerLatLon the geographic coordinate of the center of the view
   * @param windowSizeD2 the windowsize
   * @param tiles the old Tiles
   * @return tile the actual MapTiles to be displayed in the MapView
   */
  def newPosition(zoom:Int, 
                  centerLatLon: GeographicCoOrdinatePair, 
                  windowSizeD2: Vector2i, 
                  tiles: List[MapTile]):  List[MapTile]
  
  /**
   * called by the MapView when a new ImageLoadingJob will be created.
   * @param tile the MapTile to load
   * @param the callBack - called after loading the image
   * @param preferences
   */
  def createJob(tile: MapTile, 
                callBack: (Bitmap) ⇒ Unit, 
                preferences: Preferences) : Option[ImageLoadingJob]
}

