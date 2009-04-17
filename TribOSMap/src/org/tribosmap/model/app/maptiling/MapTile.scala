package org.tribosmap.model.app.maptiling

import org.tribosmap.model.math._
import android.graphics.{Rect, Bitmap}

/**
 * represents a tile of the map
 * contains some informations about the tile
 * @param position the position in pixel
 * @author Meiko Rachimow
 */
class MapTile(val position: Vector2i) {

  //val pixelPosition = position * tileSize
  
  /**
   * the row of the tile
   */
  var row: Int = -1
  
  /**
   * the column of the tile
   */
  var col: Int = -1
  
  /**
   * the actual zoomlevel
   */
  var zoom: Int = -1
  
  /**
   * the uri to load the tile
   */
  var tileUri = ""
  
  /**
   * the rect to describe the position of the tile in the view (in pixel)
   */
  var rect = new Rect()
  
  /**
   * the image of the tile
   */
  var bitmap: Bitmap = null
}

