package org.tribosmap.frontend.marker

import java.io.{FileOutputStream, File}

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.{SurfaceHolder, KeyEvent, SurfaceView, Window }
import android.content.Intent
import android.util.Log

import org.tribosmap.frontend.common.ActivityHelper;

/**
 * This Activity will show the camera window, so that the user can make a photo. 
 * After taken the photo it will be saved to the given File.
 * <p>
 * The file has the name given by the bundle of the intent,
 * which is used when starting this activity.
 * The bundle key for that string is:
 * <code>MarkerComCodes.IMAGE_FILE_PARAMETER_ID</code>
 * @author Meiko Rachimow
 */
class CameraActivity extends Activity with ActivityHelper {    
  
  /**
   * the camera window
   */
  private[this] lazy val camview: CameraView = new CameraView(this)

  /**
   * init, set layout
   * @param savedInstanceState
   * 
   * @see Activity
   */
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(camview)
  }

  /**
   * Called when the user has pressed a key.
   * Will start the photo-shooting, if the correct key was pressed.
   * 
   * @param keyCode
   * @param evt
   * @return true if the keycode was processed - otherwise false
   * 
   * @see Activity
   */
  override def onKeyDown(keyCode: Int, evt: KeyEvent): Boolean =  {
    keyCode match {
      case KeyEvent.KEYCODE_DPAD_CENTER ⇒ camview.takePicture
      case _ ⇒ false
    }
    super.onKeyDown(keyCode, evt)
  }
  
  /**
   * Called when a picture was taken.
   * 
   * @param imageData the image in bytes
   */
  private[this] def onPictureTaken(imageData: Array[Byte]) = tryCatch {
    
    val targetFileName = getIntent.getExtras.getString(
      MarkerComCodes.IMAGE_FILE_PARAMETER_ID)
    
    val targetFile = new File(targetFileName)
    
    assume(targetFile.createNewFile, 
           "Cannot write image to file: " + targetFile)
    
    val outputStream = new FileOutputStream(targetFile)
    try {
      outputStream.write(imageData) 
      outputStream.flush
    } finally {
      outputStream.close
    }
    setResult(Activity.RESULT_OK)
    finish()
  }

  /**
   * This class is used to connect to the CameraWindow.
   * It will draw the camera-content to the screen.
   * 
   * @param context the Application Context
   */
  private class CameraView(context: Context) extends 
    SurfaceView(context) with SurfaceHolder.Callback {
    
    import android.hardware.Camera
    import android.hardware.Camera.PictureCallback
    
    //the camera
    private[this] val camera: Camera = Camera.open()
    
    //connect to the graphics context of the window
    getHolder.addCallback(this);
    getHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    
    /**
     * make click - and take a picture
     */
    private[CameraActivity] def takePicture() {
      camera.takePicture(null, null, new PictureCallback {
        override def onPictureTaken(imageData: Array[Byte], camera: Camera) {
          CameraActivity.this.onPictureTaken(imageData)
        }})
    }
    
    /**
     * called when the graphic-surface was created
     * @param holder the surface holder to access the graphicsurface
     */
    override def surfaceCreated(holder: SurfaceHolder) = 
      camera.setPreviewDisplay(holder)

    /**
     * called when the graphic-surface was destroyed
     * @param holder the surface holder to access the graphicsurface
     */
    override def surfaceDestroyed(holder: SurfaceHolder) = 
      camera.stopPreview()

    /**
     * called when the graphic-surface was changed
     * @param holder the surface holder to access the graphicsurface
     * @param format 
     * @param width
     * @param height
     */
    override def surfaceChanged(
      holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        val parameters = camera.getParameters()
        parameters.setPreviewSize(width, height)
        camera.setParameters(parameters)
        camera.startPreview()
    }

  }
}
