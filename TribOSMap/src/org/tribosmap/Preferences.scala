package org.tribosmap

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import org.tribosmap.model.math.geographic.Datum.WGS84Datum
import org.tribosmap.model.math.Vector2i

/**
 * contains global values,
 * TODO: put it in a xml file instead or settings window
 * @author Meiko Rachimow
 */
object Preferences {
  
  //the size of a maptile
  val tileSize : Int = 256
  
  //the size of the screen
  //val windowSize = Vector2i(320, 480)
  
  //the standard datum used for coordinates
  val standardDatum = WGS84Datum
}

/**
 * a Preference object, containing initial values
 * TODO: find a better way to do the same in xml.
 * @author Meiko Rachimow
 */
class Preferences(context: Context) {

  PreferenceManager.setDefaultValues(
    context, "tribosmap.pref", Context.MODE_WORLD_READABLE , R.xml.preferences, false)
  
  //contains the preferences of the file "tribosmap.pref"
  lazy val preferences = context.getSharedPreferences("tribosmap.pref", 0)
 
  //the actual language
  def language: String = preferences.getString("languages","english")
  
  //the actual datafolder, is the rootdir of the application, can be used to write files
  def dataFolder: String = preferences.getString("dataFolder", context.getFilesDir + "/")
  
  //the folder used for tmp-data when loading data from the web
  def webCacheFolder: String = preferences.getString("webCacheFolder", context.getCacheDir + "/")
  
  //this folder contains the pictures of the camera
  def cameraCacheFolder: String = preferences.getString("cameraCacheFolder", context.getCacheDir + "/")
  
  //the starting folder when opening import-file dialogs
  def importFolder: String = preferences.getString("importFolder", context.getFilesDir + "/")
  
  //the satartingfolder whewen opening export dialogs
  def exportFolder: String = preferences.getString("exportFolder", context.getFilesDir + "/")
  

}
