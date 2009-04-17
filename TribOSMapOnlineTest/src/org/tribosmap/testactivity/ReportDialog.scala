package org.tribosmap.testactivity

import android.content.Context
import android.app.AlertDialog
import android.view.View
import android.widget.{TextView, ImageButton}
import org.scalatest.Report
                      
private[testactivity] class ReportDialog(context: Context, report: Report) 
  extends AlertDialog(context) {

    private[this] val status = new StringBuffer
    
    private[this] val view = 
      getLayoutInflater.inflate(R.layout.error_dialog, null);
    
    private[this] val errorTextView = 
      view.findViewById(R.id.errortext).asInstanceOf[TextView]
    
    private[this] val stackTraceBtn = 
      view.findViewById(R.id.stacktracebtn).asInstanceOf[ImageButton]
    
    private[this] val testName = 
      report.name.substring(report.name.findIndexOf(_ == ':') + 2)        
    
    
    report.throwable match {
      case Some(throwable) ⇒ {
        status.append( "FAIL: " + testName + System.getProperty("line.separator") )
        val status2 = new StringBuffer
        status2.append( report.message + System.getProperty("line.separator") )
        status2.append( throwable.getMessage + System.getProperty("line.separator") )
        status2.append( throwable.getStackTraceString + System.getProperty("line.separator"))
        stackTraceBtn.setVisibility(View.VISIBLE)
        stackTraceBtn.setOnClickListener( new View.OnClickListener() {
          def onClick(v: View) {
            errorTextView.setText(new String(status2))
            stackTraceBtn.setEnabled(false)
            stackTraceBtn.setVisibility(View.GONE)
            view.requestLayout
          }
        })
      }
      case None ⇒
        stackTraceBtn.setVisibility(View.INVISIBLE)
        status.append( "OKAY: " + testName + System.getProperty("line.separator") )
        status.append( report.message + System.getProperty("line.separator") )
    }
    
    setTitle(new String( status ))
    setView(view)
    view.requestLayout
}
