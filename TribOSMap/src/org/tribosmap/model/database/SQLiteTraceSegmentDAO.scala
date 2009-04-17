package org.tribosmap.model.database

import android.content.{Context, ContentValues}
import android.database.Cursor
import android.database.sqlite.{SQLiteOpenHelper, SQLiteDatabase}

import java.util.Date

import scala.collection.immutable.HashMap

import org.tribosmap.model.business.domain._
import org.tribosmap.model.business.repository.TraceSegmentDAO
import org.tribosmap.model.math.geographic.{Position, GeographicCoOrdinatePair}

/**
 * SQLiteTraceSegmentDAO is used by a service to
 * connect to the database and handle with TraceSegment-objects
 * 
 * @param accessor the DatabaseAdapter
 * @author Meiko Rachimow
 */
protected[database] class SQLiteTraceSegmentDAO(accessor: SQLiteDAOAccessor)  
  extends SQLiteBaseDAO[TraceSegment](accessor) 
  with TraceSegmentDAO {
    
  //the identifier and column numbers of the database objects
  private[this] val KEY_TRACEID = ("traceID", 1)
  private[this] val KEY_SIZE = ("size",2)
  
  /**
   * @see SQLiteBaseDAO
   */ 
  val DATABASE_TABLE = "tracesegment"
  
  /**
   * @see SQLiteBaseDAO
   */ 
  val DATABASE_CREATE = "CREATE TABLE " + 
    DATABASE_TABLE + " (" + 
    KEY_ROWID._1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
    KEY_TRACEID._1 + " INTEGER NOT NULL, " + 
    KEY_SIZE._1 + " INTEGER NOT NULL);"
  
  /**
   * delete all segments of a trace
   * @param trace trace which contains the segments to delete
   */
  private[database] def deleteAllSegments(trace: Trace) = {
    foreachSegment(trace){ segment ⇒ 
      delete(segment)  
    }
  }
  
  /**
   * @param segment the segment to fill in the contantvalues
   * @return the contentvalues containig the database-fields of the object
   * @see SQLiteBaseDAO
   */
  protected def fillInitValues(segment: TraceSegment) =  { 
    val initialValues = new ContentValues()
    initialValues.put(KEY_TRACEID._1, segment.traceID)
    initialValues.put(KEY_SIZE._1, segment.size)
    initialValues
  }
  
  /**
   * @param cursor the cursor contains a row of the database
   * @return the created object of type <code>TraceSegment</code>
   * @see SQLiteBaseDAO
   */
  protected def createFromCursor(cursor: Cursor) = new TraceSegment(
    cursor.getLong(KEY_ROWID._2),
    cursor.getLong(KEY_TRACEID._2),
    cursor.getLong(KEY_SIZE._2)
  )
  
  /**
   * @param segment the segment to clone
   * @param id the id for the cloned Object
   * 
   * @return the created object of type <code>TraceSegment</code>
   * @see SQLiteBaseDAO
   */
  protected def giveId(segment: TraceSegment, id: Long) = new TraceSegment(
    id,
    segment.traceID,
    segment.size
  )
  
  /**
   * @param trace the trace containing the needed segments
   * @return list of all segments in the trace
   * 
   * @see TraceSegmentDAO
   */
  def fetchAllSegments(trace: Trace): List[TraceSegment] = 
    fetchAllWhere(KEY_TRACEID._1 + "=" + trace.id)
  
  /**
   * @param trace the trace
   * @param fun the function to be applied
   * 
   * @see TraceSegmentDAO
   */
  def foreachSegment(trace: Trace)
                    (fun: (TraceSegment) ⇒ Unit) = {
    foreachWhere(KEY_TRACEID._1 + "=" + trace.id, fun)
  }
                    
  /**
   * @param segment the segment to delete
   * @return if deleted true
   * 
   * @see SQLiteBaseDAO
   */
  override def delete(segment: TraceSegment) = {
    accessor.tracePointDAO.deleteAllPoints(segment)
    super.delete(segment)
  }
}
