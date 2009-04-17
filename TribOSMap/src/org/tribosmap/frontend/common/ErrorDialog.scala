package org.tribosmap.frontend.common

import android.content.Context
import android.app.AlertDialog
import android.view.View
import android.widget.{TextView, ImageButton}

/**
 * @author Meiko Rachimow
 */
class ErrorDialog(context: Context, error: Throwable,
                  quitAction: ()⇒ Unit, okayAction: ()⇒ Unit) 
  extends AlertDialog(context) {

    val view = getLayoutInflater.inflate(R.layout.error_dialog, null);
    val errorTextView = view.findViewById(R.id.errortext).asInstanceOf[TextView]
    
    val stackTraceBtn = view.findViewById(R.id.stacktracebtn).asInstanceOf[ImageButton]
    stackTraceBtn.setVisibility(View.VISIBLE)
    
    stackTraceBtn.setOnClickListener( new OnClickAction( () ⇒ {
      errorTextView.setText(error.getStackTraceString)
      stackTraceBtn.setEnabled(false)
      stackTraceBtn.setVisibility(View.GONE)
      view.requestLayout
    }))

    setTitle("Error: " + error.getMessage + 
               "(reported by: " + error.getClass.getSimpleName + ")")
    setButton("Quit", new OnClickAction(quitAction))
    setButton2("Okay",new OnClickAction(okayAction))
    setView(view)
    view.requestLayout
}
