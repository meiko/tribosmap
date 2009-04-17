package org.tribosmap.model.business.service

import java.io.File
import java.util.Date

import org.tribosmap.model.business.domain.{Marker, MarkerType, Trace}
import org.tribosmap.model.business.repository.DAOAccessor
import org.tribosmap.model.math.geographic.Position

/**
 * this is the component of the MarkerService,
 * it is mixed in the IServiceRegistry,
 * it needs access to a MarkerDAO
 * @author Meiko Rachimow
 */
trait MarkerServiceComponent { 
  
  protected[service] val markerService: MarkerService  

  private[model] val daoAccessor: DAOAccessor

    
  
  /**
   * The service to work with a marker
   * to use this class, you have to mix the trait MarkerServiceComponent
   * in a class, and define the repositories...
   */
  protected[service] class MarkerService {
      
    private[this] val markerDAO = daoAccessor.markerDAO
    
    /**
     * TODO: Not implemented yet!
     * Export all markers from the repository to a file
     * @param the file to write to
     */
    def exportAllMarkers(file: File) = {
      require(file.canWrite, "cannot write to file: " + file.getName)
      markerDAO.foreach(
        marker ⇒ println(marker, file))
    }
    
    /**
     * load all markers from the repository
     * 
     * @return the list of markers
     */
    def getAllMarkers(): List[Marker] = {
      markerDAO.fetchAll()
    }
    
    /**
     * create a marker in the repository
     * 
     * @param traceId the id of the marker - maybe null
     * @param name the name of the marker
     * @param information the information of the marker
     * @param typ the type of the marker
     * @param imageFileName the filname of the image - maybe null
     * @param position the position of the marker
     * @return the new marker
     */
    def create(traceId: Long, 
               name: String, 
               information: String, 
               typ: MarkerType.Value, 
               imageFileName: String, 
               time: Long,
               position: Position): Marker = {

      val result = markerDAO.create( Marker(-1l, 
                                     traceId, 
                                     name,  
                                     information, 
                                     typ, 
                                     time, 
                                     imageFileName, 
                                     position ))
      assert(result != null, 
             "cannot save the marker in the repository: " + name)
      result
    }
    
    /** 
     * save a marker to the repository
     * @param marker the marker to save it in the repository
     * @return if saved true else false
     */
    def save(marker: Marker) = {
      val saved = if(marker.id < 0) {
        markerDAO.create(marker) != null
      } else {
        markerDAO.update(marker)
      }
      assert(saved, 
             "cannot save the marker in the repository: " + marker.getName)
      saved
    }
    
    /** 
     * delete a marker in the repository
     * @param marker the marker to delete
     * @return if deleted true else false
     */
    def delete(marker: Marker) = {
      val deleted = markerDAO.delete(marker)
      assert(deleted, 
             "cannot delete the marker from the repository: " + marker.getName)
      deleted
    }
  
    /** 
     * TODO: Not implemented yet !
     * export a marker to a file
     * @param marker
     * @param file
     */
    def export(marker: Marker, file: File) = {
      println(marker.xml, file)
    }
    
    /**
     * call a function on every object, 
     * immediatly when the object is fetched from the repository
     * 
     * @param fun the function to call
     */
    def foreachMarker(fun: (Marker) ⇒ Unit) = 
      markerDAO.foreach(fun)   
    
    /**
     * load the marker from the repository, 
     * will return a marker with new content.
     * 
     * @param marker the marker to refresh
     * @return the new marker
     */
    def reloadMarker(marker: Marker) = {
      require(marker.id >= 0, "this marker is not saved in the repository: " + marker.getName)
      markerDAO.fetch(marker.id)
    }
    
    def markersToXMLString(trace: Trace): String = {
      val lineSeparator = System.getProperty("line.separator")
      val stringBuffer = new StringBuffer()
      markerDAO.foreachMarker(trace)( marker ⇒ {
        stringBuffer.append(marker.xml.toString )
        stringBuffer.append(lineSeparator)
      })
      stringBuffer.toString
    }
  }
  
  /**
   * implicit definition, for Marker objects
   */
  protected implicit def doOnMarker(marker: Marker) = new {
    
    /** 
     * save the marker to the repository
     * @return if saved true else false
     */
    def save() = markerService.save(marker)
    
    /** 
     * delete the marker in the repository
     * @return if deleted true else false
     */
    def delete() = markerService.delete(marker)
    
    /** 
     * export the marker to a file
     * @param file
     */
    def export(file: File) = markerService.export(marker, file)
    
    /**
     * load the marker from the repository, 
     * will return a marker with new content.
     * 
     * @param marker the marker to refresh
     * @return the new marker
     */
    def reload() = markerService.reloadMarker(marker)
              
  }
}