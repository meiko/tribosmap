package org.tribosmap.model.database

import android.content.{Context, ContentValues}
import android.database.Cursor
import android.database.sqlite.{SQLiteOpenHelper, SQLiteDatabase}
import android.util.Log
import org.tribosmap.model.business.repository.BaseDAO


/** 
 * SQLiteBaseDAO is the baseclass for all repository classes with 
 * access to the database. 
 * Contains some methods to create, update, delete and fetch objects
 * from the database. 
 * It needs a Typeparameter where the type must contain 
 * a variable <code>id: Long</code>
 * 
 * @param accessor the DatabaseAdapter
 * @author Meiko Rachimow
 */
protected[database] abstract class SQLiteBaseDAO[T<: {val id: Long}](
  accessor: SQLiteDAOAccessor) extends BaseDAO[T] { 
  
  
  ////////////////////////////////////////////////////////
  //public
  ////////////////////////////////////////////////////////
    
  /**
   * the name of the database table
   */
  val DATABASE_TABLE: String

  /**
   * the sql statement to create the database-table
   */
  val DATABASE_CREATE: String
    
  /**
   * @param fun the function applied to all found objects
   * 
   * @see BaseDAO
   */
  def foreach(fun: (T) ⇒ Unit) = foreachWhere(null, fun)
  
  /**
   * @param id the id of the object
   * @return the object of type <code>T</code>
   * 
   * @see BaseDAO
   */
  def fetch(id: Long): T = fetchAllWhere(KEY_ROWID._1 + "=" + id).first
  
  /**
   * @return the objects of type <code>T</code> as a <code>List[T]</code>
   * 
   * @see BaseDAO
   */
  def fetchAll(): List[T] = fetchAllWhere(null)
  

  
  /**
   * @param obj the object of type <code>T</code> to delete
   * @return <code>true</code> if the deletion was successful
   * 
   * @see BaseDAO
   */
  def delete(obj: T): Boolean =  databaseWriteAccess {
    db: SQLiteDatabase ⇒ 
      db.delete(DATABASE_TABLE, KEY_ROWID._1 + "=" + obj.id, null) > 0
  }
  
  /**
   * @param obj the object of type <code>T</code> to create
   * @return the saved object of type <code>T</code> with the correct id
   * 
   * @see BaseDAO
   */
  def create(obj: T): T =  databaseWriteAccess { 
    
    val initValues = fillInitValues(obj)
    
    assume(!initValues.containsKey(KEY_ROWID._1), 
           "initvalues must not contain the field ID" +
           "when creating a new objet in the database !")
    
    db: SQLiteDatabase ⇒  {
      giveId(obj, db.insert(DATABASE_TABLE, 
                            null, 
                            initValues ))
    }
  }
  
  /**
   * @param obj the object of type <code>T</code> to update
   * @return <code>true</code> if the update was successful
   * 
   * @see BaseDAO
   */
  def update(obj: T): Boolean =  databaseWriteAccess { 
    db: SQLiteDatabase ⇒  {
      db.update(DATABASE_TABLE, 
                fillInitValues(obj), 
                KEY_ROWID._1 + "=" + obj.id, 
                null) > 0;
    }
  }
  

  ////////////////////////////////////////////////////////
  //protected
  ////////////////////////////////////////////////////////
  
  /**
   * the keymapping for id, (the id is the first column ever)
   */
  protected val KEY_ROWID = ("_id", 0)
  
  /**
   * create an object of type <code>T</code> by using the database-Cursor
   * @param cursor the cursor contains a row of the database
   * @return the created object of type <code>T</code>
   */
  protected def createFromCursor(cursor: Cursor) : T
  
  /**
   * fill the fields of an object into the contentvalues
   * @param obj the to fill in the contantvalues
   * @return the contentvalues containig the database-fields of the object
   */
  protected def fillInitValues(obj: T): ContentValues
  
  /**
   * Clones an object and give the cloned object an id.
   * 
   * @param obj the obj to clone
   * @param id the id for the cloned Object
   * @return the cloned object of type <code>T</code>
   */
  protected def giveId(obj: T, id: Long) : T
  
  /**
   * This method loads all objects of type <code>T</code> from the database,
   * by using a given where-condition.
   * 
   * @param whereString the where-condition, like <code>Columnname + "=" + Value</code>
   * @return list of all objects (<code>List[T]</code>)
   */
  protected def fetchAllWhere(whereString: String): List[T] = {
    
    val result = 
      databaseReadAccess(null, whereString, null, null, null, null) { 
      
      cursor: Cursor  ⇒  { 
        
        assume(cursor != null)
        (0 until cursor.getCount).map[T] (_ ⇒ {
          cursor.moveToNext
          createFromCursor(cursor)
        }).toList
      }
    }
    result match {
      case Some(list) ⇒ list
      case None ⇒ List()
    }
  }
  
  /**
   * Apply a function to all elements of type T in the database, with a given where-condition
   * 
   * @param whereString the where-condition, like <code>Columnname + "=" + Value</code>
   * @param fun the function applied to all found objects
   */
  protected def foreachWhere(whereString: String, fun: (T) ⇒ Unit) = {
    databaseReadAccess(null, whereString, null, null, null, null){
      cursor: Cursor ⇒  {
        assume(cursor != null)
        (0 until cursor.getCount).foreach( index ⇒ {
          cursor.moveToNext
          fun(createFromCursor(cursor))
        })
      }
    }
  }

  /**
   * Éncapsulates the write access to the database,
   * usage example:
   * <code>
   * databaseWriteAccess { 
   *   db: SQLiteDatabase ⇒  {
   *     //write to the db, use the object db
   *   }
   * }
   * </code>
   * (see Loan Pattern)
   * 
   * @param fun a Function of type <code>SQLiteDatabase ⇒ A</code>, 
   *            where the type <code>A</code> is the resulttype,
   *            will be a closure and can use the database
   * 
   * @return the result of type <code>A</code> (the result of the given function)
   */
  protected def databaseWriteAccess[A](fun: (SQLiteDatabase) ⇒ A ) : A = {
    val db = dbAccess.getWritableDatabase
    try {
      fun(db)
    } finally {
      dbAccess.close
    }
  }
  
  /**
   * This method encapsulates the read access to the database.
   * It will start a sql-query with given parameters,
   * and then it will call the given parameter-function with the
   * resulting cursor-object.
   * 
   * usage example:
   * <code>
   * databaseReadAccess(Table, whereString, null, null, null, null) { 
   *   db: SQLiteDatabase ⇒  {
   *     //use the result by the query
   *   }
   * }
   * </code>
   * (see Loan Pattern)
   * 
   * The function will return the result of the parameter-function
   * for further documentations here the parameterlist (taken from android documentation: SqliteDatabase)
   * @param columns A list of which columns to return. Passing null will
   *            return all columns, which is discouraged to prevent reading
   *            data from storage that isn't going to be used.
   * @param selection A filter declaring which rows to return, formatted as an
   *            SQL WHERE clause (excluding the WHERE itself). Passing null
   *            will return all rows for the given table.
   * @param selectionArgs You may include ?s in selection, which will be
   *         replaced by the values from selectionArgs, in order that they
   *         appear in the selection. The values will be bound as Strings.
   * @param groupBy A filter declaring how to group rows, formatted as an SQL
   *            GROUP BY clause (excluding the GROUP BY itself). Passing null
   *            will cause the rows to not be grouped.
   * @param having A filter declare which row groups to include in the cursor,
   *            if row grouping is being used, formatted as an SQL HAVING
   *            clause (excluding the HAVING itself). Passing null will cause
   *            all row groups to be included, and is required when row
   *            grouping is not being used.
   * @param orderBy How to order the rows, formatted as an SQL ORDER BY clause
   *            (excluding the ORDER BY itself). Passing null will use the
   *            default sort order, which may be unordered.
   * 
   * @param fun a Function of type <code>Cursor ⇒ A</code>, 
   *            where the type <code>A</code> is the resulttype,
   *            will be a closure and can use the given cursor
   * 
   * @return the result of type <code>A</code> (the result of the given function)
   */
  protected def databaseReadAccess[A](
    columns: Array[String], 
    selection: String, 
    selectionArgs: Array[String], 
    groupBy: String, 
    having: String,
    orderBy: String)(fun: (Cursor) ⇒ A ) : Option[A] = {

    val db = dbAccess.getReadableDatabase
    try {
      val cursor = db.query(DATABASE_TABLE, 
                            columns, 
                            selection, 
                            selectionArgs, 
                            groupBy, 
                            having, 
                            orderBy)
      try {
        
        if(! cursor.isAfterLast) Some(fun(cursor))
        else None
        
      } finally { cursor.close }
    } finally { dbAccess.close }
  }
  
  /**
   * the SQLiteOpenHelper gives the access to the sql-database
   */
  protected[database] val dbAccess = new SQLiteOpenHelper(
    accessor.context, accessor.databaseName, 
    null, accessor.databaseVersion) {

    /**
     * Called when creating a database
     * @param db The database.
     * @see SQLiteOpenHelper
     */
    override def onCreate(db: SQLiteDatabase) {
      Log.i(this.getClass.toString, "onCreate " + DATABASE_TABLE)
      db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE)
      db.execSQL(DATABASE_CREATE)
    }

    /**
     * Called when upgrading a database
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     * @see SQLiteOpenHelper
     */
    override def onUpgrade(db: SQLiteDatabase, 
                           oldVersion: Int , newVersion: Int ) {
      Log.i(this.getClass.toString, "onUpgrade " + DATABASE_TABLE)
      db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE)
      db.execSQL(DATABASE_CREATE)
    }
  }
}

