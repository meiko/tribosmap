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
class ImageTextButton(context:Context, attrs:AttributeSet, defStyle:Int) 
  extends ViewWrapper(context, attrs, defStyle) {

  def this (context:Context, attrs:AttributeSet) = this(context, attrs, 0)
  def this (context:Context) = this(context, null, 0)
  
  private val styledAttrs = 
    context.obtainStyledAttributes(attrs, R.styleable.ImageTextButton)
  
  protected val text = 
    styledAttrs.getResourceId(R.styleable.ImageTextButton_text, -1)
  
  protected val src = 
    styledAttrs.getResourceId(R.styleable.ImageTextButton_src, -1)
  
  protected val textSize = 
    styledAttrs.getResourceId(R.styleable.ImageTextButton_text_size, -1)
  
  protected val button = 
    findViewById(R.id.btn).asInstanceOf[ImageButton]
  
  protected val textField = 
    findViewById(R.id.text).asInstanceOf[TextView]
  
  if(textSize > -1) textField.setTextSize(textSize)
  if(text > -1) textField.setText(context.getResources.getString(text))
  if(src > -1) button.setImageResource(src)

  styledAttrs.recycle()
  
  def setOnClickAction(fkt:()⇒Any){
    button.setOnClickListener(
      new View.OnClickListener{
        def onClick(v:View){fkt()}})
  }
}

