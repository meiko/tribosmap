package org.tribosmap.model.database

import android.content.{Context, ContentValues}
import android.database.Cursor
import android.database.sqlite.{SQLiteOpenHelper, SQLiteDatabase}

import java.util.Date

import scala.collection.immutable.HashMap

import org.tribosmap.model.business.domain._
import org.tribosmap.model.business.repository.TraceDAO
import org.tribosmap.model.math.geographic.{Position, GeographicCoOrdinatePair}
import org.tribosmap.model.math.distance.Distance
import org.tribosmap.model.math.distance.MetricUnit.MilliMeter

/**
 * SQLiteTraceDAO is used by a service to
 * connect to the database and handle with Trace-objects
 * 
 * @param accessor the DatabaseAdapter
 * @author Meiko Rachimow
 */
protected[database] class SQLiteTraceDAO(accessor: SQLiteDAOAccessor)  
  extends SQLiteBaseDAO[Trace](accessor) with TraceDAO {
  
  //the identifier and column numbers of the database objects
  private[this] val KEY_NAME = ("name", 1)
  private[this] val KEY_INFO = ("info", 2)
  private[this] val KEY_TIME = ("time", 3)
  private[this] val KEY_SIZE = ("pointCount", 4)
  private[this] val KEY_DISTANCE = ("distance", 5)
  
  /**
   * @see SQLiteBaseDAO
   */ 
  val DATABASE_TABLE = "trace"
  
  /**
   * @see SQLiteBaseDAO
   */ 
  val DATABASE_CREATE = "CREATE TABLE " + 
    DATABASE_TABLE + " (" + 
    KEY_ROWID._1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
    KEY_NAME._1 + " TEXT NOT NULL, " + 
    KEY_INFO._1 + " TEXT NOT NULL, " + 
    KEY_TIME._1 + " INTEGER NOT NULL, " + 
    KEY_SIZE._1 + " INTEGER NOT NULL, " + 
    KEY_DISTANCE._1 + " REAL NOT NULL);";
  
  /**
   * @param trace the to fill in the contantvalues
   * @return the contentvalues containig the database-fields of the object
   * @see SQLiteBaseDAO
   */
  protected def fillInitValues(trace: Trace) =  { 
    val initialValues = new ContentValues()
    initialValues.put(KEY_NAME._1, trace.getName)
    initialValues.put(KEY_INFO._1, trace.getInformation)
    initialValues.put(KEY_TIME._1, trace.creationTime)
    initialValues.put(KEY_SIZE._1, trace.pointCount)
    initialValues.put(KEY_DISTANCE._1, trace.distance.milliMeterValue)
    initialValues
  }
  
  /**
   * @param cursor the cursor contains a row of the database
   * @return the created object of type <code>Trace</code>
   * @see SQLiteBaseDAO
   */
  protected def createFromCursor(cursor: Cursor) = new Trace(
    cursor.getLong(KEY_ROWID._2),
    cursor.getString(KEY_NAME._2),
    cursor.getString(KEY_INFO._2),
    (new Date(cursor.getLong(KEY_TIME._2))).getTime, 
    cursor.getInt(KEY_SIZE._2),
    Distance(cursor.getDouble(KEY_DISTANCE._2), MilliMeter)
  )
  
  /**
   * @param trace the marker to clone
   * @param id the id for the cloned Object
   * 
   * @return the created object of type <code>Trace</code>
   * @see SQLiteBaseDAO
   */
  protected def giveId(trace: Trace, id: Long) = new Trace(
    id,
    trace.getName,
    trace.getInformation,
    trace.creationTime,
    trace.pointCount,
    trace.distance
  )
  
  /**
   * @param segment the trace to delete
   * @return if deleted true
   * 
   * @see SQLiteBaseDAO
   */
  override def delete(trace: Trace) = {
    accessor.traceSegmentDAO.deleteAllSegments(trace)
    super.delete(trace)
  }
}
	