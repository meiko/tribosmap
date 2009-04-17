package org.tribosmap.frontend.common

import android.app.Activity
import android.util.Log
import android.view.View
import android.content.DialogInterface

/**
 * show error-dialogs etc.
 * @author Meiko Rachimow
 */
trait ActivityHelper {

  this: Activity ⇒
  
  protected val context = this
  
  //global preferences
  lazy val preferences = new Preferences(this)
  
  //to use partial functions for onclicklisteners
  implicit def clickBtn(fun: () ⇒ Any) = new View.OnClickListener {
    def onClick(v:View){ fun() }
  }
  
  implicit def clickDlg(fun: () ⇒ Any) = new DialogInterface.OnClickListener{
    def onClick(v:DialogInterface, n: Int){ fun() }
  }

  /**
   * display an error-dialog, and write the error informations to the log
   * @param e the error
   */
  protected def displayError(error: Throwable) {
    
    Log.e(this.getClass.toString, 
          "error in activity: " + this.getClass + ", " +
          error.getMessage + ", " +
          error.getStackTraceString)
    val errorDialog = new ErrorDialog(this, error, finish _, ()⇒())
    errorDialog.show
    
  }
  
  /**
   * this method can be used to wrap another method by a try-catch statement
   * it will show an error-dialog if there was an error in the wrapped method
   * 
   * @param a function to wrap
   */
  protected def tryCatch[T <: Any](fun: ⇒ T): Option[T] = {
    try {
      Some(fun)
    } catch {
      case e: Throwable ⇒ 
        displayError(e)
        None
    }
  }
  
  /**
   * find a bound view, the view was declared in a layout xml file
   * @param id the id of the view (R.*)
   */
  protected def getView[T](id: Int) = findViewById(id).asInstanceOf[T]
  
  /**
   * find a bound view, the view was declared in a layout xml file
   * @param id the id of the view (R.*)
   */
  protected def getViews[T](ids: Int*): Seq[T] = for(id <- ids) yield findViewById(id).asInstanceOf[T]
}
