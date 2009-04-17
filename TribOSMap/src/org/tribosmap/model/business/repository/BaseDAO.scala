package org.tribosmap.model.business.repository

/**
 * The base for all repository implementations.
 * These repositories are used by the Services (package: org.tribosmap.model.service)
 * They are responsible to persist different data.
 * <p>
 * To implement a repository, all classes in the package: org.tribosmap.model.repository must
 * be implemented. (an example is the database implementation org.tribosmap.database)
 * @author Meiko Rachimow
 */
protected[model] trait BaseDAO[T <: {val id: Long}] {
  
  /**
   * apply a function to all elements in the repository,
   * 
   * @param fun the function applied to all found objects
   */
  def foreach(fun: (T) ⇒ Unit): Unit
  
  /**
   * load an object of type <code>T</code> by the id from the repository
   * @param id the id of the object
   * @return the object of type <code>T</code>
   */
  def fetch(id: Long): T
  
  /**
   * load all objects of type <code>T</code> from the repository
   * @return the objects of type <code>T</code> as a <code>List[T]</code>
   */
  def fetchAll(): List[T]
  

  
  /**
   * delete an objet from the repository
   * @param obj the object of type <code>T</code> to delete
   * @return <code>true</code> if the deletion was successful
   */
  def delete(obj: T): Boolean
  
  /**
   * create an objet in the repository, and return the object with
   * the correct id.
   * The id is given by the repository.
   * @param obj the object of type <code>T</code> to create
   * @return the saved object of type <code>T</code> with the correct id
   */
  def create(obj: T): T
  
  /**
   * update an existing object in the repository
   * @param obj the object of type <code>T</code> to update
   * @return <code>true</code> if the update was successful
   */
  def update(obj: T): Boolean
  
}
