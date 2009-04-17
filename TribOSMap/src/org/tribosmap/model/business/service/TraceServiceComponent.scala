package org.tribosmap.model.business.service

import java.io.File
import java.util.Date

import android.location.{Location}
import org.tribosmap.model.business.repository.DAOAccessor
import org.tribosmap.model.business.domain.{Trace, TraceSegment, TracePoint}
import org.tribosmap.model.math.geographic.{Position, GeographicCoOrdinatePair}
import org.tribosmap.model.math.distance.Distance
import org.tribosmap.model.math.distance.MetricUnit.Meter

/**
 * this is the component of the TraceService,
 * it is mixed in the IServiceRegistry,
 * it needs access to a TraceDAO, a TraceSegmentDAO, 
 * a TracePointDAO and a MarkerDAO
 * @author Meiko Rachimow
 */
trait TraceServiceComponent {
  this: MarkerServiceComponent with TraceSegmentServiceComponent =>
  
  //a Service to work with traces
  protected[service] val traceService: TraceService

  private[model] val daoAccessor: DAOAccessor

  
  
  /**
   * The service to work with a trace and the containing objects in the trace
   * (traceSegment and tracePoint)
   * to use this class, you have to mix the trait TraceServiceComponent
   * in a class, and define the repositories...
   */
  protected[service] class TraceService {

    //the repositories
    private[this] val traceDAO = daoAccessor.traceDAO
    
    /**
     * call a function on every object, 
     * immediatly when the object is fetched from the db
     * 
     * @param fun the function to call
     */
    def foreachTrace(fun: (Trace) ⇒ Unit) = 
      traceDAO.foreach(fun)
    
    /**
     * load all traces from the repository
     * 
     * @return the list of traces
     */
    def getAllTraces(): List[Trace] = {
      traceDAO.fetchAll()
    }
    
    /**
     * create a trace in the repository
     * 
     * @param name the name of the trace to create
     * @param information the information of the trace to create
     * @param time the creation time for the trace
     * @return the new trace
     */
    def create(name:String, 
               information:String,
               time: Long ) = {

      traceDAO.create(
        Trace(-1,
              name, 
              information, 
              time, 
              0, 
              Distance(0, Meter) )
      )
    }
    
    /**
     * Save a trace to the repository
     * 
     * @param trace the Trace
     * @return if saved true else false
     */
    def save(trace: Trace):Boolean  = {
      
      val saved = if(trace.id < 0) {
        traceDAO.create(trace) != null
      } else {
        traceDAO.update(trace)
      }
      assert(saved, 
             "cannot save the trace in the repository: " + trace.getName)
      saved
    }

    /**
     * Delete a trace from the repository
     * 
     * @param trace the Trace
     * @return if deleted true else false
     */
    def delete(trace: Trace) =  {
      val deleted = traceDAO.delete(trace)
      assert(deleted, "cannot delete trace: " + trace.getName)
      deleted
    }
    

    
    /**
     * Add a new segment to a trace and save it in the repository
     * 
     * @param trace the Trace to add the segment
     * @return the created traceSegment
     */
    def addNewSegment(trace: Trace): TraceSegment = {
      save(trace)
      traceSegmentService.create(trace)
    }
    
    /**
     * load the trace from the repository, 
     * will return a trace with new content.
     * 
     * @param trace the trace to refresh
     * @return the new trace
     */
    def reloadTrace(trace: Trace) = {
      traceDAO.fetch(trace.id)
    }
    
    def traceToXMLString(trace: Trace) = {
      
      val lineSeparator = System.getProperty("line.separator")
      val stringBuffer = new StringBuffer()
      def append(string: String) {
        stringBuffer.append(string)
        stringBuffer.append(lineSeparator)
      }
      
      append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
             "<gpx version=\"1.1\" creator=\"TribosMap\" "+
             "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "+
             "xmlns=\"http://www.topografix.com/GPX/1/0\""+
             " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 "+ 
             "http://www.topografix.com/GPX/1/0/gpx.xsd\\\">")
      
      append(markerService.markersToXMLString(trace))
      
      val traceXML = trace.xml
      append("<" + traceXML.label + ">")
      
      traceXML.child.foreach( child ⇒ {
        append(child.toString)
      })
      
      append(traceSegmentService.segmentsToXmlString(trace))
      
      append("</" + traceXML.label + ">")
      
      append("</gpx>")
      
      stringBuffer.toString
    }

    /**
     * Export the trace to a gpx file.
     * a gpx file can be used in the JOSM editor
     * 
     * @param trace the trace to export
     * @param file the file to write to
     */
    def export(trace: Trace, file: File) {
    
      require(file.canWrite, "cannot write to file: " + file.getAbsolutePath)
      val outRAfile = new java.io.RandomAccessFile(file,"rw")
      outRAfile.writeBytes(traceToXMLString(trace))
      outRAfile.close
    }
  }
  
  /**
   * implicit definition, for Trace objects
   */
  protected implicit def doOnTrace(trace: Trace) = new {
    
    /**
     * Save the trace to the repository
     * @return if saved true else false
     */
    def save() = traceService.save(trace)

    /**
     * Delete the trace from the repository
     * 
     * @return if deleted true else false
     */
    def delete() = traceService.delete(trace)
    
    /**
     * Add a new segment to the trace and save it in the repository
     * 
     * @return the created traceSegment
     */
    def addNewSegment() = traceService.addNewSegment(trace)
    
    
    /**
     * Get all segments for the trace
     * @return the list of tracesegments
     */
    def getAllSegments = traceSegmentService.getAllSegments(trace)
    
    /**
     * Export the trace to a gpx file.
     * a gpx file can be used in the JOSM editor
     * 
     * @param file the file to write to
     */
    def export(file: File) = 
      traceService.export(trace, file)
    
    /**
     * load the actual trace from the repository, 
     * will return a trace with new content.
     * 
     * @return the new trace
     */ 
    def reload() = traceService.reloadTrace(trace)
    
  }
}