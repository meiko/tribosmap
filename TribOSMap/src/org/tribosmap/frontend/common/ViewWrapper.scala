package org.tribosmap.frontend.common

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.content.res

/**
 * @author Meiko Rachimow
 */
class ViewWrapper(context:Context, attrs:AttributeSet, defStyle:Int) 
  extends FrameLayout(context,attrs,defStyle) {

     def this (context:Context, attrs:AttributeSet) = this(context, attrs, 0)
     def this (context:Context) = this(context, null, 0)
     val a = context.obtainStyledAttributes(attrs, R.styleable.ViewWrapper)
     val id = a.getResourceId(R.styleable.ViewWrapper_wrapLayout, -1)
     if (id > 0) {
       val context = getContext()
       
       if(context.isInstanceOf[Activity]){
         getContext().asInstanceOf[Activity].
           getLayoutInflater.inflate(id, this, true)
         
       }else{
         
         LayoutInflater.from(getContext()).inflate(id, this, true)
       }
     }
       
     a.recycle()
}
