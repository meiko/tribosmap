package org.tribosmap.frontend.map


import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.{Menu, MenuItem, MotionEvent, KeyEvent}
import android.content.{IntentFilter, Context}
import android.location.{Location, LocationManager}
import android.widget.{TextView, CheckBox, CompoundButton}
import android.widget.CompoundButton.OnCheckedChangeListener
import android.graphics.Bitmap
import java.text.DecimalFormat

import org.tribosmap.model.math.geographic.GeographicCoOrdinatePair
import org.tribosmap.frontend.common._
import org.tribosmap.model.math._
import org.tribosmap.model.business.domain.GeoMap
import org.tribosmap.model.app.LocationAdapter
import org.tribosmap.model.ServiceAccess
import org.tribosmap.model.app.maptiling._

import org.tribosmap.model.math._
import org.tribosmap.Preferences;
import org.tribosmap.model.ServiceAccess
import org.tribosmap.model.math.geographic._
import org.tribosmap.model.app.imageloader.{ImageLoader, ImageLoadingJob}
import org.tribosmap.model.app.maptiling._

/**
 * This Activity will display a Map. It uses a MapView with a given GeoMap.
 * If no GeoMap is given in the bundle, it will display the "Openstreetmap online map".
 * The activity handles user events or gps events.
 * <p>
 * The Key for a GeoMap in the bundle is: <code>MapComCodes.MAP_PARAMETER_ID</code><br/>
 * All user events to move or zoom the map are forwarded to the MapView.
 * The informations about the actual position or zoomlevel are displayed in TextViews.
 * The MapActivity is using the LocationAdapter, to get the actual position.
 * The user can auto-center the map to the actual position. And he can show or hide
 * all markers, which are displayed when they are inside the actual displayed map section.
 * @author Meiko Rachimow
 */
class MapViewActivity extends Activity 

  with ActivityHelper 
  with LocationAdapter 
  with ServiceAccess {
  
  ////////////////////////////////////////////////////////
  //View Elements (Attributes): 
  //the following elements are defined in the xml layout file.
  //they are bound lazy, to fetch them after the Activity was started
  ////////////////////////////////////////////////////////
  
  private[this] lazy val mapView: MapView = getView(R.id.view)
  private[this] lazy val lblZoom: TextView = getView(R.id.lblZoom)
  private[this] lazy val checkCenter: CheckBox = getView(R.id.checkCenter)
  private[this] lazy val checkMarker: CheckBox = getView(R.id.checkMarker)
  private[this] lazy val lblLatitude: TextView = getView(R.id.lblLatitude)
  private[this] lazy val lblLongitude: TextView = getView(R.id.lblLongitude)
  private[this] lazy val lblAltitude: TextView = getView(R.id.lblAltitude)
  
  ////////////////////////////////////////////////////////
  //Other Attributes: 
  ////////////////////////////////////////////////////////
    
  /**
   * the used map algorithm
   */
  private[this] lazy val mapAlgorithm : MapAlgorithm = {
    /*
    //THIS crashes the compiler with 2.7.2.final
    val bundle = getIntent.getExtras
    val serial = bundle.getSerializable(MapComCodes.MAP_PARAMETER_ID)
    if(bundle != null && serial != null) {
      val map = serial.asInstanceOf[GeoMap]
      new OfflineUtmMap(map, map.getAllReferencedPixels)
    } else {
      new SlippyMap
    }
    */
    val bundle = getIntent.getExtras
    //TODO: support more algorithms - selection should depend on the map projection
    if(bundle != null) {
      val serial = bundle.getSerializable(MapComCodes.MAP_PARAMETER_ID)
      if(serial != null){
        val map = serial.asInstanceOf[GeoMap]
        new OfflineUtmMap(map, map.getAllReferencedPixels)
      }else {
        new SlippyMap
      }
    } else {
      new SlippyMap
    }
  }
  
  /**
   * the coordinate converter for the used map algorithm
   */
  private[this] lazy val converter = 
    new CoordConverter(mapAlgorithm.datum)
  
  /**
   * the coordinate converter for the used map algorithm
   */
  private[this] var zoom = 0
  
  /**
   * the geographic positions for the markers
   */
  private[this] lazy val markerPositions = 
    markerService.getAllMarkers.map( marker ⇒ marker.getPosition.latLon)
  
  /**
   * the geographic position of the center of the mapview
   */
  private[this] var centerLatLon = 
    GeographicCoOrdinatePair(61.0, 10.0)
  
  /**
   * the actual geographic position
   */
  private[this] var actualPositionLatLon = 
    GeographicCoOrdinatePair(61.0, 10.0)

    
  /**
   * the last user click position (in pixels opn the window)
   */
  private[this] var lastPoint = Vector2d(-1, -1)
  
  /**
   * if true, the map will be auto-centered to the actual position
   */
  private[this] var centerMapToActualposition = true
  
  ////////////////////////////////////////////////////////
  //Actions
  ////////////////////////////////////////////////////////
  
  /**
   * zoom in the view by one level
   */
  private[this] def zoomIn() {
    val result = mapAlgorithm.zoomIn(zoom)
    zoom = result._2
    mapView.setScale(result._1)
    newPosition(true)
  }

  /**
   * zoom out the view by one level
   */
  private[this] def zoomOut() {
    val result = mapAlgorithm.zoomOut(zoom)
    zoom = result._2
    mapView.setScale(result._1)
    newPosition(true)
  }
  
  /**
   * move the mapview by this vector (containing meter values)
   * @param delta the vector containing meter values (+x is east +y is north)
   */
  private[this] def move(delta : Vector2d) {
    
    mapView.onMove()
    centerLatLon = mapAlgorithm.computeLatLonAfterMove(
      zoom, centerLatLon, delta)
    newPosition(false)
  }
  
  /**
   * Center the MapView to the actual geographic position
   */
  private[this] def centerToActualLocation() {
    centerLatLon = actualPositionLatLon
    newPosition(false)
  }
  
  /**
   * repaints the mapview
   */
  private[this] def repaintMapView(){
    mapView.redraw(
      
      mapAlgorithm.getWindowPositionFor(
        zoom, actualPositionLatLon, centerLatLon, mapView.windowSizeD2),
      
      for (mpos <- markerPositions ) 
        yield mapAlgorithm.getWindowPositionFor(
          zoom, mpos, centerLatLon, mapView.windowSizeD2)
    )
  }
  
  /**
   * a new position, create new jobs to load the images...
   * @param switchZoom true if there is also a new zoomlevel
   */
  private[this] def newPosition(switchZoom: Boolean) = {
    val jobTiles = mapAlgorithm.newPosition(
      zoom, centerLatLon, mapView.windowSizeD2, mapView.tiles)
    jobTiles.foreach(tile ⇒ {
      tile.bitmap = null
      val tileUri = tile.tileUri
      def loadDrawableCallBack(cachedBitmap: Bitmap) {
        if(tile.tileUri.equals(tileUri)) {
          tile.bitmap = cachedBitmap  
          mapView.onNewTile(switchZoom, tile)
        }
      }
      val jobOption = mapAlgorithm.createJob(
        tile, loadDrawableCallBack, preferences)
      
      jobOption match {
        case Some(job) ⇒ ImageLoader.loadImage(job)
        case None ⇒ 
      }
      
    })
    repaintMapView()
  }
  
  ////////////////////////////////////////////////////////
  //Init and other Overridden Memebers
  ////////////////////////////////////////////////////////
    
  /**
   * Init the Activity, connect the buttons to the actions,<br/>
   * set the layout from xml,<br/>
   * init the other views, read the map from the bundle
   * 
   * @param savedInstanceState the global Bundle
   * 
   * @see Activity
   */
  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.mapview)

    
    checkMarker.setFocusable(false)
    checkMarker.setChecked(true)
    
    checkCenter.setFocusable(false)
    checkCenter.setChecked(true)
    
    checkCenter.setOnCheckedChangeListener( new OnCheckedChangeListener() {
      override def onCheckedChanged(
        buttonView: CompoundButton, isChecked: Boolean) {
        centerMapToActualposition = isChecked
        if(centerMapToActualposition) {
          centerToActualLocation()
        }
      }
    })
    
    checkMarker.setOnCheckedChangeListener( new OnCheckedChangeListener() {
      override def onCheckedChanged(
        buttonView: CompoundButton, isChecked: Boolean) {
        mapView.setShowMarker(isChecked)
        repaintMapView()
      }
    })
    zoom = mapAlgorithm.initialZoom
    lblZoom.setText(zoom.toString)
    newPosition(false)
    startListening()
  }
  
  /**
   * @see Activity
   */
  override def onDestroy {
    super.onDestroy()
    endListening()
  }
  
  /**
   * @see Activity
   * @param event the MotionEvent
   */
  override def onTrackballEvent(event : MotionEvent) = {
    onTouchEvent(event)
  }
  
  /**
   * @see Activity
   * @param event the MotionEvent
   */
  override def onTouchEvent(event : MotionEvent) = {
    val newPoint = Vector2d(event.getX.toInt, event.getY.toInt)
    if((lastPoint - newPoint).length > 5) {
      event.getAction match {
        case MotionEvent.ACTION_MOVE ⇒ move(lastPoint - newPoint)
        case _ ⇒
      }
      lastPoint = newPoint
    }
    true
  }
  
  /**
   * @see LocationAdapter
   * @param loc the Location
   */
  override def onLocationChanged(loc : Location) {
    val format = new DecimalFormat();
    format.setMaximumFractionDigits(5);
    lblLatitude.setText(format.format(loc.getLatitude) + "°N")
    lblLongitude.setText(format.format(loc.getLongitude) + "°E")
    format.setMaximumFractionDigits(1);
    lblAltitude.setText(format.format(loc.getAltitude) + " m")
    actualPositionLatLon = GeographicCoOrdinatePair(
      loc.getLatitude(), loc.getLongitude())
    if(centerMapToActualposition) {
      centerToActualLocation()
    }
    repaintMapView()
  }

  /**
   * @see Activity
   * @param keyCode the KeyCode
   * @param event the KeyEvent
   */
  override def onKeyDown(keyCode : Int, event: KeyEvent) =  {
    
    keyCode match {
      case KeyEvent.KEYCODE_DPAD_UP ⇒ {
        zoomIn
        lblZoom.setText(zoom.toString)
        true
      }
      case KeyEvent.KEYCODE_DPAD_DOWN ⇒ {
        zoomOut
        lblZoom.setText(zoom.toString)
        true
      }
      case KeyEvent.KEYCODE_MENU ⇒ {
        openOptionsMenu
        true
      }
      case _ ⇒ super.onKeyDown(keyCode, event)
    }
    
  }
}