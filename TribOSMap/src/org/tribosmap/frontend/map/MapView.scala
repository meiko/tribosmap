package org.tribosmap.frontend.map

import android.content.Context
import android.graphics.{BitmapShader, Shader, Rect, Canvas, Color}
import android.graphics.drawable.{ShapeDrawable, BitmapDrawable, Drawable}
import android.graphics.drawable.shapes.RectShape
import android.graphics.{BitmapFactory, Bitmap, Matrix}
import android.util.{AttributeSet, Log}
import android.view.View
import org.tribosmap.model.math._
import org.tribosmap.Preferences
import org.tribosmap.model.app.maptiling.MapTile

/**
 * the view for a map
 * @param context the Context
 * @param attrs the AttributeSet
 * @see View
 * @author Meiko Rachimow
 */
class MapView(val context : Context, 
              attrs : AttributeSet) 
  
  extends View(context, attrs) {
  
  def this(context : Context) = this(context, null)
  
  ////////////////////////////////////////////////////////
  //final attributes
  ////////////////////////////////////////////////////////
  private[this] lazy val windowSize = Vector2i(320, 430)
  private[this] lazy val countTilesInner = 3 //TODO: should depend on windowSize
  private[this] lazy val dimInnerX = countTilesInner * Preferences.tileSize
  private[this] lazy val dimensionInner = Vector2i(dimInnerX, dimInnerX)
  private[this] lazy val countTile = countTilesInner * countTilesInner
  private[this] lazy val zoomBitmap = GlobalObjectsForReuse.zoomBitmap
  private[this] lazy val zoomCanvas = GlobalObjectsForReuse.zoomCanvas
  private[this] lazy val actualPositionMarker = 
    new BitmapDrawable(BitmapFactory.decodeResource(
      getResources, R.drawable.marker_red))
  
  private[this] lazy val markerDrawable = 
    new BitmapDrawable(BitmapFactory.decodeResource(
      getResources, R.drawable.marker_green))
  
  protected[map] lazy val windowSizeD2 = windowSize/2
  
  protected[map] lazy val tiles = spiral(
    new MapTile(Vector2i(0,0)), 0, 4, 0).take(countTile).toList.reverse
  
  /**
   * Holds the Canvas for a bitmap - used to display scaled view, when zoomed
   * a bug in the android runtime will not free the memory of this bitmap
   * so we use the same every time when the MapView is instantiated
   */
  private object GlobalObjectsForReuse {
    lazy val zoomBitmap = Bitmap.createBitmap(
      windowSize.x, windowSize.y, Bitmap.Config.RGB_565)
    lazy val zoomCanvas = new Canvas(zoomBitmap)
  }
  
  ////////////////////////////////////////////////////////
  //State
  ////////////////////////////////////////////////////////
  
  private[this] var recalledTilesCount = 0
  private[this] var showMarker = true
  private[this] var actualPosition = Vector2i(0,0)
  private[this] var markersPositions = List[Vector2i]()
  
  ////////////////////////////////////////////////////////
  //methods called by the MapViewActivity
  ////////////////////////////////////////////////////////
    
  /**
   * the markers will be drawed at the given positions
   * @param i_actualPosition position of the actualPosition marker
   * @param i_markerPositions position of other markers
   */
  protected[map] def redraw(i_actualPosition: Vector2i, i_markerPositions: List[Vector2i]) {
    actualPosition = i_actualPosition
    markersPositions = i_markerPositions
    invalidate
  }
  
  /**
   * if a new tile has to be displayed,
   * this method has to be called
   * @param switchScale is true if the new tile has an other zoom level
   * @param tile the tile to draw
   */
  protected[map] def onNewTile(switchScale: Boolean, tile: MapTile) {
    if(switchScale) recalledTilesCount -= 1
    invalidate(tile.rect)
  }
  
  /**
   * if the map was used, this function has to be called
   */
  protected[map] def onMove() {
    recalledTilesCount = 0
  }
  
  /**
   * set the marker to be shown
   * @param show true and the markers will be shown
   */
  protected[map] def setShowMarker(show: Boolean) {
    showMarker = show
  }
  
  /**
   * set the scale of the mapView
   * @param scale the scale factor
   * 
   */
  protected[map] def setScale(scale: Float) {
    
    val zoomedSize = windowSize * scale
    val leftTop = (windowSize - zoomedSize) / 2
    val rightBottom = leftTop + zoomedSize
    
    val targetRect = new Rect(leftTop.x, leftTop.y, 
                              rightBottom.x, rightBottom.y)
    
    if(zoomedSize.x < windowSize.x) zoomCanvas.drawColor(Color.BLACK)
    zoomCanvas.drawBitmap(getDrawingCache(), null, targetRect, null) 
    recalledTilesCount = countTile
    
  } 
  
  ////////////////////////////////////////////////////////
  //overridden methods
  ////////////////////////////////////////////////////////
    
  /**
   * @see View
   */
  override def onAttachedToWindow {
    setDrawingCacheEnabled(true)
    setDrawingCacheBackgroundColor(Color.BLACK)
    setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW)
  }
  
  /**
   * @see View
   */
  override def onDetachedFromWindow {
    setDrawingCacheEnabled(false)
  }
  
  /**
   * @see View
   */
  override def onDraw(canvas : Canvas) {
    if(recalledTilesCount > 0){
      canvas.drawBitmap(zoomBitmap, canvas.getMatrix, null)
    }
    tiles.foreach(tile ⇒ {
      if(tile.bitmap != null) {
        canvas.drawBitmap(tile.bitmap, null, tile.rect, null) 
      }
    })
    
    if(showMarker)  {
      markersPositions.foreach( markerPosition ⇒ { 
        
        markerDrawable.setBounds(markerPosition.x, markerPosition.y - 20, 
                                 markerPosition.x + 20, markerPosition.y)
        markerDrawable.draw(canvas)
      })
    }
    
    actualPositionMarker.setBounds(actualPosition.x, actualPosition.y - 20, 
                                   actualPosition.x + 20, actualPosition.y)
    actualPositionMarker.draw(canvas)
  }
  
  
  ////////////////////////////////////////////////////////
  //private methods
  ////////////////////////////////////////////////////////
  
  /**
   * init all the maptiles and bring them into an order,
   * so that they are placed in a spiral
   * @param the initial (first/centered) MapTile
   * @param the actual maximum of maptiles for the actual row
   * @param the direction 0 east, 1 south, 2 west, 3 north
   * @param the actual count of tiles
   * @return the created MapTiles in a Stream
   */
  private[this] def spiral(tile : MapTile, actualMaxInRow : Int, 
                     direction : Int, count: Int): Stream[MapTile] = {
    
    val position = tile.position
    val newDirection = if(
      direction == 0 && position.x == actualMaxInRow ||
      direction == 1 && position.y == actualMaxInRow ||
      direction == 2 && position.x == -actualMaxInRow ||
      direction == 3 && position.y == -actualMaxInRow ){
        direction + 1
      } else {
        direction
    }
    Stream.cons(tile, 
      if (newDirection >= 4) {
        val posNew = Vector2i(-actualMaxInRow, -actualMaxInRow - 1)
        spiral(new MapTile(posNew), actualMaxInRow + 1,  0, count+1)
      } else {  
        val posNew = Vector2i(
          position.x + {
            if(newDirection == 0) 1 else if(newDirection == 2) -1 else 0
          }, 
          position.y + {
            if(newDirection == 1) 1 else if(newDirection == 3) -1 else 0
          })
        spiral(new MapTile(posNew), actualMaxInRow, newDirection, count+1)
     })
  }
}

