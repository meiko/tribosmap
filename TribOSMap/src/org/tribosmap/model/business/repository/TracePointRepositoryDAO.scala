package org.tribosmap.model.business.repository

import org.tribosmap.model.business.domain._

/**
 * the interface for the TracePoint-repository
 * @author Meiko Rachimow
 */
protected[model] trait TracePointDAO extends BaseDAO[TracePoint] {
  
  /**
   * create an TracePoint in the repository, returns the id of the saved object
   * (this method should be very performant, because it is used when recording a trace)
   * @param point the TracePoint
   * @return the id of the saved object
   */
  def createFast(point: TracePoint): Long
  
  /**
   * Apply a function to all points of a segment
   * 
   * @param segment the segment
   * @param fun the function to be applied
   */
  def foreachPoint(segment: TraceSegment)
                  (fun: (TracePoint) ⇒ Unit): Unit


}

