package org.tribosmap.model.app.imageloader

import android.graphics.{Bitmap, BitmapFactory}
import java.io.RandomAccessFile

/**
 * An ImageLoadingJob to load an image from a tribosmap-file (*.tosm).
 * To load this image, a RandomAccessFile is used.
 * The image is saved in a specified block inside the file.
 * (see documentation of the converter for tosm files)
 * 
 * @param tileId the unique tileId
 * @param mapFileName the name of the tosm-file
 * @param header the header of the image files in the tosm-file
 * @param offset the offset in the file (in bytes) for the image to load
 * @param dataLength the length in bytes of the image to load
 * @param callBack the callback to call after finishing the load
 * @author Meiko Rachimow
 */
class OfflineImageLoadingJob(
          tileId : String, 
          mapFileName : String, 
          header: Array[Byte], 
          offset: Long, 
          dataLength: Int,
          callBack: (Bitmap) ⇒ Unit) 
  extends ImageLoadingJob(tileId, callBack) {  
    
  /**
   * load an image from a tosm file
   * 
   * @return the loaded bitmap
   */  
  protected[imageloader] def work(): Bitmap = {
    
    val imageData = new Array[Byte](dataLength + header.length)
    Array.copy(header,0,imageData,0 ,header.length)
    val raf = new RandomAccessFile(mapFileName, "r")
    try {
      raf.seek(offset)
      raf.readFully(imageData, header.length, dataLength)
    } finally {
      raf.close
    }
    return BitmapFactory.decodeByteArray(imageData, 0, imageData.length)
  }
}