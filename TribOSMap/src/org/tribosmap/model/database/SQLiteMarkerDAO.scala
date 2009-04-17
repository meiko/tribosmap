package org.tribosmap.model.database

import android.content.{Context, ContentValues}
import android.database.Cursor
import android.database.sqlite.{SQLiteOpenHelper, SQLiteDatabase}

import java.util.Date

import scala.collection.immutable.HashMap

import org.tribosmap.model.business.domain._
import org.tribosmap.model.business.repository.MarkerDAO
import org.tribosmap.model.math.geographic._

/**
 * SQLiteMarkerDAO is used by a service to
 * connect to the database and handle with marker-objects
 * 
 * @param accessor the DatabaseAdapter
 * @author Meiko Rachimow
 */
protected[database] class SQLiteMarkerDAO(accessor: SQLiteDAOAccessor)  
  extends SQLiteBaseDAO[Marker](accessor) with MarkerDAO {
  
  //the identifier and column numbers of the database objects
  private[this] val KEY_TRACEID = ("traceId", 1)
  private[this] val KEY_NAME = ("name", 2)
  private[this] val KEY_INFO = ("info", 3)
  private[this] val KEY_TYPE = ("type", 4)
  private[this] val KEY_TIME = ("date", 5)
  private[this] val KEY_IMAGE = ("imageFileName", 6)
  private[this] val KEY_LATITUDE = ("latitude", 7)
  private[this] val KEY_LONGITUDE = ("longitude", 8)
  private[this] val KEY_ALTITUDE = ("altitude", 9)
  
  /**
   * @see SQLiteBaseDAO
   */ 
  val DATABASE_TABLE = "marker"
  
  /**
   * @see SQLiteBaseDAO
   */ 
  val DATABASE_CREATE = "CREATE TABLE " + 
    DATABASE_TABLE +  " (" + 
    KEY_ROWID._1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
    KEY_TRACEID._1 + " INTEGER NOT NULL, " + 
    KEY_NAME._1 + " TEXT NOT NULL, " + 
    KEY_INFO._1 + " TEXT NOT NULL, " + 
    KEY_TYPE._1 + " INTEGER NOT NULL, " + 
    KEY_TIME._1 + " INTEGER NOT NULL, " + 
    KEY_IMAGE._1 + " TEXT, " + 
    KEY_LATITUDE._1 + " REAL NOT NULL, " + 
    KEY_LONGITUDE._1 + " REAL NOT NULL, " + 
    KEY_ALTITUDE._1 + " REAL NOT NULL);";
  
  /**
   * @param marker the to fill in the contantvalues
   * @return the contentvalues containig the database-fields of the object
   * @see SQLiteBaseDAO
   */
  protected def fillInitValues(marker: Marker) =  { 
    val initialValues = new ContentValues()
    val position = marker.getPosition()
    initialValues.put(KEY_TRACEID._1, marker.traceId)
    initialValues.put(KEY_NAME._1, marker.getName())
    initialValues.put(KEY_INFO._1, marker.getInformation())
    initialValues.put(KEY_TYPE._1, marker.typ.id)
    initialValues.put(KEY_TIME._1, marker.creationTime)
    initialValues.put(KEY_IMAGE._1, marker.getPhotoFileName)
    initialValues.put(KEY_LATITUDE._1, position.latLon.latitude)
    initialValues.put(KEY_LONGITUDE._1, position.latLon.longitude)
    initialValues.put(KEY_ALTITUDE._1, position.altitude)
    initialValues
  }
  
  /**
   * @param cursor the cursor contains a row of the database
   * @return the created object of type <code>Marker</code>
   * @see SQLiteBaseDAO
   */
  protected def createFromCursor(cursor: Cursor) = new Marker(
    cursor.getLong(KEY_ROWID._2), 
    cursor.getLong(KEY_TRACEID._2),
    cursor.getString(KEY_NAME._2),
    cursor.getString(KEY_INFO._2), 
    MarkerType(cursor.getInt(KEY_TYPE._2)), 
    (new Date(cursor.getLong(KEY_TIME._2))).getTime,
    cursor.getString(KEY_IMAGE._2),
    new Position( GeographicCoOrdinatePair(
      cursor.getDouble(KEY_LATITUDE._2), 
      cursor.getDouble(KEY_LONGITUDE._2)), 
      cursor.getDouble(KEY_ALTITUDE._2),
      Datum.WGS84Datum)
    )	
  
  /**
   * @param marker the marker to clone
   * @param id the id for the cloned Object
   * 
   * @return the created object of type <code>Marker</code>
   * @see SQLiteBaseDAO
   */
  protected def giveId(marker: Marker, id: Long) = new Marker(
    id,
    marker.traceId, 
    marker.getName(),
    marker.getInformation(),
    marker.typ,
    marker.creationTime,
    marker.getPhotoFileName,
    marker.getPosition()
  )
  
  /**
   * @param traceId the id of the trace
   * @return list of all markers in the trace with given id
   * 
   * @see MarkerDAO
   */
  def fetchAllMarkers(trace: Trace): List[Marker] = 
    fetchAllWhere(KEY_TRACEID._1 + "=" + trace.id)
  
  
  
  /**
   * @param trace the trace
   * @param fun the function to be applied
   * 
   * @see MarkerDAO
   */
  def foreachMarker(trace: Trace)
                   (fun: (Marker) ⇒ Unit) = {
    foreachWhere(KEY_TRACEID._1 + "=" + trace.id, fun)
  }
}
