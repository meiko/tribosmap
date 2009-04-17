package org.tribosmap.frontend.common

import android.content.DialogInterface
import android.view.View

/**
 * We use a callback instead of implementing every time these stupid OnClickListeners
 * @param the callback function
 * @author Meiko Rachimow
 */
class OnClickAction(fkt: () ⇒ Any) extends 
  DialogInterface.OnClickListener with 
  View.OnClickListener{
  
  def this() = this(() ⇒ {})
  
  override def onClick(dialog:DialogInterface, whichButton:Int) {
    fkt()
  }
  
  override def onClick(v:View) {
    fkt()
  }
  
}
