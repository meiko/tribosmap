package org.tribosmap.model.business.repository

trait DAOAccessor {
  private[model] val markerDAO: MarkerDAO
  
  private[model] val traceDAO: TraceDAO
  
  private[model] val tracePointDAO: TracePointDAO
  
  private[model] val traceSegmentDAO: TraceSegmentDAO
  
  private[model] val geoMapDAO: GeoMapDAO
      
  private[model] val geoReferencedPixelDAO: GeoReferencedPixelDAO
}
