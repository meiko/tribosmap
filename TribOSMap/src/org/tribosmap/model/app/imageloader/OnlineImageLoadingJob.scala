package org.tribosmap.model.app.imageloader

import org.apache.http.client.methods.HttpGet
import org.apache.http.protocol.BasicHttpContext
import android.graphics.{Bitmap, BitmapFactory}
import java.io.{File, FileOutputStream, DataInputStream}

/**
 * An ImageLoadingJob to load an image from the internet.
 * It uses a given cacheDirectory, to save a loaded image to a file.
 * 
 * @param tileId the unique tileId
 * @param uriServer the Server from where to load the image
 * @param tileUri  the tile uri (without the server uri)
 * @param callBack the callback to call after finishing the load
 * @param cacheDirectory the directory to cache loaded images
 * @author Meiko Rachimow
 */
class OnlineImageLoadingJob(tileId : String, 
                            uriServer : String, 
                            tileUri: String, 
                            callBack: (Bitmap) ⇒ Unit, 
                            cacheDirectory: String) 
  extends ImageLoadingJob(tileId, callBack) {
  
  /**
   * load an image from the internet,
   * uses the HttpAccess object to load data per http
   * 
   * @return the loaded bitmap
   */
  protected[imageloader] def work(): Bitmap = {
    
    val cacheFile = new File(cacheDirectory, tileUri.replaceAll("/", "-"))
    
    if(cacheFile.exists) return BitmapFactory.decodeFile(cacheFile.getPath)
    val uri = uriServer + tileUri
    
    val httpget = new HttpGet(uri)
    try {
      
      val response = HttpAccess.httpClient.execute(httpget, new BasicHttpContext())
      val entity = response.getEntity()
      
      if(! entity.getContentType.getValue.contains("image/")) 
        throw new Exception("resource " + uri + 
                              " is not an image " + entity.getContentType.getName)
      
      val imageDataSize = entity.getContentLength.toInt
      val imageData = new Array[Byte](imageDataSize);
      val inputStream = new DataInputStream(entity.getContent)
      
      try { 
        inputStream.readFully(imageData)
      } finally {
        inputStream.close
      }
      
      val outputStream = new FileOutputStream(cacheFile)
      
      try {
        outputStream.write(imageData) 
        outputStream.flush
      } finally {
        outputStream.close
      }
      
      BitmapFactory.decodeByteArray(imageData, 0, imageData.length); 
      
    } finally {
      httpget.abort()
    }
  }
}
