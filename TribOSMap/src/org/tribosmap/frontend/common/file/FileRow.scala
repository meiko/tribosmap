package org.tribosmap.frontend.common.file

import java.io.File
import java.util.Date
import android.content.res.Resources
import android.graphics.drawable.{BitmapDrawable, Drawable}
import android.graphics.{BitmapFactory, Bitmap}

import org.tribosmap.frontend.common.list.TribosListRow;


/**
 * Used by the FileChooser,
 * diplays a row in the list of files
 * 
 * @param file a file to diplay in the row
 * @param isParent is the file a folder
 * @param resources the ressources of the activity
 * @author Meiko Rachimow
 */
class FileRow(val file : File, 
              isFolder : Boolean, 
              resources : Resources) extends TribosListRow {
  
 /** 
  * if it is a file to display but not a folder, 
  * this constructor should be used if possible
  * @param file a file to diplay in the row
  * @param resources the ressources of the activity
  */
  def this(file : File, resources : Resources) = 
    this(file, false, resources)
  
  //the drawable to display a folder
  private val folderDrawable = 
    new BitmapDrawable(BitmapFactory.decodeResource(
      resources, R.drawable.folder))
  
  //the drawable to display a file but not a folder
  private val fileDrawable = 
    new BitmapDrawable(BitmapFactory.decodeResource(
      resources, R.drawable.file_map))
  
  //the title of the file, if it is a folder the will end with '/'
  val title : Option[String] = Some(
    if(file.isDirectory){
      if(! isFolder) file.getName + "/"  
      else "../" 
    }else{ file.getName }
  )
  
  //the subtitle
  val subtitle : Option[String] = 
    Some(new Date(file.lastModified).toString())
  
  //the icon of the file
  val icon : Option[Drawable] = Some(
    if(file.isDirectory) folderDrawable
    else fileDrawable
  )
  
  //the informations to the file
  val information : Option[String] = Some(
    
    if(file.isDirectory) resources.getString(
      R.string.folder_prefix) + file.getPath
    
    else resources.getString(R.string.file_prefix) + file.getPath
    
  )
}
