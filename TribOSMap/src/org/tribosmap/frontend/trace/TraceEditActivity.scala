package org.tribosmap.frontend.trace

import android.app.AlertDialog
import android.app.Activity
import android.util.Log
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.ImageButton
import org.tribosmap.model.business.domain.Trace
import org.tribosmap.frontend.common.{ActivityHelper, Messager}
import org.tribosmap.model.math.distance.MetricUnit
import org.tribosmap.model.ServiceAccess

/**
 * This Activity is used to edit a Trace object, it has fields for the name etc.
 * <p>
 * It needs a Trace object in the init bundle.<br/>
 * The Key for that Trace in the bundle is: <code>TraceComCodes.TRACE_PARAMETER_ID</code><br/>
 * This activity is using the TraceService, to save the edited Trace-object in the repository.<br/>
 * The resultcode of this Activity after closing, can be:<br/>
 * <code>Activity.RESULT_OK</code> if the Trace was saved (changed),<br/>
 * or <br/>
 * <code>Activity.RESULT_CANCELED</code> if the Trace wasn't saved.<br/>
 * @author Meiko Rachimow
 */
class TraceEditActivity extends Activity 
  with ActivityHelper 
  with ServiceAccess {
  
  ////////////////////////////////////////////////////////
  //View Elements (Attributes): 
  //the following elements are defined in the xml layout file.
  //they are bound lazy, to fetch them after the Activity was started
  ////////////////////////////////////////////////////////
  
  private[this] lazy val editNameField: EditText = getView(R.id.edit_name)
  private[this] lazy val editInformationField: EditText = getView(R.id.edit_information)
  private[this] lazy val createdView: TextView = getView(R.id.created)
  private[this] lazy val lengthView: TextView = getView(R.id.length)
  private[this] lazy val pointCountView: TextView = getView(R.id.pointcount)
  private[this] lazy val saveButton: ImageButton = getView(R.id.save_btn)
  private[this] lazy val cancelButton: ImageButton  = getView(R.id.cancel_btn)

  
  ////////////////////////////////////////////////////////
  //Actions: 
  ////////////////////////////////////////////////////////
  
  /**
   * Called when the user has pressed the save button.
   * This method will save the changes of the Trace object to the repository,
   * and close the activity.
   */
  private[this] def save(trace: Trace) =  tryCatch {
    val newTrace = new Trace(trace.id, 
                             editNameField.getText.toString, 
                             editInformationField.getText.toString,
                             trace.creationTime,
                             trace.pointCount,
                             trace.distance)
    
    newTrace.save                   
    setResult(Activity.RESULT_OK)
    finish()
    Messager.makeMessageShort(this, getString(R.string.trace_saved))
  }
  
  /**
   * Called when the user has pressed the cancel button.
   * It will show a dialog, in which the user can close the activity,
   * without saving the changes of the edited Trace object.
   */
  private[this] def cancel() {
    new AlertDialog.Builder(this)            	
      .setTitle(getString(R.string.discard_changes))
      .setMessage(getString(R.string.dialog_question_cancel_edit))
      .setIcon(R.drawable.warning)
      .setPositiveButton(getString(R.string.dialog_yes), 
        () ⇒  {
          setResult(Activity.RESULT_CANCELED)
          finish()
          Messager.makeMessageShort(this, getString(R.string.trace_not_saved))
        })
      .setNegativeButton(getString(R.string.dialog_no),()⇒{})
      .show()
  }
  
  ////////////////////////////////////////////////////////
  //Init and other Overridden Memebers
  ////////////////////////////////////////////////////////

  /**
   * Initialize the Activity, set the layout from xml, 
   * connect the buttons to actions and read the Trace object from the bundle.
   * The fields of the Activity will be initialized with the data from the Trace object.
   * 
   * @param savedInstanceState the bundle to initialize this activity
   * 
   * @see Activity
   */ 
  override def onCreate(savedInstanceState : Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.trace_edit)

    assume(getIntent.getExtras != null,
           getString(R.string.need_bundle))
    
    val traceObject = getIntent.getExtras.getSerializable(
      TraceComCodes.TRACE_PARAMETER_ID)
    
    assume(traceObject != null,
           getString(R.string.need_trace_bundle))
    
    val trace = traceObject.asInstanceOf[Trace]
    
    editNameField.setText(trace.getName)
    editInformationField.setText(trace.getInformation)
    createdView.setText((new java.util.Date(trace.creationTime)).toString)
    pointCountView.setText(trace.pointCount.toString)
    lengthView.setText(getResources.getString(R.string.distance_label) + 
                         " " + trace.distance.to(MetricUnit.KiloMeter))

    cancelButton.setOnClickListener(cancel _)
    saveButton.setOnClickListener(() ⇒ save(trace))
  }
}

