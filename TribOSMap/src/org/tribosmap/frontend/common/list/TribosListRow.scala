package org.tribosmap.frontend.common.list

import android.graphics.drawable.Drawable

/**
 * used together with the ListActivity to display a row in the list
 * @author Meiko Rachimow
 */
trait TribosListRow {

  /**
   * title of the row
   */
  protected[list] val title : Option[String]

  /**
   * subtitle of the row
   */
  protected[list] val subtitle : Option[String]

  /**
   * icon of the row
   */
  protected[list] val icon : Option[Drawable]

  /**
   * information of the row
   */
  protected[list] val information : Option[String]
  
}
