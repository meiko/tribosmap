package org.tribosmap

import java.io.File
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.{DisplayMetrics, Log}
import android.widget.Toast
import scala.actors.Actor.actor
import org.tribosmap.frontend.common.{ActivityHelper, Messager}
import org.tribosmap.frontend.MainActivity
import org.tribosmap.model.ServiceAccess

/**
 * This activity will start the MainActivity, it will do some
 * init things... starting the application, check folders, read properties, 
 * check datasbase and screen size
 * 
 * @author Meiko Rachimow
 */
class StartActivity extends Activity with ActivityHelper with ServiceAccess {
  
  override def onCreate(savedInstanceState : Bundle) = tryCatch {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.init)

    Messager.makeMessageShort(this, "starting... ")
    
    //check display size
    val dm = new DisplayMetrics()
    getWindowManager().getDefaultDisplay().getMetrics(dm)
    val screenHeight = dm.heightPixels
    val screenWidth = dm.widthPixels
    
    //check language
    val language = preferences.language
    //... TODO:
    
    //check and create app folders
    val folders = List(
      new File(preferences.dataFolder),
      new File(preferences.webCacheFolder),
      new File(preferences.cameraCacheFolder),
      new File(preferences.exportFolder),
      new File(preferences.importFolder))
    
    folders.filter(! _.exists).foreach( file ⇒ {
      if(! file.mkdir) {
        error("cannot create directory: " + file)
      }
    })
    
  /*
   * we init the actor system.
   * TODO: the reason to do this, is a bug of the Scala / Android combination:
   * if the application is immediately starting after booting the Android system,
   * it is possible, that the actor system will not start correctly.
   * With this hack the application will not start when this happened.
   * (Note: it seems, that it happened one time after 20 fast boot-starts)
   */
    actor { Log.i("Actors", "Initializing...") } 
    
    finish()
    startActivity(new Intent(this, classOf[MainActivity]))
  }
}

