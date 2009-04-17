package org.tribosmap.frontend.marker

import org.tribosmap.model.business.domain.Marker
import org.tribosmap.frontend.common.list.TribosListRow
import android.content.res.Resources
import android.graphics.drawable.{BitmapDrawable, Drawable}
import android.graphics.{BitmapFactory, Bitmap}

/**
 * Objects of this class contains a Marker object and some data, 
 * used in the ListView of the MarkerListActivity to
 * display the row. 
 * @param marker the Marker of the row
 * @param resources the global object to access resources
 * @author Meiko Rachimow
 */
protected[marker] class MarkerRow(val marker : Marker, resources : Resources) 
  extends TribosListRow {
  
  /**
   * every marker has an icon(TODO: maybe we want different icons here)
   */
  private[this] val markerDrawable = new BitmapDrawable(
    BitmapFactory.decodeResource(resources, R.drawable.marker_red))
  
  /**
   * @see TribosListRow
   */
  override val title : Option[String] = Some(marker.getName)
  
  /**
   * @see TribosListRow
   */
  override val subtitle : Option[String] = Some(
      resources.getString(R.string.position_short_label) + " " + 
      marker.getPosition + System.getProperty("line.separator") +
      resources.getString(R.string.date_of_creation)+ " " + 
      (new java.util.Date(marker.creationTime)).toString)

  /**
   * @see TribosListRow
   */
  override val information : Option[String] = Some(
    resources.getString(R.string.position_short_label_utm) + " " + 
    marker.getPosition.utm + System.getProperty("line.separator") + 
    marker.getInformation)
  
  /**
   * @see TribosListRow
   */
  override val icon : Option[Drawable] =  {
    if(marker.getPhotoFileName != null && 
       new java.io.File(marker.getPhotoFileName).canRead) {
      val fullSizeBmp = BitmapFactory.decodeFile(marker.getPhotoFileName)
      Some(new BitmapDrawable(Bitmap.createScaledBitmap(fullSizeBmp, 40, 40, true)))
    } else {
      Some(markerDrawable)
    }
  }
}
