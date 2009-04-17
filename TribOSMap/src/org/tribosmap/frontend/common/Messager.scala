package org.tribosmap.frontend.common

import android.widget.Toast
import android.content.Context

/**
 * Can be used to display a short message onto the screen,
 * it will hide the last shown message.
 * It acts like a singleton. So it is not possible to 
 * display more then one Toast-Views at the time.
 * @author Meiko Rachimow
 */
object Messager {
  
  //the last shown toast message object
  private[this] var toast: Toast = null
  
  /**
   * display a message for long time onto the screen,
   * by using this toast-view
   * after calling this method, it will be displayed
   * 
   * @param the actual Context
   * @param a message to display
   * 
   * @return the msg view (a Toast object)
   */
  def makeMessage(context: Context, message: String): Toast = {
    makeMessage(context, message, Toast.LENGTH_LONG)
  }
  
  /**
   * display a message for short time onto the screen,
   * by using this toast-view,
   * after calling this method, it will be displayed
   * 
   * @param the actual Context
   * @param a message to display
   * 
   * @return the msg view (a Toast object)
   */
  def makeMessageShort(context: Context, message: String): Toast = {
    makeMessage(context, message, Toast.LENGTH_SHORT)
  }
    /**
   * display a message for long time onto the screen,
   * by using this toast-view
   * after calling this method, it will be displayed
   * 
   * @param the actual Context
   * @param a message to display
   * @param duration the Duration to display this toast
   * 
   * @return the msg view (a Toast object)
   */
  def makeMessage(context: Context, message: String, duration: Int): Toast = {
    if(toast != null) toast.cancel
    toast = Toast.makeText(context, message, duration)
    toast.show
    toast
  }
  
}
