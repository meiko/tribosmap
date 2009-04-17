package org.tribosmap.testactivity

import android.graphics.drawable.Drawable
import org.scalatest.Report
import android.content.res.Resources
import android.graphics.drawable.{BitmapDrawable, Drawable}
import android.graphics.{BitmapFactory, Bitmap}

private[testactivity] class ReportRow(val report: Report, resources : Resources) {
  
  //the drawable to display a folder
  private[this] val errorDrawable = 
    new BitmapDrawable(BitmapFactory.decodeResource(
      resources, R.drawable.redball_small))
  
  //the drawable to display a file but not a folder
  private[this] val okayDrawable = 
    new BitmapDrawable(BitmapFactory.decodeResource(
      resources, R.drawable.greenball_small))
  
  //title of the row
  private[testactivity] val title : Option[String] = Some({
    report.name.substring(report.name.findIndexOf(_ == ':') + 2)
  })
  
  //icon of the row
  private[testactivity] val icon : Option[Drawable] = 
    Some(if(report.throwable.isDefined){
      errorDrawable
    }else{ 
      okayDrawable 
    })
}
