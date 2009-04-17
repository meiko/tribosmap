package org.tribosmap.model.database

import android.content.{Context, ContentValues}
import android.database.Cursor
import android.database.sqlite.{SQLiteOpenHelper, SQLiteDatabase}

import org.tribosmap.model.business.domain._
import org.tribosmap.model.business.repository.GeoReferencedPixelDAO
import org.tribosmap.model.math._
import org.tribosmap.model.math.geographic.{UtmCoOrdinates, Hemisphere}
                                
/**
 * SQLiteGeoReferencedPixelDAO is used by a service to
 * connect to the database and handle with GeoReferencedPixel-objects
 * 
 * @param accessor the DatabaseAdapter
 * @author Meiko Rachimow
 */
protected[database] class SQLiteGeoReferencedPixelDAO(accessor: SQLiteDAOAccessor)  
  extends SQLiteBaseDAO[GeoReferencedPixel](accessor) with GeoReferencedPixelDAO {

  //the identifier and column numbers of the database objects
  private[this] val KEY_ROWID = ("_id", 0)
  private[this] val KEY_MAPID = ("mapId", 1)
  private[this] val KEY_PIXELX = ("pixelx", 2)
  private[this] val KEY_PIXELY = ("pixely", 3)
  private[this] val KEY_NORTHING = ("northing", 4)
  private[this] val KEY_EASTING = ("easting", 5)
  private[this] val KEY_ZONE = ("zone", 6)
  private[this] val KEY_HEMISPHERE = ("hemisphere", 7)

  /**
   * @see SQLiteBaseDAO
   */ 
  val DATABASE_TABLE = "geopixel"
  
  /**
   * @see SQLiteBaseDAO
   */ 
  val DATABASE_CREATE =
    "CREATE TABLE " + DATABASE_TABLE + " (" + 
    KEY_ROWID._1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
    KEY_MAPID._1 + " INTEGER NOT NULL, " + 
    KEY_PIXELX._1 + " INTEGER NOT NULL, " + 
    KEY_PIXELY._1 + " INTEGER NOT NULL, " + 
    KEY_NORTHING._1 + " REAL, " + 
    KEY_EASTING._1 + " REAL, " + 
    KEY_ZONE._1 + " INTEGER, " + 
    KEY_HEMISPHERE._1 + " TEXT);";

  /**
   * deletes all georeferenced pixel from the database,
   * which are connected with a map
   * 
   * @param map the GeoMap
   * @return <code>true</code> if the deletion was successful
   */
  private[database] def deleteAll(map: GeoMap) = 
    databaseWriteAccess {
      db: SQLiteDatabase ⇒  {
        db.delete(DATABASE_TABLE, 
                  KEY_MAPID._1 + "=" + map.id, null) > 0
      }
  }
  
  /**
   * @param map the GeoMap containing the needed GeoReferencedPixels
   * @return list of all GeoReferencedPixels in the map
   */
  def fetchAll(map: GeoMap): List[GeoReferencedPixel] = 
    fetchAllWhere(KEY_MAPID._1 + "=" + map.id)
  
  /**
   * @param pixel the GeoReferencedPixel to fill in the contentvalues
   * @return the contentvalues containig the database-fields of the object
   * @see SQLiteBaseDAO
   */
  protected def fillInitValues(point: GeoReferencedPixel) =  { 
    val initialValues = new ContentValues()
    initialValues.put(KEY_MAPID._1, point.mapId)
    initialValues.put(KEY_PIXELX._1, point.pixel.x)
    initialValues.put(KEY_PIXELY._1, point.pixel.y)
    initialValues.put(KEY_EASTING._1, point.utm.eastingD)
    initialValues.put(KEY_NORTHING._1, point.utm.northingD)
    initialValues.put(KEY_ZONE._1, point.utm.zoneNumber)
    initialValues.put(KEY_HEMISPHERE._1, point.utm.hemisphere.toString)
    initialValues
  }
               
  /**
   * @param cursor the cursor contains a row of the database
   * @return the created object of type <code>GeoReferencedPixel</code>
   * @see SQLiteBaseDAO
   */
  protected def createFromCursor(cursor: Cursor) = {
    new GeoReferencedPixel(
      cursor.getLong(KEY_ROWID._2),
      cursor.getLong(KEY_MAPID._2), 
      Vector2i(cursor.getInt(KEY_PIXELX._2), 
               cursor.getInt(KEY_PIXELY._2)),
      new UtmCoOrdinates(
        cursor.getDouble(KEY_NORTHING._2),
        cursor.getDouble(KEY_EASTING._2),
        cursor.getInt(KEY_ZONE._2),
        Hemisphere(cursor.getString(KEY_HEMISPHERE._2)),
        Preferences.standardDatum)
    )
  }
  
    
    	
  
  /**
   * @param point the GeoReferencedPixel to clone
   * @param id the id for the cloned Object
   * 
   * @return the created object of type <code>GeoReferencedPixel</code>
   * @see SQLiteBaseDAO
   */
  protected def giveId(point: GeoReferencedPixel, id: Long) = new GeoReferencedPixel(
    id,
    point.mapId, 
    point.pixel,
    point.utm
  )
    
}
