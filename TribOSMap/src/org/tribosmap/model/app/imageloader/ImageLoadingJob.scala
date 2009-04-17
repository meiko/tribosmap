package org.tribosmap.model.app.imageloader

import android.graphics.Bitmap

/**
 * The base class for ImageLoadingJobs.
 * These ImageLoadingJobs are used by the ImageLoader to load images (method work),
 * and use the given callback with the loaded bitmap as a parameter (method callBack).
 * 
 * @param tileId a unique id for the job
 * @param callBack this function will be called if the loader is ready, 
 * the param of this callBack is the loaded Bitmap
 * @author Meiko Rachimow
 */
abstract class ImageLoadingJob( val tileId: String, 
                                protected[imageloader] val callBack: (Bitmap) ⇒ Unit) { 
  /**
   * this function is used by the loader to load the Bitmap
   * @return the loaded bitmap
   */
  protected[imageloader] def work(): Bitmap 
}
