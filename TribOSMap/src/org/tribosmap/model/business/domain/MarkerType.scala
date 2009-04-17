package org.tribosmap.model.business.domain

/**
 * The Type of a marker, at the moment there are two types: 
 * one for the actual position of the user and the other one for all other markers (PointOfInterest)
 * @author Meiko Rachimow 
 */
object MarkerType extends Enumeration("ActualPosition", "PointOfInterest") {
  type MarkerType = Value
  val ActualPosition, PointOfInterest = Value
}

