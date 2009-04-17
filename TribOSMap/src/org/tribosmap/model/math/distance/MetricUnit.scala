package org.tribosmap.model.math.distance
 
/**
 * A Metric unit (used by class org.tribosmap.math.distance.Distance)
 * based on Millimeter
 * @author Meiko Rachimow
 */
object MetricUnit {


  private[MetricUnit] trait MetricUnit extends DistanceUnit  {
    val toMilliMeterFactor = 1.0
    val base = MilliMeter
  }
  
  /**
   * a Kilometer unit
   */
  object KiloMeter extends MetricUnit{ val name = "km"; val toBaseFactor = 1000000.0 } 
  
  /**
   * a Meter unit
   */
  object Meter extends MetricUnit { val name = "m"; val toBaseFactor = 1000.0 } 
  
  /**
   * a CentiMeter unit
   */
  object CentiMeter extends MetricUnit { val name = "cm"; val toBaseFactor = 10.0 }
  
  /**
   * a MilliMeter unit
   */
  object MilliMeter extends MetricUnit { val name = "mm"; val toBaseFactor = 1.0 }
}