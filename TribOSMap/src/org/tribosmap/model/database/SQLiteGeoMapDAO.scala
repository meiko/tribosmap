package org.tribosmap.model.database

import android.content.{Context, ContentValues}
import android.database.Cursor
import android.database.sqlite.{SQLiteOpenHelper, SQLiteDatabase}

import org.tribosmap.model.business.domain._
import org.tribosmap.model.business.repository.GeoMapDAO
import org.tribosmap.model.math._
import org.tribosmap.model.math.geographic.Datum

/**
 * SQLiteGeoMapDAO is used by a service to
 * connect to the database and handle with map-objects
 * 
 * @param accessor the DatabaseAdapter
 * @author Meiko Rachimow
 */
protected[database] class SQLiteGeoMapDAO(accessor: SQLiteDAOAccessor)  
  extends SQLiteBaseDAO[GeoMap](accessor) with GeoMapDAO {

  //the identifier and column numbers of the database objects
  private[this] val KEY_NAME = ("name", 1)
  private[this] val KEY_FILENAME = ("filename", 2)
  private[this] val KEY_SIZEX = ("sizex", 3)
  private[this] val KEY_SIZEY = ("sizey", 4)
  private[this] val KEY_DATUM = ("datum", 5)
  private[this] val KEY_PROJECTION = ("projection", 6)
  private[this] val KEY_TIME = ("date", 7)
  
  /**
   * @see SQLiteBaseDAO
   */ 
  val DATABASE_TABLE = "map"
  
  /**
   * @see SQLiteBaseDAO
   */ 
  val DATABASE_CREATE =
    "CREATE TABLE " + DATABASE_TABLE + " (" + 
    KEY_ROWID._1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
    KEY_NAME._1 + " TEXT NOT NULL, " + 
    KEY_FILENAME._1 + " TEXT NOT NULL, " + 
    KEY_SIZEX._1 + " INTEGER NOT NULL, " + 
    KEY_SIZEY._1 + " INTEGER NOT NULL, " + 
    KEY_DATUM._1 + " TEXT NOT NULL, " + 
    KEY_PROJECTION._1 + " TEXT NOT NULL, " + 
    KEY_TIME._1 + " INTEGER NOT NULL);";
  
  /**
   * @param map the GeoMap to fill in the contentvalues
   * @return the contentvalues containig the database-fields of the object
   * @see SQLiteBaseDAO
   */
  protected def fillInitValues(map: GeoMap) =  { 
    val initialValues = new ContentValues()
    initialValues.put(KEY_NAME._1, map.getName)
    initialValues.put(KEY_FILENAME._1, map.fileName)
    initialValues.put(KEY_SIZEX._1, map.imageSize.x)
    initialValues.put(KEY_SIZEY._1, map.imageSize.y)
    initialValues.put(KEY_DATUM._1, map.datum.name)
    initialValues.put(KEY_PROJECTION._1, map.projection)
    initialValues.put(KEY_TIME._1, map.time)
    initialValues
  }
               
  /**
   * @param cursor the cursor contains a row of the database
   * @return the created object of type <code>GeoMap</code>
   * @see SQLiteBaseDAO
   */
  protected def createFromCursor(cursor: Cursor) = {
    new GeoMap(
      cursor.getLong(KEY_ROWID._2),
      cursor.getString(KEY_NAME._2), 
      cursor.getString(KEY_FILENAME._2),
      new Vector2i(
        cursor.getInt(KEY_SIZEX._2),
        cursor.getInt(KEY_SIZEY._2)
      ),
      Datum(cursor.getString(KEY_DATUM._2)), 
      cursor.getString(KEY_PROJECTION._2), 
      cursor.getLong(KEY_TIME._2))
  }
    
    	
  
  /**
   * @param map the GeoMap to clone
   * @param id the id for the cloned Object
   * 
   * @return the created object of type <code>GeoMap</code>
   * @see SQLiteBaseDAO
   */
  protected def giveId(map: GeoMap, id: Long) = new GeoMap(
    id,
    map.getName,
    map.fileName,
    map.imageSize,
    map.datum,
    map.projection,
    map.time
  )
  
  /**
   * @param map the GeoMap to delete
   * @return if deleted true
   * 
   * @see SQLiteBaseDAO
   */
  override def delete(map: GeoMap) = {
    accessor.geoReferencedPixelDAO.deleteAll(map)
    super.delete(map)
  }
}
