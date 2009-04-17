package org.tribosmap.model.math

/** 
 * represents a pair of x and y Double - values,
 * describes a point in two dimensions
 * 
 * @param x the x coordinate
 * @param y the y coordinate
 * @author Meiko Rachimow
 */
case class Vector2d(val x : Double, val y : Double) {

  def + (other : Vector2d) =  Vector2d(this.x + other.x, this.y + other.y)
  
  def - (other : Vector2d) =  Vector2d(this.x - other.x, this.y - other.y)
  
  def * (other : Vector2d) =  Vector2d(this.x * other.x, this.y * other.y)
  
  def / (other : Vector2d) =  Vector2d(this.x / other.x, this.y / other.y)
  
  def % (other : Vector2d) =  Vector2d(this.x % other.x, this.y % other.y)
  
  def * (other : Double) : Vector2d = this *  Vector2d(other, other)
  
  def / (other : Double) : Vector2d = this /  Vector2d(other, other)
  
  def % (other : Double) : Vector2d = this %  Vector2d(other, other)
  
  def != (other : Vector2d) : Boolean = {
    ! (this == other)
  }
  
  def toVector2iFloor =  Vector2i(this.x.toInt, this.y.toInt)
   
  def toVector2iRound =  Vector2i(Math.round(this.x.toFloat), Math.round(this.y.toFloat))
  
  def normalize = {
    val length = Math.sqrt(x * x + y * y)
    Vector2d(x/length, y/length)
  }
  
  def length = {
    Math.sqrt(x * x + y * y)
  }
  
  override def toString() = {
    "(" + x.toString + "," + y.toString + ")"
  }
}
