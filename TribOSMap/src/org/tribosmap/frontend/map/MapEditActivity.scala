package org.tribosmap.frontend.map

import java.text.DecimalFormat

import android.app.{AlertDialog, Activity}
import android.util.Log
import android.os.Bundle
import android.widget.{EditText, TextView, ImageView, ImageButton}

import org.tribosmap.model.ServiceAccess
import org.tribosmap.model.business.domain.GeoMap
import org.tribosmap.model.app.maptiling.OfflineUtmMap
import org.tribosmap.frontend.common.{ActivityHelper, Messager}

/**
 * This Activity is used to edit a GeoMap object, it has fields for the name etc.
 * <p>
 * It needs a GeoMap object in the init bundle.<br/>
 * The Key for that GeoMap in the bundle is: <code>MapComCodes.MAP_PARAMETER_ID</code><br/>
 * This activity is using the GeoMapService, to save the edited GeoMap-object in the repository.<br/>
 * The resultcode of this Activity after closing, can be:<br/>
 * <code>Activity.RESULT_OK</code> if the GeoMap was saved (changed),<br/>
 * or <br/>
 * <code>Activity.RESULT_CANCELED</code> if the GeoMap wasn't saved.<br/>
 * @author Meiko Rachimow
 */
class MapEditActivity extends Activity with ActivityHelper with ServiceAccess {
  
  ////////////////////////////////////////////////////////
  //View elements (attributes): 
  //The following elements are defined in the xml layout file.
  //They are bound lazy, to fetch them after the Activity was started.
  ////////////////////////////////////////////////////////
  
  private[this] lazy val editNameField: EditText = getView(R.id.edit_name)
  private[this] lazy val imageView: ImageView = getView(R.id.map_image)
  private[this] lazy val sizeXview: TextView = getView(R.id.size_x)
  private[this] lazy val sizeYview: TextView = getView(R.id.size_y)
  private[this] lazy val scaleView: TextView = getView(R.id.scale)
  private[this] lazy val leftUpLatView: TextView = getView(R.id.left_up_latitude)
  private[this] lazy val leftUpLonView: TextView = getView(R.id.left_up_longitude)
  private[this] lazy val rightDownLatView: TextView = getView(R.id.right_down_latitude)
  private[this] lazy val rightDownLonView: TextView = getView(R.id.right_down_longitude)
  private[this] lazy val createdView: TextView = getView(R.id.created)
  private[this] lazy val saveButton: ImageButton = getView(R.id.save_btn)
  private[this] lazy val cancelButton: ImageButton = getView(R.id.cancel_btn)
  
  ////////////////////////////////////////////////////////
  //Other Attributes: 
  ////////////////////////////////////////////////////////
  
  /**
   * the given GeoMap to edit
   */
  private[this] lazy val map = { 

    val bundle = getIntent.getExtras
    if (bundle == null) error("need a bundle")
    
    val serial = bundle.getSerializable(MapComCodes.MAP_PARAMETER_ID) 
    if(serial == null) error("need a serialized map in the bundle")
      
    serial.asInstanceOf[GeoMap]
  }
    
  ////////////////////////////////////////////////////////
  //Actions: 
  ////////////////////////////////////////////////////////
  
  /**
   * Called when the user has pressed the cancel button.
   * It will show a dialog, in which the user can close the activity,
   * without saving the changes of the edited GeoMap object.
   */
  private[this] def cancelAction() {
    
    new AlertDialog.Builder(this)            	
      .setTitle(getString(R.string.discard_changes))
      .setMessage(getString(R.string.dialog_question_cancel_edit))
      .setIcon(R.drawable.warning)
      .setPositiveButton(getString(R.string.dialog_yes), () ⇒ {
        setResult(Activity.RESULT_CANCELED)
        finish()
      })
      .setNegativeButton(getString(R.string.dialog_no), () ⇒ {})
      .show()
  }

  /**
   * Called when the user has pressed the save button.
   * This method will save the changes of the GeoMap object to the repository,
   * and close the activity.
   */
  private[this] def saveAction() = tryCatch {
    
    map.rename(editNameField.getText.toString)
    map.save()
    setResult(Activity.RESULT_OK)
    finish()
  }
    
  ////////////////////////////////////////////////////////
  //Init and other Overridden Memebers
  ////////////////////////////////////////////////////////
    
  /**
   * Initialize the Activity, set the layout from xml, 
   * connect the buttons to actions and read the GeoMap object from the bundle.
   * The fields of the Activity will be initialized with the data from the GeoMap object.
   * 
   * @param savedInstanceState the bundle to initialize this activity
   * 
   * @see Activity
   */ 
  override def onCreate(savedInstanceState : Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.map_edit)

    editNameField.setText(map.getName)
      //TODO: imageView.setBackgroundDrawable
    sizeXview.setText(map.imageSize.x.toString)
    sizeYview.setText(map.imageSize.y.toString)
      
    val algorithm = new OfflineUtmMap(map, map.getAllReferencedPixels)
      
    val leftTopCoord = algorithm.getLeftTopLatLon
    val rightBottomCoord = algorithm.getRightBottomLatLon
    
    val format = new DecimalFormat();
    
    format.setMaximumFractionDigits(1);
    val mPerPixelString = format.format(algorithm.getMeterPerPixel)
    
    format.setMaximumFractionDigits(5);
    val latleftTopString = format.format(leftTopCoord.x) + "°N, "
    val lonleftTopString = format.format(leftTopCoord.y) + "°E "
    val latrightBottomString = format.format(rightBottomCoord.x) + "°N, "
    val lonrightBottomString = format.format(rightBottomCoord.y) + "°E "
      
    scaleView.setText(mPerPixelString)
    leftUpLatView.setText(latleftTopString)
    leftUpLonView.setText(lonleftTopString)
    rightDownLatView.setText(latrightBottomString)
    rightDownLonView.setText(lonrightBottomString)
    createdView.setText(new java.util.Date(map.time).toString)

    cancelButton.setOnClickListener( () ⇒ { cancelAction() })
    saveButton.setOnClickListener( () ⇒ { saveAction() })
  }  
}

