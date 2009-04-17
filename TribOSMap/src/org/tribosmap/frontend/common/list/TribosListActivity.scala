package org.tribosmap.frontend.common.list

import android.app.ListActivity
import android.os.Bundle
import android.widget.{ListView, TextView, LinearLayout}
import android.widget.AdapterView.{OnItemClickListener, INVALID_POSITION}
import android.widget.LinearLayout.LayoutParams
import android.view.{View, Menu, MenuInflater}
import android.util.{Log, DisplayMetrics}


/**
 * A ListActivity, can be used to display objects of type <code>T <: ListRow[_]</code>
 * @author Meiko Rachimow
 */
abstract class TribosListActivity[T <: TribosListRow] 
  extends ListActivity {
  
  /**
   * last choosed rowIndex in the list
   */
  protected[this] var lastChoosedRowIndex: Int = -1  
  
  /**
   * the id of the menu (referenced by a layout xml)
   */
  protected def menuId() : Int
  
  /**
   * the id of the menuItemGroup (referenced by a layout xml)
   */
  protected def menuItemGroupId() : Int
  
  /**
   * will return the object, which was choosed in the list by the user
   * @return the choosed object
   */
  protected def getSelectedObject : T = { 
    lastChoosedRowIndex = getListView.getCheckedItemPosition
    getListAdapter.getItem(lastChoosedRowIndex)
  }
  
  /**
   * convert a scala list to a java list when needed
   */
  protected implicit def scalaListToJavaList(xs: scala.List[T]): java.util.List[T] = {
    val l = new java.util.LinkedList[T]
    xs.foreach(x ⇒ l.add(x))
    l
  }


  /**
   * load the items into the list, 
   * the listadapter has to be notified about the new items
   */
  protected def loadItems()
  
  /**
   * @return the underlying listAdapter
   * @see ListActivity
   */
  override def getListAdapter() : TribosListDataAdapter[T] = {
    super.getListAdapter.asInstanceOf[TribosListDataAdapter[T]]
  }
  
  /**
   * @param savedInstanceState the bundle
   * @see Activity
   */
  protected override def onCreate(savedInstanceState : Bundle) {
    super.onCreate(savedInstanceState)

    val dm = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(dm);
    val height = dm.heightPixels;
    
    getListView.setItemsCanFocus(false)
    getListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE)
    
    val footer = new View(this)
    footer.setMinimumHeight(height / 4)
    getListView.addFooterView(footer, null, false)
    
    setListAdapter(new TribosListDataAdapter[T](
      this, R.layout.list_entry, R.id.row_title, List()))
    
    loadItems()
  }
  
  /**
   * @see ListActivity
   */
  override def onPrepareOptionsMenu(menu : Menu) : Boolean = {
    menu.setGroupVisible(menuItemGroupId, 
                         getListView.getCheckedItemPosition != INVALID_POSITION)
    
    true
  }
  
  /**
   * @see ListActivity
   */
  override def onCreateOptionsMenu(menu : Menu) : Boolean = {
    getMenuInflater.inflate(menuId, menu)
    true
  }
  
  /**
   * @see ListActivity
   */
  override def onOptionsMenuClosed(menu : Menu) {
    getListView.clearChoices()
    getListAdapter.notifyDataSetChanged
  }
  
  /**
   * @see ListActivity
   */
  protected override def onListItemClick(
    parent:ListView, view:View, position:Int, id:Long) {
    
    super.onListItemClick(parent, view, position, id)
    getListView.setSelectionFromTop(position, 0)
    openOptionsMenu()
    
  }
  
}




