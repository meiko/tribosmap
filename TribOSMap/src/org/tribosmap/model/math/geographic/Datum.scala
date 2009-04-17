package org.tribosmap.model.math.geographic

/**
 * The datum describes a geoid.
 * Used to convert between utm and lat/lon coordinates.
 * @author Meiko Rachimow
 */
trait Datum extends java.io.Serializable {
  
  /**
   * the name of the Datum
   */
  val name: String
  
  /**
   * Semi-major Axis in meter (equatorial radius of the ellipsoid)
   */
  val equatorialRadius: Double
  
  /**
   * (a-b)/a
   * :: a is the equatorial-radius, b is the polar radius
   */
  val flattening: Double
}

object Datum {
  
  object WGS84Datum extends Datum{
    val name = "WGS 84"
    val equatorialRadius = 6378137.0
    val flattening = 1 / 298.257223563
  }
  
  def apply(name: String): Datum = {
    name match {
      case WGS84Datum.name ⇒ WGS84Datum
    }
  }
}
