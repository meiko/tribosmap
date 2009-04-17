package org.tribosmap.frontend.map

import android.app.{AlertDialog, Activity}
import android.content.Intent
import android.os.{Bundle, Handler}
import android.view.{Menu, MenuItem, MenuInflater}

import scala.actors.Actor.actor


import org.tribosmap.model.business.domain.GeoMap
import org.tribosmap.frontend.common.list.TribosListActivity
import org.tribosmap.frontend.common.{ActivityHelper, Messager}
import org.tribosmap.model.ServiceAccess

/**
 * This Activity holds a list of all imported GeoMaps.
 * These GeoMap objects were fetched from the repository.
 * The user can delete, or edit them here.
 * And it is possible to select a map, and show it in the MapViewActivity.
 * @author Meiko Rachimow
 */
class MapListActivity extends TribosListActivity[MapRow] 
  with ActivityHelper  with ServiceAccess  {
    
  /**
   * a key used when communicate with the MapEditActivity
   */
  private[this] val KEY_EDITMARKER = 33
    
  ////////////////////////////////////////////////////////
  //Actions
  ////////////////////////////////////////////////////////
    
  /**
   * Called when the user has pressed the delete button in the menu.
   * It will show a dialog, in which the user can delete the selected GeoMap.
   */
  private[this] def deleteAction() {
    
    val mapRow = getSelectedObject
    
    /**
     * delete a GeoMap from the repository
     */
    def delete() {
      mapRow.map.delete
      getListAdapter.remove(mapRow)
      Messager.makeMessageShort(this, getString(R.string.map_deleted))
    }
    
    
    new AlertDialog.Builder(this)            	
      .setTitle(getString(R.string.dialog_question))
      .setMessage(getString(R.string.dialog_question_delete) + mapRow.title + "?")
      .setIcon(R.drawable.warning)
      .setPositiveButton(getString(R.string.dialog_yes), () ⇒ {delete()})
      .setNegativeButton(getString(R.string.dialog_no), () ⇒ {})
      .show()
  }

  /**
   * Called when the user has pressed the edit button in the menu.
   * It will open the MapEditActivity to edit the selected GeoMap.
   */
  private[this] def editAction() {
    Messager.makeMessageShort(this, getString(R.string.window_open))
    val bundle = new Bundle()
    bundle.putSerializable(MapComCodes.MAP_PARAMETER_ID, getSelectedObject.map)
    val intent = new Intent(this, classOf[MapEditActivity])
    intent.putExtras(bundle)
    startActivityForResult(intent, KEY_EDITMARKER)
  }
  
  /**
   * Called when the user has pressed the open button in the menu.
   * It will open the MapViewactivity with the selected GeoMap.
   */
  private[this] def openAction() {
    Messager.makeMessageShort(this, getString(R.string.window_open))
     val bundle = new Bundle()
    bundle.putSerializable(MapComCodes.MAP_PARAMETER_ID, getSelectedObject.map)
    val intent = new Intent(this, classOf[MapViewActivity])
    intent.putExtras(bundle)
    startActivity(intent)
  }
  
  /**
   * This method reloads the GeoMap at the last selected position
   * from the database, and refreshes the row in the listview.
   */
  private[this] def reloadAtLastSelectedPosition() {
    val lastSelectedMarkerRow = getListAdapter.getItem(lastChoosedRowIndex)
    getListAdapter.remove(lastSelectedMarkerRow)
    getListAdapter.insert(
      new MapRow(lastSelectedMarkerRow.map.reload(), getResources, this), 
      lastChoosedRowIndex)
    getListAdapter().notifyDataSetChanged()
  }
  
  ////////////////////////////////////////////////////////
  // Overriden Methods
  ////////////////////////////////////////////////////////

  /**
   * the menuId for the map list
   * @return the menuId for the map list
   * @see TribosListActivity
   */
  protected override def menuId = R.menu.map_list
  
  /**
   * the id of the menu group
   * @return the id of the menu group
   * @see TribosListActivity
   */
  protected override def menuItemGroupId = R.id.menu_group
  
  /**
   * This method will load items (all GeoMaps) into the listview of this activity.
   * The listadapter will be notified about this items.
   * The GeoMaps are fetched from the mapService, wich loads them from the repository.
   */
  protected override def loadItems() {
    val handler = new Handler()
    val msg = Messager.makeMessage(this, getString(R.string.map_loading))
    msg.show()
    actor {
      mapService.foreachMap(map ⇒ {
        val mapRow = new MapRow(map, getResources, this)
        handler.post(new Runnable { def run() {
          getListAdapter.add(mapRow)
          msg.cancel()
        }})
        Thread.sleep(50)
      })
      handler.post(new Runnable { def run() { msg.cancel() }})
    }
  }
  
  /**
   * This method is called if the user selected a menuItem from the option-menu.
   * @param item the MenuItem of the selection
   * @return true if the selection was successful
   * @see TribosListActivity
   */ 
  override def onOptionsItemSelected(item : MenuItem) : Boolean = {
    item.getItemId() match {
      case R.id.open_btn ⇒ openAction()
      case R.id.edit_btn ⇒ editAction()
      case R.id.delete_btn ⇒ deleteAction()
      case _ ⇒ false
    }
    true
  }
  
  /**
   * This method is called when a sub-activity has been finished.
   * In this case - if the MapEditActivity was closed.
   * @param requestCode the original requestcode when opening the subactivity
   * @param resultCode the resultcode of the subactivity
   * @param data the data given from the subactivity
   * @see TribosListActivity
   */
  protected override def onActivityResult(requestCode: Int, resultCode: Int, data : Intent){
    requestCode match {
      case KEY_EDITMARKER ⇒ if(resultCode == Activity.RESULT_OK) {
        reloadAtLastSelectedPosition()
      }
      case _ ⇒ None
    }
  }
}


