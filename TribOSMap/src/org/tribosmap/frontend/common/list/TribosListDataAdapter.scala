package org.tribosmap.frontend.common.list

import android.app.Activity
import android.widget.{ArrayAdapter, TextView, ImageView, ListView}
import android.view.{View, ViewGroup}
import android.util.Log


/**
 * The data adapter for the tribos-listactivity
 * @author Meiko Rachimow
 */
final protected class TribosListDataAdapter[T <: TribosListRow](
  activity:Activity, resource:Int, textViewResourceId:Int, val objects:java.util.List[T])
    extends ArrayAdapter[T](activity, resource, textViewResourceId, objects) {
  
  /**
   * a row
   */
  protected class ViewRow(val title:TextView, 
                          val subTitle:TextView, 
                          val icon:ImageView,
                          val information:TextView)
  
  private val selectedColor = activity.getResources.getDrawable(R.drawable.selected)

  override def getView(position:Int, convertView:View, parent:ViewGroup) : View = {
    
    val view = if (convertView == null) { 
      
      val newView = activity.getLayoutInflater.inflate(resource, null) 
      newView.setTag(
        new ViewRow(newView.findViewById(R.id.row_title).asInstanceOf[TextView],
                    newView.findViewById(R.id.row_subtitle).asInstanceOf[TextView],
                    newView.findViewById(R.id.row_icon).asInstanceOf[ImageView],
                    newView.findViewById(R.id.row_information).asInstanceOf[TextView]))
      
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
    
    item.subtitle match {
      case Some(subtitle) ⇒ viewTag.subTitle.setText(subtitle)
      case _ ⇒ viewTag.subTitle.setText(null)
    }
    
    item.icon match {
      case Some(icon) ⇒ viewTag.icon.setImageDrawable(icon)
      case _ ⇒ viewTag.icon.setImageDrawable(null)
    }
    
    parent.asInstanceOf[ListView].getCheckedItemPosition match {
      case `position` ⇒ {
        viewTag.information.setText(item.information.getOrElse(""))
        view.setBackgroundDrawable(selectedColor)
      }
      case _ ⇒ {
        viewTag.information.setText("")
        view.setBackgroundDrawable(null)
      }
    }
    
    view
  }
}


