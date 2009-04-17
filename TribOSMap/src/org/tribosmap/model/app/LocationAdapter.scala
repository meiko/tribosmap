package org.tribosmap.model.app

import android.location.LocationListener
import android.location.Location
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Bundle
import android.util.Log
import android.content.Context

/**
 * The LocationAdapter used get information of the gps receiver.
 * the GPS System Service will call the Methods in this Class,
 * if a new position was received.
 * @author Meiko Rachimow
 */
trait LocationAdapter extends LocationListener  {

  /**
   * Called when the location has changed.
   * @param location The new location, as a Location object.
   */
  override def onLocationChanged(loc : Location) {}
  
  /**
   * Called when the provider status changes.
   * @param provider the name of the location provider
   * @param status of the Provider
   * @param extras an optional Bundle which will contain provider specific
   * status variables.
   * <p> in the bundle with key: satellites - the number of satellites
   */
  override def onStatusChanged(provider : String, status : Int, extras : Bundle) {}

  /**
   * Called when the provider is enabled by the user.
   *
   * @param provider the name of the location provider
   */
  override def onProviderEnabled(provider : String) {}

  /**
   * Called when the provider is disabled by the user.
   *
   * @param provider the name of the location provider
   */
  override def onProviderDisabled(provider : String) {}
  
  /**
   * Must be implemented in the derived class,
   * is used to access the System service of the android system.
   * 
   * @param serviceId the id of the service
   * @return an android System Service for the given id
   */
  protected def getSystemService(serviceId: String): Object
  
  /**
   * @return the location manager (the android location service)
   */
  protected def getLocationService = 
    getSystemService(Context.LOCATION_SERVICE).asInstanceOf[LocationManager]
  
  /**
   * start listening to the gps updates
   */
  protected def startListening() {
    require(getLocationService.isProviderEnabled(
      LocationManager.GPS_PROVIDER), "GPS_PROVIDER is not enabled !")
    getLocationService.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this)
  }
  
  /**
   * end listening to the gps updates
   */    
  protected def endListening() {
    getLocationService.removeUpdates(this)
  }
        
}