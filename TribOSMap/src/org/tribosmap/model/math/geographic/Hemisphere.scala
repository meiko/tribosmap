package org.tribosmap.model.math.geographic

sealed abstract class Hemisphere

/**
 * represents the hemisphere (north or south)
 * used for the UtmCoOrdinates
 * @author Meiko Rachimow
 */
object Hemisphere extends Enumeration("North", "South") {
  
  type Hemisphere = Value
  
  val North = Value
  val South = Value
  
  /**
   * @param the name of the hemisphere
   * @return the Hemisphere value object
   */
  def apply(name: String): Hemisphere.Value = {
    val result = filter(_.toString equals name)
    if(result.hasNext) return result.next
    else error("unknown name "+ name +" for enumeration")
  }

}
