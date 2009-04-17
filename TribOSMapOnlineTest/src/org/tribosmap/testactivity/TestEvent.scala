package org.tribosmap.testactivity

private[testactivity] sealed abstract class TestEvent
private[testactivity] object TestEvent {
  case object START_TEST extends TestEvent
  case object END_TEST extends TestEvent
  case object FAILURE extends TestEvent
}
