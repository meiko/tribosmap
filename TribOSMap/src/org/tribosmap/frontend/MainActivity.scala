package org.tribosmap.frontend

import android.app.{Activity, AlertDialog}
import android.content.{Intent, Context}
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import java.util.Date
import java.text.DecimalFormat
import java.io.File

import org.tribosmap.frontend.map.{MapListActivity, MapViewActivity}
import org.tribosmap.frontend.marker._
import org.tribosmap.frontend.trace.TraceListActivity
import org.tribosmap.model.app.{TraceRecordServiceConnection, ITraceRecordService, TraceRecordService}
import org.tribosmap.model.math.distance.{Distance, MetricUnit}
import org.tribosmap.model.math.geographic.{GeographicCoOrdinatePair, Position}
import org.tribosmap.model.business.domain.{Marker, MarkerType}
import org.tribosmap.frontend.common._
import org.tribosmap.frontend.common.file.{FileChooser, FileChooserComCodes}
import org.tribosmap.model.app.LocationAdapter
import org.tribosmap.model.ServiceAccess

/**
 * This is the central application activity.
 * In this activity the Listfrontend of the Marker, GeoMap and Trace Objects can be opened.
 * The user can start a trace-record here, he can create a new marker by opening the MarkerEditActivity and
 * he can import a GeoMap or open the MapViewActivity to view the online map.
 * The activity will show some textual informations about the actual position, and the actual recorded trace.
 * @author Meiko Rachimow
 */
class MainActivity extends Activity 
  with LocationAdapter 
  with ActivityHelper 
  with ServiceAccess {
  
    
  ////////////////////////////////////////////////////////
  //View Elements (Attributes): 
  //the following elements are defined in the xml layout file.
  //they are bound lazy, to fetch them after the Activity was started
  ////////////////////////////////////////////////////////
  
  private[this] lazy val lblLatitude: TextView = getView(R.id.lblLatitude)
  private[this] lazy val lblLongitude: TextView = getView(R.id.lblLongitude)
  private[this] lazy val lblAltitude: TextView = getView(R.id.lblAltitude)
  private[this] lazy val lblRecordText: TextView = getView(R.id.record_trace_text)
  private[this] lazy val lblRecordText2: TextView = getView(R.id.record_trace_text2)
  private[this] lazy val lblRecordPointCount: TextView = getView(R.id.record_trace_pointcount)
  private[this] lazy val lblRecordDistance: TextView = getView(R.id.record_trace_distance)
  private[this] lazy val traceBtn: ImageTextButton = getView(R.id.main_trace_btn)
  private[this] lazy val quitBtn: ImageTextButton = getView(R.id.main_quit_btn)
  private[this] lazy val markerBtn: ImageTextButton = getView(R.id.main_marker_btn)
  private[this] lazy val mapBtn: ImageTextButton = getView(R.id.main_map_btn)
  private[this] lazy val importBtn: ImageTextButton = getView(R.id.main_import_btn)
  private[this] lazy val createMarkerBtn: ImageTextButton = getView(R.id.main_create_marker_btn)
  private[this] lazy val osmViewBtn: ImageTextButton = getView(R.id.main_osmview_btn)
  private[this] lazy val recordBtn: ImageTextSwitchButton = getView(R.id.main_record_btn)
  
  ////////////////////////////////////////////////////////
  //Other Attributes
  ////////////////////////////////////////////////////////
  
  /**
   * a requestcode to handle the createMarkerAction
   */
  private[this] val KEY_CREATE_MARKER = 11
    
  /**
   * a requestcode to handle the chooseFile when importMapAction
   */
  private[this] val KEY_CHOOSE_FILE = 22
  
  /**
   * the actual position
   */
  private[this] var actualPosition = new Position(
    new GeographicCoOrdinatePair(0,0),0, Preferences.standardDatum)
  
  /**
   * the the connection to the trace record service
   */
  private[this] val recordConnection = new TraceRecordServiceConnection()
  
  /**
   * the the trace record service used to record a trace
   */
  private[this] def recordService: ITraceRecordService = {
    recordConnection.getRecordService
  }
  
  
  ////////////////////////////////////////////////////////
  //Init
  ////////////////////////////////////////////////////////
  
  /**
   * Initialize the Activity, set the layout from xml, 
   * connect the buttons to actions etc.
   * and start listening for new gps-events (LocationAdapter)
   * 
   * @param savedInstanceState the bundle to initialize this activity
   * 
   * @see Activity
   */ 
  override def onCreate(savedInstanceState : Bundle) {
    
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main_menu)

    createMarkerBtn.setOnClickAction(createMarkerAction _ )
    importBtn.setOnClickAction(selectMapFileAction _ )
    recordBtn.setOnClickAction(recordTraceAction _ )
    quitBtn.setOnClickAction(quitAction _ )
    
    traceBtn.setOnClickAction( () ⇒ 
      startActivity(new Intent(this, classOf[TraceListActivity])) 
    )
    
    markerBtn.setOnClickAction( () ⇒ 
      startActivity(new Intent(this, classOf[MarkerListActivity])) 
    )
    
    mapBtn.setOnClickAction( () ⇒ 
      startActivity(new Intent(this, classOf[MapListActivity])) 
    )
    
    osmViewBtn.setOnClickAction( () ⇒ 
      startActivity(new Intent(this, classOf[MapViewActivity])) 
    )

    bindService(new Intent(this,classOf[TraceRecordService]), 
                recordConnection, Context.BIND_AUTO_CREATE)
    
    tryCatch {
      startListening()
    }
  } 
  
  
  ////////////////////////////////////////////////////////
  //Actions
  ////////////////////////////////////////////////////////
  
  /**
   * Called if the user pressed the create-marker-button.
   * This Action will create a new Marker, and open the 
   * MarkerEditActivity to let the user edit this Marker.
   */
  private[this] def createMarkerAction() = tryCatch {
    
    val traceId = if(recordService != null && recordService.isRecording) {
      recordService.actualTraceId
    } else {
      -1l
    }
    
    val actualTime = new Date()
    val bundle = new Bundle()
    bundle.putSerializable(MarkerComCodes.MARKER_PARAMETER_ID, 
                           new Marker(-1, traceId, 
                                      actualTime.toGMTString, "", 
                                      MarkerType.PointOfInterest, 
                                      actualTime.getTime, null,
                                      actualPosition))
    
    val intent = new Intent(this, classOf[MarkerEditActivity])
    intent.putExtras(bundle)
    Messager.makeMessageShort(this, "open window MarkerEditActivity")
    startActivityForResult(intent, KEY_CREATE_MARKER)
  }
  
  /**
   * Called when the user has pressed the quit button.
   * It will show a dialog, in which the user can quit the application.
   */
  private[this] def quitAction() {
    new AlertDialog.Builder(this)            	
      .setTitle(
        getResources.getString(R.string.dialog_question))
      .setMessage(
        getResources.getString(R.string.dialog_question_quit))
      .setIcon(R.drawable.warning)
      .setPositiveButton(getString(R.string.dialog_yes), finish _ )
      .show()
  }
  
  /**
   * Called when the user has pressed the record trace button.
   * It uses the record service to start the record (if all is running fine, else shows an error).
   */
  private[this] def recordTraceAction() {
    try {
      if(recordService == null) {
        error("cannot connect to the service, maybe later.")
      } else {
        if(!recordBtn.isOn) {
          recordService.startRecord()
          lblRecordPointCount.setText(recordService.getPointCount.toString)
          lblRecordText.setVisibility(View.VISIBLE)
          lblRecordPointCount.setVisibility(View.VISIBLE)
          lblRecordDistance.setVisibility(View.VISIBLE)
          lblRecordText2.setVisibility(View.VISIBLE)
        }else {
          recordService.stopRecord()
          lblRecordText.setVisibility(View.INVISIBLE)
          lblRecordPointCount.setVisibility(View.INVISIBLE)
          lblRecordDistance.setVisibility(View.INVISIBLE)
          lblRecordText2.setVisibility(View.INVISIBLE)
        }     
      }
    } catch {
      case e: Throwable ⇒ 
        displayError(e)
        recordBtn.setOff
    }
  }
  
  /**
   * Called when the user has pressed the import map button.
   * It will open the FileChooser, so that the user can select a map from the filesystem.
   */
  private[this] def selectMapFileAction() {
    val bundle = new Bundle()
    bundle.putStringArray(FileChooserComCodes.KEY_FILTER, Array("xml"))
    bundle.putString(FileChooserComCodes.KEY_ROOTDIR, preferences.importFolder)
    val intent = new Intent(this, classOf[FileChooser])
    intent.putExtras(bundle)
    Messager.makeMessageShort(this, "open window: FileChooser")
    startActivityForResult(intent, KEY_CHOOSE_FILE)
  }
  
  /**
   * Will import a map into the system, by using the MapService.
   * 
   * @param fileName the fileName of the map describing xml file.
   */
  private[this] def importMapAction(fileName: String) {
    mapService.importMapFromXml(new File(fileName), new Date().getTime)
  }
  
  
  ////////////////////////////////////////////////////////
  //Overridden
  ////////////////////////////////////////////////////////
  
 /**
  * Called if the gps receiver is sending a new position to our application.
  * Here the TextViews with some informations about the actual recorded Trace
  * and about the actual position are updated.
  * 
  * @param loc the new Location (a gps object describing the position)
  * @see LocationAdapter
  */
  override def onLocationChanged(loc : Location) = tryCatch {
    val format = new DecimalFormat()
    format.setMaximumFractionDigits(5)
    lblLatitude.setText(format.format(loc.getLatitude) + "°N")
    lblLongitude.setText(format.format(loc.getLongitude) + "°E")
    format.setMaximumFractionDigits(1)
    lblAltitude.setText(format.format(loc.getAltitude) + " m")
    actualPosition = new Position(
      new GeographicCoOrdinatePair(loc.getLatitude, loc.getLongitude),
      loc.getAltitude, Preferences.standardDatum
    )
    if(recordService != null && recordService.isRecording) {
      lblRecordPointCount.setText(recordService.getPointCount.toString)
      lblRecordDistance.setText(
        Distance(recordService.getActualDistanceInMeter, MetricUnit.Meter).
          to(MetricUnit.KiloMeter).toString)
    }
  }
  
  /**
   * This method is called when a sub-activity has been finished.
   * In this case - if the MarkerEditActivity(to create a new marker) 
   * or the FileChooser(to import a map) was closed.
   * 
   * @param requestCode the original requestcode when opening the subactivity
   * @param resultCode the resultcode of the subactivity
   * @param data the data given from the subactivity
   * @see Activity
   */
  protected override def onActivityResult (
    requestCode: Int, resultCode: Int, data : Intent){
    
      requestCode match {
        
        case KEY_CREATE_MARKER ⇒ 
          
          if(resultCode == Activity.RESULT_OK && 
             recordService != null && 
             recordService.isRecording) tryCatch {
            
            recordService.newMarker()
          }
          
        case KEY_CHOOSE_FILE ⇒
          if(resultCode == Activity.RESULT_OK) {
            importMapAction(data.getExtras.get(FileChooserComCodes.RESULT).toString)
          }
          
        case _ ⇒ None
      }
  }

  /**
   * When the application is closed (at the moment when this activity is finished)
   * we have to stop the TraceRecordService, which is running in the background.
   * So we do it right here...
   * 
   * @see Activity
   */
  protected override def onDestroy() {
    
    if(recordService != null && recordService.isRecording) {
      recordService.stopRecord
    }                                
    unbindService(recordConnection)
    endListening()
    super.onDestroy
  }
}

