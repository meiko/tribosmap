package org.tribosmap.model.math

/** 
 *  represents a pair of x and y Integer - value,
 *  describes a point in two dimensions
 * 
 *  @param x the x coordinate
 *  @param y the y coordinate
 *  @author Meiko Rachimow
 */
case class Vector2i(val x : Int, val y : Int) {

  def + (other : Vector2i) : Vector2i = {
     Vector2i(this.x + other.x, this.y + other.y)
  }
  
  def - (other : Vector2i) : Vector2i = {
     Vector2i(this.x - other.x, this.y - other.y)
  }
  
  def * (other : Vector2d) : Vector2i = {
     Vector2i(Math.round(this.x.toDouble * other.x).toInt, 
              Math.round(this.y.toDouble * other.y).toInt)
  }
  
  def * (other : Vector2i) : Vector2i = {
     Vector2i(this.x * other.x, this.y * other.y)
  }
  
  def / (other : Vector2i) : Vector2i = {
     Vector2i(this.x / other.x, this.y / other.y)
  }
  
  def % (other : Vector2i) : Vector2i = {
     Vector2i(this.x % other.x, this.y % other.y)
  }
  
  def * (other : Int) : Vector2i = {
    this *  Vector2i(other, other)
  }
  
  def * (other : Double) : Vector2i = {
    this *  Vector2d(other, other)
  }
  
  def / (other : Int) : Vector2i = {
    this /  Vector2i(other, other)
  }
  
  def / (other : Double) : Vector2d = {
     Vector2d(this.x / other, this.y / other)
  }
  
  def % (other : Int) : Vector2i = {
    this %  Vector2i(other, other)
  }
  
  def + (other : Int) : Vector2i = {
    this +  Vector2i(other, other)
  }
  
  def != (other : Vector2i) : Boolean = {
    ! (this == other)
  }
  
  def abs : Vector2i = {
    Vector2i(Math.abs(this.x), Math.abs(this.y))
  }
  
  def toVector2d =  Vector2d(this.x, this.y)
  
  override def toString() : String = {
    "(" + x.toString + "," + y.toString + ")"
  }
}

