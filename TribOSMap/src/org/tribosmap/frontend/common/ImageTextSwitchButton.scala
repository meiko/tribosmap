package org.tribosmap.frontend.common

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import android.widget.TextView
import android.view.View
import android.util.Log

/**
 * @author Meiko Rachimow
 */
class ImageTextSwitchButton(context:Context, attrs:AttributeSet, defStyle:Int) 
  extends ImageTextButton(context, attrs, defStyle) {

  def this (context:Context, attrs:AttributeSet) = this(context, attrs, 0)
  def this (context:Context) = this(context, null, 0)
  
  protected val attributes = 
    context.obtainStyledAttributes(attrs, R.styleable.ImageTextSwitchButton)
  
  protected val srcOff = 
    attributes.getResourceId(R.styleable.ImageTextSwitchButton_src_off, -1)
  
  protected val textOff = 
    attributes.getResourceId(R.styleable.ImageTextSwitchButton_text_off, -1)
  
  attributes.recycle()
  
  def isOn = on
  private var on = false
  
  override def setOnClickAction(fkt:()⇒Any){
    button.setOnClickListener(new View.OnClickListener{def onClick(v:View){
      if(on) {
        setOn()
      }
      else {
        setOff()
      }
      fkt()
      on = !on
    }})
  }
  
  def setOn() {
    textField.setText(context.getResources.getString(text))
    button.setImageResource(src)
  }
  
  def setOff() {
    textField.setText(context.getResources.getString(textOff))
    button.setImageResource(srcOff)
  }
}

