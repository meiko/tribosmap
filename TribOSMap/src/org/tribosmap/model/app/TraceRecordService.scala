package org.tribosmap.model.app

import java.util.Date
import android.app.Service
import android.util.Log
import android.content.Intent
import android.os.IBinder
import android.location.{LocationManager, Location}

import org.tribosmap.model.business.domain.{Trace, TraceSegment, Marker}
import org.tribosmap.model.math.geographic.{GeographicCoOrdinatePair, Position}
import org.tribosmap.model.math.distance.MetricUnit

/**
 * This Service will record a trace. To use it, it can be bound to an activity.
 * <p>
 * When it is bound the first time, it will instantiate the communication processing stuff.
 * It is important to unbind this Service when closing the application, because it is running 
 * independently from any activities as a demon.
 * <p>
 * The Service will record all given locations from the gps-receiver.
 * It uses the system service <code>LocationManager.GPS_PROVIDER</code>,
 * to receive that gps positions.
 * <p>
 * When starting a record, the service will create and save a Trace object, and a first TraceSegment object.
 * All received positions(points) are saved to the segments. It is possible to add a marker to a 
 * running record, then the last tracesegment of the recording trace will be saved,
 * and a new one will be created.
 * While recording, all created segments and points are saved to the repository. After stopping the record,
 * the trace will be saved if there are recorded points, or deleted if there are no recorded points.
 * 
 * For that reasons the ServiceAccess trait is used to access the TraceService.
 * <p>
 * This class contains the real Service in his Binder object.
 * This Binder is defined in an inner class, which was automatically created 
 * using 'aidl' (see Android documentation).
 * @author Meiko Rachimow
 */
class TraceRecordService extends Service 
  with LocationAdapter with ServiceAccess {

  val context = this
  
  /**
   * Nothing to do here
   * 
   * @see Service
   */
  override def onCreate {}
  
  /**
   * the actual recorded trace in this option
   */
  private[this] var traceOption: Option[Trace] = None
  
  /**
   * the actual trace segment
   */
  private[this] var traceSegmentOption: Option[TraceSegment] = None
  
  /**
   * is true when the service is recording a trace
   */
  private[this] var recording = false
  
  /**
   * the actual position
   */
  private[this] var actualPosition: Option[Position] = None
  
  /**
   * Is called when a new Location was delivered by the System Service
   * Here we will read the information from the given Location object,
   * and create a TracePoint which is saved into the repository.
   * 
   * @param location the new Location
   * 
   * @see LocationAdapter
   */
  override def onLocationChanged(location : Location) {
    
    val newPosition = new Position(
      new GeographicCoOrdinatePair(location.getLatitude, location.getLongitude),
      location.getAltitude,
      Preferences.standardDatum
    )
    
    traceOption match {
      case Some(trace: Trace) ⇒ {
        
        if(!traceSegmentOption.isDefined){
          val newTraceSegment = trace.addNewSegment
          newTraceSegment.size = 0
          traceSegmentOption = Some(newTraceSegment)
        }
        
        val locationTime = new Date(location.getTime)
        val traceSegment = traceSegmentOption.get
        
        traceSegment.addPoint(
          new Position(
            GeographicCoOrdinatePair(
              location.getLatitude, 
              location.getLongitude
            ), 
            location.getAltitude, Preferences.standardDatum), 
          location.getExtras().getInt("satellites"), 
          location.getBearing, 
          location.getSpeed, 
          location.getExtras().getFloat("hdop"),
          locationTime 
        )
        
        traceSegment.size += 1
        trace.pointCount += 1
        
        if(actualPosition.isDefined) {
          trace.distance += actualPosition.get.distanceTo(newPosition)
        }
        
        val traceFromDb = trace.reload()
        traceFromDb.pointCount = trace.pointCount
        traceFromDb.distance = trace.distance
        traceFromDb.save()
        traceSegment.save()
        actualPosition = Some(newPosition)
      }
      case None ⇒ 
        if(recording) 
          error("critical error no trace exists to record points")
    }			
  }
  
  /**
   * The binder is connected to the system service.
   * it implements the defined methods of the ITraceRecordService.aidl
   * interface.
   * 
   * @param intent the intent
   * @return the binder object
   * @see ITraceRecordService
   */
  override def onBind(intent : Intent): IBinder = 
    new ITraceRecordService.Stub() {

    /**
     * get the recording state,
     * if the result is true, the service is recording
     * 
     * @return the recording state
     */
    def isRecording = recording
    
    /**
     * the pointcount in the trace,
     * 
     * @return the point count of the record (trace)
     */
    def getPointCount = traceOption match {
      case Some(trace: Trace) ⇒ 
        trace.pointCount
      case None ⇒
        error("TraceRecordService not recording !")
    }
    
    /**
     * the distance in the trace (sum of distances between each point),
     * 
     * @return the distance of the record (trace)
     */
    def getActualDistanceInMeter = traceOption match {
      case Some(trace: Trace) ⇒ 
        trace.distance.to(MetricUnit.Meter).value
      case None ⇒
        error("TraceRecordService not recording !")
    }
    
    /**
     * start a record
     */
    def startRecord() {
      require(getLocationService.isProviderEnabled(
        LocationManager.GPS_PROVIDER), "GPS_PROVIDER is not enabled !")
      
      traceOption match {  
        case Some(trace: Trace) ⇒ 
          error("TraceRecordService already recording !")
        case None ⇒ {
          recording = true
          traceOption = Some(traceService.create("noName", "", new Date().getTime))
          startListening()
          Log.i(this.getClass.toString, "startRecord at position: " + 
                  getLocationService.getLastKnownLocation(
                    LocationManager.GPS_PROVIDER))
        }
      }
    }
    
    /**
     * stop a record
     */
    def stopRecord(): Unit = traceOption match {
      case Some(trace: Trace) ⇒ {
        
        actualPosition = None
        traceOption = None
        
        traceSegmentOption match {
          case Some(traceSegment) ⇒ {
            val traceFromDb = trace.reload
            traceFromDb.pointCount = trace.pointCount
            traceFromDb.distance = trace.distance
            traceFromDb.save()
            traceSegment.save()
            traceSegmentOption = None
          }
          case None ⇒ trace.delete
        }
        endListening()
        recording = false
        Log.i(this.getClass.toString, "stopRecord at position: " + 
                getLocationService.getLastKnownLocation(
                  LocationManager.GPS_PROVIDER) + 
                " with points: " + trace.pointCount)
      }
      case None ⇒ 
        error("there is no trace to save, probably not recording.")
    }
    
    /**
     * put a new marker into the record, a new segment will be added
     * to the trace.
     */
    def newMarker() = traceSegmentOption match {
      case Some(traceSegment) ⇒ {
        traceSegment.save()
        traceSegmentOption = None
      }
      case None ⇒ 
    }
    
    /**
     * @return the id of the actual trace (or None if no record exists)
     */
    def actualTraceId: Long = traceOption match {
      case Some(trace: Trace) ⇒ 
        trace.id
      case None ⇒ 
        error("there is no trace to get id, probably not recording.")
    }
  }
}
