package org.tribosmap.frontend.trace

/**
 * Communication codes for the TraceActivities.
 * Used by the Activities to communicate (see: Intent / Activity / Bundle)
 * @author Meiko Rachimow
 */
protected[trace] object TraceComCodes {
  
  /**
   * this flag is used as a key in a bundle to identify a serialized trace
   */
  val TRACE_PARAMETER_ID = "TRACE_PARAMETER_ID"
}
