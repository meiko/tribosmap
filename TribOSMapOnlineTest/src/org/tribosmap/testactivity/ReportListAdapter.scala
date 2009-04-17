package org.tribosmap.testactivity

import android.app.Activity
import android.widget.{ArrayAdapter, TextView, ImageView, ListView}
import android.view.{View, ViewGroup}
import android.util.Log

private[testactivity] class ReportListAdapter ( 
  activity:Activity, resource:Int, textViewResourceId:Int, 
  private[testactivity] val objects:java.util.List[ReportRow])
  extends ArrayAdapter[ReportRow](activity, resource, textViewResourceId, objects) {
  
  private[this] class ViewRow(val title:TextView, val icon:ImageView)

  private[this] val selectedColor = activity.getResources.getDrawable(R.drawable.selected)

  override def getView(position:Int, convertView:View, parent:ViewGroup) : View = {
    
    val view = if (convertView == null) { 
      
      val newView = activity.getLayoutInflater.inflate(resource, null) 
      newView.setTag(
        new ViewRow(newView.findViewById(R.id.row_title).asInstanceOf[TextView],
                    newView.findViewById(R.id.row_icon).asInstanceOf[ImageView]))
      
      newView
	} else {
      convertView
	}
    
    val item = objects.get(position)
	val viewTag = view.getTag().asInstanceOf[ViewRow]
    
    item.title match {
      case Some(title) ⇒ viewTag.title.setText(title)
      case _ ⇒ viewTag.title.setText(null)
    }

    item.icon match {
      case Some(icon) ⇒ viewTag.icon.setImageDrawable(icon)
      case _ ⇒ viewTag.icon.setImageDrawable(null)
    }
    
    parent.asInstanceOf[ListView].getCheckedItemPosition match {
      case `position` ⇒ {
        view.setBackgroundDrawable(selectedColor)
      }
      case _ ⇒ {
        view.setBackgroundDrawable(null)
      }
    }
    
    view
  }
}


