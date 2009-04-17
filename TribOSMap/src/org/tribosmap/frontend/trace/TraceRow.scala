package org.tribosmap.frontend.trace

import org.tribosmap.model.business.domain.Trace
import org.tribosmap.frontend.common.list.TribosListRow
import android.content.res.Resources
import android.graphics.drawable.{BitmapDrawable, Drawable}
import android.graphics.{BitmapFactory, Bitmap}
import org.tribosmap.model.math.distance.MetricUnit

/**
 * Objects of this class contains a Trace object and some data, 
 * used in the ListView of the TraceListActivity to
 * display the row. 
 * @param trace the Trace of the row
 * @param resources the global object to access resources
 * @author Meiko Rachimow
 */
protected[trace] class TraceRow (val trace : Trace, resources : Resources) 
  extends TribosListRow {
  
  /**
   * every trace has an icon(TODO: maybe we want different icons here)
   */
  private val traceDrawable = new BitmapDrawable(
    BitmapFactory.decodeResource(resources, R.drawable.trace_icon))
  
  /**
   * @see TribosListRow
   */
  val title : Option[String] = Some(trace.getName)
  
  /**
   * @see TribosListRow
   */
  val subtitle : Option[String] = Some(
    resources.getString(R.string.point_count_label) + " " + 
    trace.pointCount + System.getProperty("line.separator") +
    resources.getString(R.string.distance_label) + " " + 
    trace.distance.to(MetricUnit.KiloMeter) + System.getProperty("line.separator") +
    resources.getString(R.string.date_of_creation)+ " " + 
    (new java.util.Date(trace.creationTime)).toString
    
  )
  
  /**
   * @see TribosListRow
   */
  val icon : Option[Drawable] = Some(traceDrawable)
  
  /**
   * @see TribosListRow
   */
  val information : Option[String] = Some(trace.getInformation)
}
