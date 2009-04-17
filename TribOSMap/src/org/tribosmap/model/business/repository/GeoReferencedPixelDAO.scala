package org.tribosmap.model.business.repository

import org.tribosmap.model.business.domain._

/**
 * the interface for the GeoReferencedPixel-repository
 * @author Meiko Rachimow
 */
protected[model] trait GeoReferencedPixelDAO extends BaseDAO[GeoReferencedPixel] {
  
  /**
   * load all GeoReferencedPixels from the repository,
   * which are belongs to a given GeoMap
   *
   * @param map the GeoMap containing the referenced pixels
   * @return list of all GeoReferencedPixel in the GeoMap
   */
  def fetchAll(map: GeoMap): List[GeoReferencedPixel]  
}