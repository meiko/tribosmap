package org.tribosmap.model.database

import android.content.{Context, ContentValues}
import android.database.Cursor
import android.database.sqlite.{SQLiteOpenHelper, SQLiteDatabase}

import java.util.Date

import scala.collection.immutable.HashMap

import org.tribosmap.model.business.domain._
import org.tribosmap.model.business.repository.TracePointDAO
import org.tribosmap.model.math.geographic._


/**
 * SQLiteTracePointDAO is used by a service to
 * connect to the database and handle with TracePoint-objects
 * 
 * @param accessor the DatabaseAdapter
 * @author Meiko Rachimow
 */
protected[database] class SQLiteTracePointDAO(accessor: SQLiteDAOAccessor)  
  extends SQLiteBaseDAO[TracePoint](accessor) with 
    TracePointDAO {   
  
  //the identifier and column numbers of the database objects
  private[this] val KEY_SEGMENTID = ("segmentID", 1)
  private[this] val KEY_TIME = ("time", 2)
  private[this] val KEY_LATITUDE = ("latitude", 3)
  private[this] val KEY_LONGITUDE = ("longitude", 4)
  private[this] val KEY_ALTITUDE = ("altitude", 5)
  private[this] val KEY_SAT = ("satellites", 6)
  private[this] val KEY_COURSE = ("course", 7)
  private[this] val KEY_SPEED = ("speed", 8)
  private[this] val KEY_HDOP = ("hdop", 9)
  
  /**
   * @see SQLiteBaseDAO
   */ 
  val DATABASE_TABLE = "tracepoint"
  
  /**
   * @see SQLiteBaseDAO
   */ 
  val DATABASE_CREATE = "CREATE TABLE " + 
    DATABASE_TABLE + " (" + 
    KEY_ROWID._1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
    KEY_SEGMENTID._1 + " INTEGER NOT NULL," + 
    KEY_TIME._1 + " INTEGER NOT NULL," + 
    KEY_LATITUDE._1 + " REAL NOT NULL, " + 
    KEY_LONGITUDE._1 + " REAL NOT NULL, " + 
    KEY_ALTITUDE._1 + " REAL NOT NULL," + 
    KEY_SAT._1 + " INTEGER NOT NULL," + 
    KEY_COURSE._1 + " REAL NOT NULL," + 
    KEY_SPEED._1 + " REAL NOT NULL," + 
    KEY_HDOP._1 + " REAL);"
  
  /**
   * deletes all tracePoints from the database,
   * which are connected with a given tracesegment
   * 
   * @param segment the TraceSegment
   * @return <code>true</code> if the deletion was successful
   */
  private[database] def deleteAllPoints(segment: TraceSegment) = 
    databaseWriteAccess {
      db: SQLiteDatabase ⇒  {
        db.delete(DATABASE_TABLE, KEY_SEGMENTID._1 + 
                    "=" + segment.id, null) > 0
      }
  }
  
  /**
   * @param segment the TraceSegment
   * @param fun the function to be applied
   * 
   * @see TracePointDAO
   */
  def foreachPoint(segment: TraceSegment)
                  (fun: (TracePoint) ⇒ Unit) = {
    foreachWhere(KEY_SEGMENTID._1 + "=" + segment.id, fun)
  }
  
  /**
   * @param obj the object of type <code>T</code> to create
   * @return the id
   * 
   * @see TracePointDAO
   */
  def createFast(obj: TracePoint) =  databaseWriteAccess { 
    db: SQLiteDatabase ⇒  {
      db.insert(DATABASE_TABLE, 
                null, 
                fillInitValues(obj) )
    }	
  }	
  
  /**
   * @param tracePoint the to fill in the contantvalues
   * @return the contentvalues containig the database-fields of the object
   * @see SQLiteBaseDAO
   */
  protected def fillInitValues(tracePoint: TracePoint) =  { 
    val initialValues = new ContentValues()
    val position = tracePoint.position
    initialValues.put(KEY_SEGMENTID._1, tracePoint.segmentID)
    initialValues.put(KEY_LATITUDE._1, position.latLon.latitude)
    initialValues.put(KEY_LONGITUDE._1, position.latLon.longitude)
    initialValues.put(KEY_ALTITUDE._1, position.altitude)
    initialValues.put(KEY_TIME._1, tracePoint.time)
    initialValues.put(KEY_SAT._1, tracePoint.satellites)
    initialValues.put(KEY_COURSE._1, tracePoint.course)
    initialValues.put(KEY_SPEED._1, tracePoint.speed)
    initialValues.put(KEY_HDOP._1, tracePoint.hdop)
    initialValues
  }
  
  /**
   * @param cursor the cursor contains a row of the database
   * @return the created object of type <code>TracePoint</code>
   * @see SQLiteBaseDAO
   */
  protected def createFromCursor(cursor: Cursor) = new TracePoint(
    cursor.getLong(KEY_ROWID._2),
    cursor.getLong(KEY_SEGMENTID._2),
    (new Date(cursor.getLong(KEY_TIME._2)).getTime),
    new Position(GeographicCoOrdinatePair(
      cursor.getDouble(KEY_LATITUDE._2),
      cursor.getDouble(KEY_LONGITUDE._2)),
      cursor.getDouble(KEY_ALTITUDE._2), 
      Datum.WGS84Datum
    ),	
    cursor.getInt(KEY_SAT._2),
    cursor.getFloat(KEY_COURSE._2),
    cursor.getFloat(KEY_SPEED._2),
    cursor.getFloat(KEY_HDOP._2)
  )
    
  /**
   * @param point the point to clone
   * @param id the id for the cloned Object
   * 
   * @return the created object of type <code>TracePoint</code>
   * @see SQLiteBaseDAO
   */
  protected def giveId(point: TracePoint, id: Long) = new TracePoint(
    id,
    point.segmentID,
    point.time,
    point.position,
    point.satellites,
    point.course,
    point.speed,
    point.hdop
  )
}
