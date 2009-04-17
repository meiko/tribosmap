package org.tribosmap.frontend.marker

import android.app.{AlertDialog, Activity}
import android.content.Intent
import android.graphics.{Bitmap, BitmapFactory}
import android.os.Bundle
import android.view.{View, ViewGroup}
import android.widget.{EditText, Spinner, SpinnerAdapter, 
                       AdapterView, ImageButton, RadioGroup, 
                       RadioButton, ViewSwitcher, Toast, LinearLayout}

import android.widget.AdapterView.OnItemSelectedListener
import android.widget.RadioGroup.OnCheckedChangeListener
import android.widget.LinearLayout.LayoutParams

import org.tribosmap.model.business.domain.{Marker, MarkerType}
import org.tribosmap.model.math.geographic._
import org.tribosmap.frontend.common.{ActivityHelper, Messager};
import org.tribosmap.model.ServiceAccess

/**
 * This Activity is used to edit a Marker object, it has fields for the name etc.
 * <p>
 * It needs a Marker object in the init bundle.<br/>
 * The Key for that Marker in the bundle is: <code>MarkerComCodes.MARKER_PARAMETER_ID</code><br/>
 * This activity is using the MarkerService, to save the edited Marker-object in the repository.<br/>
 * The resultcode of this Activity after closing, can be:<br/>
 * <code>Activity.RESULT_OK</code> if the Marker was saved (changed),<br/>
 * or <br/>
 * <code>Activity.RESULT_CANCELED</code> if the Marker wasn't saved.<br/>
 * @author Meiko Rachimow
 */
class MarkerEditActivity extends Activity 
  with ActivityHelper  
  with ServiceAccess  {
  
  ////////////////////////////////////////////////////////
  //View Elements (Attributes): 
  //the following elements are defined in the xml layout file.
  //they are bound lazy, to fetch them after the Activity was started
  ////////////////////////////////////////////////////////
  
  private[this] lazy val Seq( mainView, utmZoneLayout) = 
    getViews[LinearLayout]( R.id.edit_marker_main, R.id.edit_utm_zone_row)
  
  private[this] lazy val Seq( editNameField, editInformationField, editUtmNorthField, editUtmEastField, 
                              editUtmZoneField, editLatField, editLonField, editAltitudeField) = 
    getViews[EditText]( R.id.edit_name, R.id.edit_information, R.id.edit_utm_north, R.id.edit_utm_east, 
                        R.id.edit_utm_zone, R.id.edit_latitude, R.id.edit_longitude, R.id.edit_altitude )
  
  private[this] lazy val Seq( radioButtonUtm, radioButtonLatLon) = 
    getViews[RadioButton]( R.id.coord_switch_utm, R.id.coord_switch_latlon)
  
  private[this] lazy val Seq( saveButton, cancelButton, cameraButton) = 
    getViews[ImageButton]( R.id.save_btn, R.id.cancel_btn, R.id.camera_btn)
  
  private[this] lazy val selectHemisphereSpinner: Spinner = getView(R.id.hemisphere)
  private[this] lazy val radioGroupSwitchUTMlatLon: RadioGroup = getView(R.id.coord_switch_radio)
  private[this] lazy val switcherUtmLatLon: ViewSwitcher = getView(R.id.coord_flipper)
    
  
  ////////////////////////////////////////////////////////
  //Other Attributes: 
  ////////////////////////////////////////////////////////
  
  //some strings defined in xml
  private[this] lazy val hemisphereNorth = getResources.getString(R.string.north)
  private[this] lazy val hemisphereSouth = getResources.getString(R.string.south)
  private[this] lazy val hemisphereArray = getResources.getStringArray(R.array.hemisphere)
  
  /**
   * the actual marker to edit - taken from the bundle
   */
  private[this] lazy val marker = {

    val bundle = getIntent.getExtras
    assume(bundle != null, getString(R.string.need_bundle))
    
    val markerObj = bundle.getSerializable(MarkerComCodes.MARKER_PARAMETER_ID)
    assume(markerObj != null, 
           getString(R.string.need_marker_bundle))
    
    markerObj.asInstanceOf[Marker]
    
  }
  
  /**
   * a response / requestcode to handle the imageTakePhotoAction
   */
  private[this] val KEY_IMAGE_FILE_EDIT = 99
  
  
  ////////////////////////////////////////////////////////
  //Actions: 
  ////////////////////////////////////////////////////////

  /**
   * Called if the user has pressed the Photo-Button, <br/>
   * Will take open the CameraActivity to let the user make the photo, 
   * or if an image already exists, 
   * it will open the MarkerImageActivity to show this image.
   */
  private[this] def imageTakePhotoAction() = tryCatch {
    
    val bundle = new Bundle
    val intent = if(marker.getPhotoFileName != null) {
      new Intent(this, classOf[MarkerImageActivity])
    } else {
      
      marker.editPhoto(preferences.cameraCacheFolder + 
        System.currentTimeMillis + ".jpg")
      
      new Intent(this, classOf[CameraActivity])
    }
    bundle.putString(
        MarkerComCodes.IMAGE_FILE_PARAMETER_ID, marker.getPhotoFileName)
    intent.putExtras(bundle)
    startActivityForResult(intent, KEY_IMAGE_FILE_EDIT)
    Messager.makeMessageShort(this,getString(R.string.window_open))
  }
  
  /**
   * Called when the user has pressed the cancel button.
   * It will show a dialog, in which the user can close the activity,
   * without saving the changes of the edited Marker object.
   */
  private[this] def cancelAction() {
    new AlertDialog.Builder(this)            	
      .setTitle(getString(R.string.discard_changes))
      .setMessage(getString(R.string.dialog_question_cancel_edit))
      .setIcon(R.drawable.warning)
      .setPositiveButton(getString(R.string.dialog_yes), () ⇒ {
          setResult(Activity.RESULT_CANCELED)
          finish()
          Messager.makeMessageShort(this, getString(R.string.marker_not_saved))
        })
      .setNegativeButton(getString(R.string.dialog_no), ()⇒())
      .show()
  }
  
  /**
   * Called when the user has pressed the save button.
   * This method will save the changes of the Marker object to the repository,
   * and close the activity.
   */
  private[this] def saveAction() = tryCatch {

    require(getLatLonFromView.isDefined, getString(R.string.position_is_needed))
    val altitude = editAltitudeField.getText.toString
    val pos = new Position(getLatLonFromView.get, altitude.toDouble, Preferences.standardDatum)
    
    marker.rename(editNameField.getText.toString)
    marker.editInformation(editInformationField.getText.toString)
    marker.editPosition(pos)
    marker.save

    setResult(Activity.RESULT_OK)
    finish()
    Messager.makeMessageShort(this, getString(R.string.marker_saved))
  }
  
  ////////////////////////////////////////////////////////
  //Init and other Overridden Memebers
  ////////////////////////////////////////////////////////

  /**
   * Initialize the Activity, set the layout from xml, 
   * connect the buttons to actions and read the Marker object from the bundle.
   * The fields of the Activity will be initialized with the data from the Marker object.
   * 
   * @param savedInstanceState the bundle to initialize this activity
   * 
   * @see Activity
   */ 
  override def onCreate(savedInstanceState : Bundle) {
    
    super.onCreate(savedInstanceState)
    setContentView(R.layout.marker_edit)
    
    assume(marker.getPosition != null, 
           getString(R.string.position_is_needed))
    
    editNameField.setText(marker.getName)
    editInformationField.setText(marker.getInformation)
    setPositionInView(marker.getPosition)
    setImage()
    
    radioButtonLatLon.setChecked(true)
    mainView.requestLayout
    
    cameraButton.setOnClickListener(imageTakePhotoAction _ )
    cancelButton.setOnClickListener(cancelAction _)
    saveButton.setOnClickListener(saveAction _)
    
    radioGroupSwitchUTMlatLon.setOnCheckedChangeListener(
      new CoordinateTypeChangeListener())
  }
  
  /**
   * This method is called when a sub-activity has been finished.
   * In this case - if the CameraActivity or MarkerImageActivity was closed.
   * 
   * @param requestCode the original requestcode when opening the subactivity
   * @param resultCode the resultcode of the subactivity
   * @param data the data given from the subactivity
   * 
   * @see Activity
   */
  protected override def onActivityResult(
    requestCode: Int, resultCode: Int, data : Intent) {
    
    requestCode match {
      case KEY_IMAGE_FILE_EDIT ⇒ resultCode match {
        case Activity.RESULT_OK ⇒ 
          setImage()
          Messager.makeMessageShort(this, getString(R.string.photo_saved))
        case MarkerComCodes.RESULT_DELETE_IMAGE ⇒ 
          marker.editPhoto(null)
          setImage()
          Messager.makeMessageShort(this, getString(R.string.image_deleted))
        case _ ⇒ None
      }
      case _ ⇒ None
    }
  }
  
  
  ////////////////////////////////////////////////////////
  //View Setter/Getter and Listener
  ////////////////////////////////////////////////////////
  
  /**
   * set the coordinates in the view
   * 
   * @param position the position to display in the view
   */
  private[this] def setPositionInView(position: Position) {
    
    val utm = position.utm
    editUtmNorthField.setText(utm.northing.toString)
    editUtmEastField.setText(utm.easting.toString)
    editUtmZoneField.setText(utm.zoneNumber.toString)
    val actualZoneLetterIndex = hemisphereArray.findIndexOf(
      _ equals utm.hemisphere.toString)
    
    val latLon = position.latLon
    selectHemisphereSpinner.setSelection(actualZoneLetterIndex,true)
    editLatField.setText(latLon.x.toString)
    editLonField.setText(latLon.y.toString)
    
    editAltitudeField.setText(marker.getPosition.altitude.toString)
  }
  
  /**
   * get the geographic coordinates from the view
   * 
   * @return the geogpraphic coordinates as an option (None if there are no coords)
   */
  private[this] def getLatLonFromView : Option[GeographicCoOrdinatePair] = {
    
    if(editLatField.getText.length < 1 || editLonField.getText.length < 1) {
      None
    }
    else {
      Some(GeographicCoOrdinatePair(
        editLatField.getText.toString.toFloat, 
        editLonField.getText.toString.toFloat))
    }
  }
  
  /***
   * set the image in the view
   */
  private[this] def setImage() {
    if(marker.getPhotoFileName != null) {
      val fullSizeBmp = BitmapFactory.decodeFile(marker.getPhotoFileName)
      val smallImage = Bitmap.createScaledBitmap(fullSizeBmp, 40, 40, true)
      cameraButton.setImageBitmap(smallImage)
      cameraButton.setAdjustViewBounds(true)
    } else {
      cameraButton.setImageResource(R.drawable.image);
    }
  }
  
  /**
   * This class is used to control the coordinate type switch.<br/>
   * The user can switch the type between UTM and Geographic Coordinates.<br/>
   * When the type was switched, we have to compute the other values,
   * and set it in the view.
   */
  private class CoordinateTypeChangeListener extends OnCheckedChangeListener {
    
    //the index of the utm coord view
    private[this] lazy val utmViewIndex = 
      if(switcherUtmLatLon.getChildAt(0).getId == R.id.coord_utm_view) 0 else 1
  
    //the index of the geo-coords view
    private[this] lazy val lonLatViewIndex = if(utmViewIndex == 0) 1 else 0
    
    /**
     * clear all coords in the view
     */
    private[this] def clearCoordinatesInView() {
      
      editUtmNorthField.getText.clear
      editUtmEastField.getText.clear
      editUtmZoneField.getText.clear
      editLatField.getText.clear
      editLonField.getText.clear
    }
    
    /**
     * get the utm coords from the view
     * 
     * @return the utm coord as an option(None if there are no coords in the view)
     */
    private[this] def getUtmFromView : Option[UtmCoOrdinates] = {
    
      if(editUtmNorthField.getText.length < 1 ||
           editUtmEastField.getText.length < 1 ||
           editUtmZoneField.getText.length < 1) {
        None
      } else tryCatch {
          UtmCoOrdinates(
            editUtmNorthField.getText.toString.toDouble,
            editUtmEastField.getText.toString.toDouble,
            editUtmZoneField.getText.toString.toInt,
            Hemisphere(
              selectHemisphereSpinner.getSelectedItem.asInstanceOf[String]),
            Preferences.standardDatum
          )
      }
    }
    
    /**
     * called if the user switched to the other coord type
     * 
     * @param group the group view id
     * @param checkedId the id of the view
     * 
     * @see OnCheckedChangeListener
     */
    override def onCheckedChanged(group : RadioGroup, checkedId : Int) {
      
      group.getCheckedRadioButtonId match {
        
        case R.id.coord_switch_utm ⇒ {
            
          utmZoneLayout.setLayoutParams(
            new LayoutParams(
              ViewGroup.LayoutParams.WRAP_CONTENT, 
              ViewGroup.LayoutParams.WRAP_CONTENT))
          if(getLatLonFromView.isDefined) tryCatch {
            val posLatLon: Position = new Position(
              getLatLonFromView.get, editAltitudeField.getText.toString.toDouble, 
              Preferences.standardDatum)
            setPositionInView(posLatLon)
          } else{
            clearCoordinatesInView()
          }
          switcherUtmLatLon.setDisplayedChild(utmViewIndex)
        }
        
        case R.id.coord_switch_latlon ⇒  {
          
          utmZoneLayout.setLayoutParams(
            new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0))
          if(getUtmFromView.isDefined) tryCatch {
            
            val posUtm: Position = new Position(
              getUtmFromView.get, editAltitudeField.getText.toString.toDouble)
            setPositionInView(posUtm)
          } else{
            clearCoordinatesInView()
          }
          switcherUtmLatLon.setDisplayedChild(lonLatViewIndex)
        }
        
      }
      
      mainView.requestLayout
    }
  }
}
