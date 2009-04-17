package org.tribosmap.model.math.distance

/**
 * A Distance unit (used by class org.tribosmap.math.distance.Distance)
 * international measure, United States customary system
 * based on inch (1 inch is equal to 25,4 mm)
 * @author Meiko Rachimow
 */
object AmericanUnit {

  private[AmericanUnit] trait AmericanUnit extends DistanceUnit {
    val toMilliMeterFactor = 25.4
    val base = Inch
  }
  
  /**
   * a mile unit (international measure)
   */
  object Mile extends AmericanUnit{ val name = "mi"; val toBaseFactor = 63360.0 }
  
  /**
   * a yard unit (international measure)
   */
  object Yard extends AmericanUnit { val name = "yd"; val toBaseFactor = 36.0 }
  
  /**
   * a foot unit (international measure)
   */
  object Foot extends AmericanUnit { val name = "ft"; val toBaseFactor = 12.0 }
  
  /**
   * an inch unit (international measure)
   */
  object Inch extends AmericanUnit { val name = "in"; val toBaseFactor = 1.0 }
}