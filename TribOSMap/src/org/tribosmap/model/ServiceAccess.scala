package org.tribosmap.model

import org.tribosmap.model.business.service._
import org.tribosmap.model.business.repository._
import org.tribosmap.model.database._

import android.content.Context

/**
 * The base class of activities which have access to the services of the model.
 * Here the instantiated services of the model, are plugged with the DAO Access objects.
 * (in this case with a database implementation (org.tribosmap.database))
 * @author Meiko Rachimow
 */
trait ServiceAccess 
  extends TraceServiceComponent 
  with MarkerServiceComponent 
  with GeoMapServiceComponent
  with TraceSegmentServiceComponent { 
    
  protected val context: Context
  
 /**
  * the markerService  
  */
  lazy val markerService = new MarkerService()
  
 /**
  * the traceService  
  */
  lazy val traceService = new TraceService()
  
 /**
  * the mapService  
  */
  lazy val mapService = new GeoMapService()
  
 /**
  * the traceSegmentService  
  */
  lazy val traceSegmentService = new TraceSegmentService()

  
  private[model] lazy val daoAccessor = new SQLiteDAOAccessor(
    context, "tribosmap", 1)
  
}
