package org.tribosmap.model.business.repository

import org.tribosmap.model.business.domain._


/**
 * the interface for the Marker-repository
 * @author Meiko Rachimow
 */
protected[model] trait MarkerDAO extends BaseDAO[Marker] {

  /**
   * Apply a function to all markers belongs to a trace
   * 
   * @param trace the trace
   * @param fun the function to be applied
   */
  def foreachMarker(trace: Trace)
                   (fun: (Marker) ⇒ Unit): Unit
                     
  /**
   * load all markers from the repository, which are belongs to a trace
   * @param trace the Trace
   * @return list of all markers belonging to the given Trace
   */
  def fetchAllMarkers(trace: Trace): List[Marker]
  

}