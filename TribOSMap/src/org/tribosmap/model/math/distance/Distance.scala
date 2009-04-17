package org.tribosmap.model.math.distance

import java.text.DecimalFormat

/**
 * This class represents a distance with a value and a distanceType (DistanceUnit)
 * <p>
 * The Distance Implementation is based on Millimeters,
 * so it depends on the class MetricUnit and the Object MilliMeter !
 * to convert between each Measurement-system, the MilliMeter is used as the base.
 * (see DistanceUnit and see MetricUnit)
 * </p>
 * @param value the number (value) of the distance
 * @param unit the Distance Unit of measurement
 * @author Meiko Rachimow
 */
case class Distance(value: Double, unit: DistanceUnit)  extends java.io.Serializable 
  with Ordered[Distance] {

  /**
   * the Millimeter value of the Distance
   */
  val milliMeterValue = (value * unit.toBaseFactor) * unit.toMilliMeterFactor
  
  /**
   * convert a distance of a specified type (measurement system) to
   * another Unit and/or System
   * @param i_unit the target Unit
   * @return the new distance object
   */
  def to(i_unit: DistanceUnit) = {
    Distance((milliMeterValue / i_unit.toMilliMeterFactor ) /  i_unit.toBaseFactor, i_unit)
  }
  
  /**
   * Helper for some public methods, will call a function on the values
   * of this and an other object. The Unit(type) of the reslut depends on the 
   * types of this and the other object.
   * If both have the same Unit-type, the result will also have this Unit.
   * Else if both have the same base-type (the same measurement system), 
   * the result will get the base-unit of that system. Else the result will get the 
   * MetricUnit/MilliMeter Unit as unit.
   * @param other the other distance
   * @param fun a function which take the conveted Distances of this and the other object as parameter.
   * @return the new distance object
   */  
  protected def doFunBetweenTwo[T](other: Distance)
                                  (fun: (Distance, Distance) ⇒ T) = {
    if(other.unit == unit) 
      fun(this, other)
    else if(other.unit.base == unit.base)
      fun(this.to(unit.base), other.to(unit.base))
    else
      fun(this.to(MetricUnit.MilliMeter), other.to(MetricUnit.MilliMeter))
  }	

  /**
   * add a distance to this object
   * @param other the other distance
   * @return the new distance object
   */
  def +(other: Distance) = doFunBetweenTwo(other) {
    (a, b) ⇒ Distance(a.value + b.value, a.unit )
  }
  /**
   * subtract a distance to this object
   * @param other the other distance
   * @return the new distance object
   */
  def -(other: Distance) = doFunBetweenTwo(other) {
    (a, b) ⇒ Distance(a.value - b.value, a.unit )
  }
  
  def /(divisor: Double) = {
    Distance(value / divisor , unit )
  }
  
  /**
   * computes the average of this and some other distances
   * @param others the other distance objects
   * @return the new distance object
   */
  def average(others: Distance*) =
    others.reduceLeft(_ + _) + this / (others.length + 1)
  
  override def compare(that : Distance) : Int = { milliMeterValue.compare(that.milliMeterValue) }
  
  def differenceTo(that: Distance) = Distance(
    Math.abs(milliMeterValue - that.milliMeterValue), MetricUnit.MilliMeter)

  /**
   * this function will return true, if the difference between the compared distances is smaller than 0.001mm
   * to compare the equality use <code>equal(obj: Object)</code> instaead.
   * @param that the other distance objects
   * @return true if this distance is similar to the other distance
   */
  def alwaysEqual(that: Distance) = differenceTo(that) < Distance(0.001, MetricUnit.MilliMeter)
  
  /**
   * @see Distance.alwaysEquals
   */
  def ≈(that: Distance) =  alwaysEqual(that)
  
  /**
   * this function will return false, if the difference between the compared distances is smaller than 0.001mm
   * to compare the equality use <code>equal(obj: Object)</code> instaead.
   * @param that the other distance objects
   * @return true if this distance is similar to the other distance
   */
  def notAlwaysEqual(that: Distance) = ! alwaysEqual(that)
  
  /**
   * @see Distance.notAlwaysEqual
   */
  def ≉(that: Distance) = notAlwaysEqual(that)
  
  
  override def toString: String = {
    val format = new DecimalFormat();
    format.setMaximumFractionDigits(3);
    val valueString = format.format(value)
    valueString + " " + unit.name
  }
  
}