package org.tribosmap.frontend.marker

/**
 * Communication codes for the MarkerActivities.
 * Used by the Activities to communicate (see: Intent / Activity / Bundle)
 * @author Meiko Rachimow
 */
protected[frontend] object MarkerComCodes {
  
  /**
   * this flag is used as a key in a bundle to identify a serialized marker
   */
  val MARKER_PARAMETER_ID = "MARKER_PARAMETER_ID"
  
  /**
   * this flag is used as a key in a bundle to identify the image-filename
   */
  protected[marker] val IMAGE_FILE_PARAMETER_ID = "IMAGE_PARAMETER_ID"
  
  /**
   * the resultcode is used by MarkerImage- MarkerEditActivity
   */
  protected[marker] val RESULT_DELETE_IMAGE = 77
  
}
