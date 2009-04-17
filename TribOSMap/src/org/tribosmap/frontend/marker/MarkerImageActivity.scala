package org.tribosmap.frontend.marker

import java.io.File
import android.app.AlertDialog
import android.app.Activity
import android.widget.ImageButton
import android.widget.ImageView
import android.os.Bundle
import android.graphics.BitmapFactory
import org.tribosmap.frontend.common.ActivityHelper;

/**
 * This Activity is used to display an image of a marker,
 * the user can delete this image or he can make a new one.
 * <p>
 * The location of the image is defined by a fileName in the bundle
 * the bundle key for that string is:
 * <code>MarkerComCodes.IMAGE_FILE_PARAMETER_ID</code>
 * <p>
 * If the user wants to delete the image, the responsecode of this
 * Activity is <code>MarkerComCodes.RESULT_DELETE_IMAGE</code>
 * @author Meiko Rachimow
 */
class MarkerImageActivity extends Activity with ActivityHelper {

  ////////////////////////////////////////////////////////
  //View Elements (Attributes): 
  ////////////////////////////////////////////////////////
  
  private[this] lazy val imageView: ImageView = getView(R.id.image_view)
  private[this] lazy val deleteButton: ImageButton = getView(R.id.delete_btn)
  
  ////////////////////////////////////////////////////////
  //Other Attributes: 
  ////////////////////////////////////////////////////////
  
  private[this] lazy val bundle = getIntent.getExtras
  private[this] lazy val imageFileName = bundle.getString(
    MarkerComCodes.IMAGE_FILE_PARAMETER_ID)
  
  
  ////////////////////////////////////////////////////////
  //Methods: 
  ////////////////////////////////////////////////////////
  
  /**
   * Init the Activity, connect the buttons to the actions,<br/>
   * set the layout from xml,<br/>
   * init the other views, read the imageFileName from the bundle
   * 
   * @param savedInstanceState the global Bundle
   * 
   * @see Activity
   */
  override def onCreate(savedInstanceState : Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.marker_image_view)
    assume(bundle != null, getString(R.string.need_bundle))
    assume(imageFileName != null, getString(R.string.need_file_bundle))
    val fullSizeBmp = BitmapFactory.decodeFile(imageFileName)
    imageView.setImageBitmap(fullSizeBmp)

    deleteButton.setOnClickListener(deleteAction _)
    
  }
  
  /**
   * Called if the user has pressed the delete button.
   * It will display a dialog, to let the user delete the image.
   */
  private[this] def deleteAction() {
    
    def delete() = tryCatch {
      new File(imageFileName).delete
      setResult(MarkerComCodes.RESULT_DELETE_IMAGE)
      finish()
    }
    
    new AlertDialog.Builder(this)            	
      .setTitle(getString(R.string.dialog_question))
      .setMessage(getString(R.string.dialog_question_delete) + imageFileName + " ?")
      .setIcon(R.drawable.warning)
      .setPositiveButton(getString(R.string.dialog_yes), delete _)
      .setNegativeButton(getString(R.string.dialog_no), () ⇒ ())
      .show()
  }
}
