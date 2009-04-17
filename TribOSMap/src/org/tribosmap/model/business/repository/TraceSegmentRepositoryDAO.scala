package org.tribosmap.model.business.repository

import org.tribosmap.model.business.domain._

/**
 * the interface for the TraceSegment-repository
 * @author Meiko Rachimow
 */
protected[model] trait TraceSegmentDAO extends BaseDAO[TraceSegment] {

  /**
   * Apply a function to all markers of a trace
   * 
   * @param trace the trace
   * @param fun the function to be applied
   */
  def foreachSegment(trace: Trace)
                    (fun: (TraceSegment) ⇒ Unit): Unit
                     
  /**
   * loads all traceSegments from the repository,
   * which are connected by a trace
   *
   * @param trace the trace containing the segments
   * @return list of all segments in the trace with given id
   */
  def fetchAllSegments(trace: Trace): List[TraceSegment]
  

}

