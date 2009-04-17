package org.tribosmap.model.database

import android.database.sqlite.{SQLiteOpenHelper, SQLiteDatabase}
import android.util.Log
import android.content.Context
import org.tribosmap.model.business.repository.DAOAccessor
/**
 * The central adapter to the database
 * This class initializes the database access objects. (e.g. SQLiteMarkerDAO) 
 * In general - here is the connection to the database initialized.
 * This Class is used by the ServiceAccess to get access to the database access objects.
 * It works as an adapter for the <code>SQLiteOpenHelper</code>
 * when created the db the first time, it will create all tables etc.
 * @param context the application Context
 * @param databaseName the name of the database
 * @param databaseVersion the version of the database
 * @author Meiko Rachimow
 */
protected[model] final class SQLiteDAOAccessor (
  protected[database] val context: Context, 
  protected[database] val databaseName: String, 
  protected[database] val databaseVersion: Int) extends DAOAccessor {
  
  /**
   * the database access object for Marker objects
   */
  lazy val markerDAO = new SQLiteMarkerDAO(this)

  /**
   * the database access object for Trace objects
   */
  lazy val traceDAO = new SQLiteTraceDAO(this)

  /**
   * the database access object for TraceSegment objects
   */
  lazy val traceSegmentDAO = new SQLiteTraceSegmentDAO(this)

  /**
   * the database access object for TracePoint objects
   */
  lazy val tracePointDAO = new SQLiteTracePointDAO(this)

  /**
   * the database access object for GeoMap objects
   */
  lazy val geoMapDAO = new SQLiteGeoMapDAO(this)

  /**
   * the database access object for GeoReferencedPixel objects
   */
  lazy val geoReferencedPixelDAO = new SQLiteGeoReferencedPixelDAO(this)
  
}