package org.tribosmap.model.math.distance

/**
 * this abstract class represents a unit (measurement system)
 * and is used by the class Distance.
 * <p>
 * to implement a new system of units of measurement:</br>
 * - derive this trait with a new trait and call it as the system </br>
 * - derive some objects for all individual units (also for the base unit !)</br>
 * examples are the objects MetricUnit or AmerianUnit
 * @author Meiko Rachimow
 */
trait DistanceUnit extends java.io.Serializable { 
  //the name of an unit
  val name: String
  //the base unit (should be the smallest unit in the system)
  val base: DistanceUnit
  //a factor from every unit to the base unit
  val toBaseFactor: Double 
  //a factor to the metric millimeter unit
  val toMilliMeterFactor : Double
}
