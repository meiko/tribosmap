package org.tribosmap.math.test

import org.scalatest.prop.PropSuite
import org.scalacheck.Prop.{forAll, passed ⇒ OKAY}
import org.scalacheck.{Test, Gen}
import org.scalacheck.Prop._

import org.tribosmap.model.math.distance._
import org.tribosmap.model.math.distance.MetricUnit._
import org.tribosmap.model.math.distance.AmericanUnit._
import org.tribosmap.model.math.distance.DistanceImplicits._

/**
 * contains tests for the Distance classes (Metric and AmerivanUnit conversions etc.)
 * @author Meiko Rachimow
 */
trait DistanceTest extends PropSuite {
  
  val testParams: Test.Params
  
  implicit def specialDistanceComparator(some: Distance) = new {
    def =:=(other: Distance) {
      assert(some ≈ other, some + " is not == " + other)
    }
  }

  test("DistanceTest: add, sub and compare with fixed values") {
    1.cm =:= 10.mm
    1.in =:= 25.4.mm
    1.ft =:= 12.in
    1.ft =:= 0.3048.m
    1.yd =:= 3.ft
    3.ft =:= 0.9144.m
    1.mi =:= 5280.ft
    1760.yd =:= 1.609344.km
    1.mm + 1.cm + 1.m + 1.km =:= (1.0/25.4).in + (1.0/30.48).ft + 1.0936133.yd + 0.621371192.mi
    1.mi + 1.km =:= 5280.ft + 1000.m
    2.km - (2*0.621371192).mi =:= 0.m
    1.mi - 5280.ft + 1.km - 1000.m =:= 0.mm
  }
  
  test("DistanceTest: average with fixed values") {
    10.m.average(10.m) =:= 10.m
    100.m.average(200.m, 1.km, 2.km) =:= 825.m
    1.yd.average(3.ft, 36.in) =:= 1.yd
    1.0936133.yd.average(2.m, 100.cm) =:= (4/3.0).m
  }
  
  test("DistanceTest: comparators <, <=, >, >= with fixed values") {
    assert(1.m < 2.m, "false 1.m < 2.m")
    assert(1.m <= 2.m, "false 1.m <= 2.m")
    assert(1.m <= 1.m, "false 1.m <= 1.m")
    assert(1.m >= 1.m, "false 1.m >= 1.m")
    assert(2.m >= 1.m, "false 2.m >= 1.m")
    assert(2.m > 1.m, "false 2.m > 1.m")
    assert(1.km < 1.mi, "false 1.km < 1.mi")
    assert(1.yd <= 1.m, "false 1.yd <= 1.m")
    assert(1.ft > 1.cm, "false 1.ft > 1.cm")
    assert(1.mm < 1.in, "false 1.mm < 1.in")
  }
  
  test("DistanceTest: some direct conversion tests with fixed values") {

    1.mm.to(MilliMeter) =:= 1.mm
    1.mm.to(CentiMeter) =:= 0.1.cm
    1.mm.to(Meter) =:= 0.001.m
    1.mm.to(KiloMeter) =:= 0.000001.km
    1.cm.to(CentiMeter) =:= 1.cm
    1.cm.to(Meter) =:= 0.01.m
    1.cm.to(KiloMeter) =:= 0.00001.km
    1.m.to(Meter) =:= 1.m
    1.m.to(KiloMeter) =:= 0.001.km
    1.km.to(KiloMeter) =:= 1.km
    1.in.to(Inch) =:= 1.in
    1.in.to(Foot) =:= (1/12.0).ft
    1.in.to(Yard) =:= (1/36.0).yd
    1.in.to(Mile) =:= (1/63360.0).mi
    1.ft.to(Foot) =:= 1.ft
    1.ft.to(Yard) =:= (1/3.0).yd
    1.ft.to(Mile) =:= (1/5280.0).mi
    1.yd.to(Yard) =:= 1.yd
    1.yd.to(Mile) =:= (1/1760.0).mi
    1.mi.to(Mile) =:= 1.mi
    1.mm.to(Inch) =:= (1/25.4).in
    1.cm.to(Foot) =:= (1/30.48).ft
    1.m.to(Yard) =:= 1.0936133.yd
    1.km.to(Mile) =:= 0.621371192.mi
    1.mi.to(MilliMeter) =:= 1609344.mm
    1.yd.to(CentiMeter) =:= 91.44.cm
    1.ft.to(Meter) =:= 0.3048.m
    1.in.to(KiloMeter) =:= 0.0000254.km
  }
  
  test("DistanceTest: some test with generators") {
    val values = Gen.choose(0, 1000.0)
    check(forAll(values) { value ⇒ {
      value.mm.to(Foot).to(Yard).to(CentiMeter).to(KiloMeter).to(Inch) =:= value.mm
      value.mm + value.mm + value.mm =:= (3*value).mm
      value.cm + value.m + value.km =:= (1001.01 * value).m
      value.km + value.mi =:= (value * 1000 + value * 1000 * 1.609344).m
      value.km - value.m =:= (value - value/1000).km
      true
    }},testParams)
  }
}
