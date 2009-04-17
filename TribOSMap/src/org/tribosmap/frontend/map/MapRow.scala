package org.tribosmap.frontend.map

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.{BitmapDrawable, Drawable}
import android.graphics.{BitmapFactory, Bitmap}
import java.text.DecimalFormat

import org.tribosmap.model.app.maptiling._
import org.tribosmap.model.business.domain.GeoMap
import org.tribosmap.frontend.common.list.TribosListRow
import org.tribosmap.model.ServiceAccess

/**
 * Objects of this class contains a GeoMap object and some data, 
 * used in the ListView of the MapListActivity to
 * display the row. 
 * <p>
 * The MapRow is using a service to get some informations about his GeoMap.
 * (TODO:make the service unnecessary here)
 * @param map the GeoMap of the row
 * @param resources the global object to access resources
 * @param context the global object to access the application context (used to access a service)
 * @author Meiko Rachimow
 */
class MapRow(val map : GeoMap, resources : Resources, protected val context: Context) 
  extends TribosListRow with ServiceAccess {
  
  /**
   * every map has an icon(TODO: maybe we want different icons here)
   */
  private[this] val mapDrawable = 
    new BitmapDrawable(BitmapFactory.decodeResource(resources, R.drawable.map_icon))
  
  //the algorithm object of the GeoMap
  private[this] val algorithm: MapAlgorithm = 
    new OfflineUtmMap(map, map.getAllReferencedPixels)
  
    
  /**
   * @see TribosListRow
   */
  override val title : Option[String] = Some(map.getName)
  
  /**
   * @see TribosListRow
   */
  override val subtitle : Option[String] = {
    
    val leftTopCoord = algorithm.getLeftTopLatLon
    val rightBottomCoord = algorithm.getRightBottomLatLon
    val format = new DecimalFormat()
    format.setMaximumFractionDigits(5);
    val latLeftTopString = format.format(leftTopCoord.x) + "°N, "
    val lonLeftTopString = format.format(leftTopCoord.y) + "°E "
    val latRightBottomString = format.format(rightBottomCoord.x) + "°N, "
    val lonRightBottomString = format.format(rightBottomCoord.y) + "°E "
    
    Some(
      resources.getString(R.string.left_up) + ": " + latLeftTopString + lonLeftTopString +
        System.getProperty("line.separator") + 
      resources.getString(R.string.right_down) + ": " + latRightBottomString + lonRightBottomString +
        System.getProperty("line.separator") + 
      resources.getString(R.string.date_of_creation) + new java.util.Date(map.time).toString)
  }
  
  /**
   * @see TribosListRow
   */
  override val icon : Option[Drawable] = Some(mapDrawable)
  
  /**
   * @see TribosListRow
   */
  override val information : Option[String] = { 
    val format = new DecimalFormat()
    format.setMaximumFractionDigits(1);
    val mPerPixelString = format.format(algorithm.getMeterPerPixel) + 
      resources.getString(R.string.meter_per_pixel)
    Some(
      resources.getString(R.string.scale) + ": " + mPerPixelString + 
        System.getProperty("line.separator") +
      resources.getString(R.string.image_size) + ": " + map.imageSize.x + ", " + map.imageSize.y + 
        System.getProperty("line.separator")
      
    )
  }

}
