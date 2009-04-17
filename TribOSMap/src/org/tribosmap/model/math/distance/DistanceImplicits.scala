package org.tribosmap.model.math.distance

/**
 * The implicit definitions inside this object will convert an Int or Double
 * to an object, which deliveres the possibility, to use plain numbers
 * as distance objects (e.g. <code>1.m</code> means the same as <code>Distance(1, Meter)</code>)
 * 
 * If you have implemented a new Distance system, you can put new definitions into the
 * DistanceConverter.
 * @author Meiko Rachimow
 */
object DistanceImplicits {
  
  /**
   * This class deliveres functions to create Distance objects.
   * (used with implicit definitions)
   */
  protected[distance] class NumberToDistance(value: Double){
    
    import MetricUnit._
    import AmericanUnit._
    
    /**
     * convert to Kilometer
     * @return the Distance object with a Kilometer unit
     */
    def km = Distance(value, KiloMeter)
        
    /**
     * convert to Meter
     * @return the Distance object with a Meter unit
     */
    def m = Distance(value, Meter)
        
    /**
     * convert to CentiMeter
     * @return the Distance object with a CentiMeter unit
     */
    def cm = Distance(value, CentiMeter)
        
    /**
     * convert to MilliMeter
     * @return the Distance object with a MilliMeter unit
     */
    def mm = Distance(value, MilliMeter)
        
    /**
     * convert to Mile
     * @return the Distance object with a Mile unit
     */
    def mi = Distance(value, Mile)
        
    /**
     * convert to Yard
     * @return the Distance object with a Yard unit
     */
    def yd = Distance(value, Yard)
        
    /**
     * convert to Foot
     * @return the Distance object with a Foot unit
     */
    def ft = Distance(value, Foot)
        
    /**
     * convert to Inch
     * @return the Distance object with a Inch unit
     */
    def in = Distance(value, Inch)
  } 
  
  /**
   * convert a Double object implicit to a DistanceConverter object
   */
  implicit def doubleInDistance(value: Double) = new NumberToDistance(value)
  
  /**
   * convert a Int object implicit to a DistanceConverter object
   */
  implicit def intInDistance(value: Int) = new NumberToDistance(value)
}
